package com.github.aakumykov.yandex_disk_cloud_writer

import android.util.Log
import com.github.aakumykov.cloud_writer.CloudWriter
import com.github.aakumykov.cloud_writer.StreamFinishCallback
import com.github.aakumykov.cloud_writer.StreamWritingCallback
import com.github.aakumykov.copy_between_streams_with_counting.copyBetweenStreamsWithCounting
import com.google.gson.Gson
import com.yandex.disk.rest.json.Link
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class YandexDiskCloudWriter(
    private val authToken: String,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build(),
    private val gson: Gson = Gson()
) : CloudWriter {

    private val defaultMediaType: MediaType get() = DEFAULT_MEDIA_TYPE.toMediaType()

    // TODO: проверить с разными аргументами
    @Throws(
        IOException::class,
        CloudWriter.OperationUnsuccessfulException::class,
    )
    override fun createDir(basePath: String, dirName: String) {
        Log.d(TAG, "createDir() called with: basePath = $basePath, dirName = $dirName")
        if (!dirName.contains(CloudWriter.DS)) createOneLevelDir(
            CloudWriter.composeFullPath(
                basePath,
                dirName
            )
        )
        else createMultiLevelDir(basePath, dirName)
    }

    override fun createDirResult(basePath: String, dirName: String): Result<String> {
        return try {
            createDir(basePath, dirName)
            Result.success(File(basePath, dirName).absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(
        IOException::class,
        CloudWriter.OperationUnsuccessfulException::class,
    )
    private fun createMultiLevelDir(parentDirName: String, childDirName: String) {

        Log.d(TAG, "createMultiLevelDir() called with: parentDirName = $parentDirName, childDirName = $childDirName")

        if (fileExists(parentDirName, childDirName))
            return

        var pathToCreate = ""

        childDirName.split(CloudWriter.DS).forEach { dirName ->

            pathToCreate += CloudWriter.DS + dirName

            createOneLevelDir(parentDirName, pathToCreate)
        }
    }


    @Throws(
        IOException::class,
        CloudWriter.OperationUnsuccessfulException::class,
    )
    private fun createOneLevelDir(parentDirName: String, childDirName: String) {
        Log.d(TAG, "createOneLevelDir() called with: parentDirName = $parentDirName, childDirName = $childDirName")
        createOneLevelDir(CloudWriter.composeFullPath(parentDirName, childDirName))
    }

    @Throws(
        IOException::class,
        CloudWriter.OperationUnsuccessfulException::class,
    )
    private fun createOneLevelDir(absoluteDirPath: String) {

        Log.d(TAG, "createOneLevelDir() called with: absoluteDirPath = $absoluteDirPath")

        val url = RESOURCES_BASE_URL.toHttpUrl().newBuilder().apply {
            addQueryParameter("path", absoluteDirPath)
        }.build()

        val requestBody = "".toRequestBody(null)

        val request: Request = Request.Builder()
            .header("Authorization", authToken)
            .url(url)
            .put(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            when (response.code) {
                201 -> return
                else -> throw unsuccessfulResponseException(response)
            }
        }
    }


    @Throws(IOException::class, CloudWriter.OperationUnsuccessfulException::class)
    override fun putFile(file: File, targetPath: String, overwriteIfExists: Boolean) {

        Log.d(TAG, "putFile() called with: file = $file, targetDirPath = $targetPath, overwriteIfExists = $overwriteIfExists")

        val uploadURL = getURLForUpload(targetPath, overwriteIfExists)
        putFileReal(file, uploadURL)
    }


    @Throws(IOException::class, CloudWriter.OperationUnsuccessfulException::class)
    override fun putStream(
        inputStream: InputStream,
        targetPath: String,
        overwriteIfExists: Boolean,
        writingCallback: StreamWritingCallback?,
        finishCallback: StreamFinishCallback?,
    ) {
        val uploadURL = getURLForUpload(targetPath, overwriteIfExists)
        putStreamReal(inputStream, uploadURL, writingCallback, finishCallback)
    }


    @Throws(IOException::class, CloudWriter.OperationUnsuccessfulException::class)
    override fun fileExists(parentDirName: String, childName: String): Boolean {

        Log.d(
            TAG,
            "fileExists() called with: parentDirName = $parentDirName, childName = $childName"
        )

        val dirName = CloudWriter.composeFullPath(parentDirName, childName)

        Log.d(TAG, "dirName: $dirName")

        val url = RESOURCES_BASE_URL.toHttpUrl().newBuilder().apply {
            addQueryParameter("path", dirName)
        }.build()

        val request: Request = Request.Builder()
            .header("Authorization", authToken)
            .url(url)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            return when (response.code) {
                200 -> true
                404 -> false
                else -> throw unsuccessfulResponseException(response)
            }
        }
    }


    @Throws(
        IOException::class,
        CloudWriter.OperationUnsuccessfulException::class,
        CloudWriter.OperationTimeoutException::class
    )
    override fun deleteFile(basePath: String, fileName: String) {

        Log.d(TAG, "deleteFile() called with: basePath = $basePath, fileName = $fileName")

        var timeElapsed = 0L

        try {
            deleteFileSimple(basePath, fileName)
        }
        catch (e: IndeterminateOperationException) {
            Log.d(TAG, "Длительная операция удаления (${hashCode()}) ...")

            while(!operationIsFinished(e.operationStatusLink)) {

                Log.d(TAG, "... операция удаления продолжается (${hashCode()}) ...")

                TimeUnit.MILLISECONDS.sleep(OPERATION_WAITING_STEP_MILLIS)

                timeElapsed += OPERATION_WAITING_STEP_MILLIS

                if (timeElapsed > FILE_OPERATION_WAITING_TIMEOUT_MILLIS)
                    throw CloudWriter.OperationTimeoutException(
                        "Deletion of file '${
                            CloudWriter.composeFullPath(
                                basePath,
                                fileName
                            )
                        }' is timed out. Maybe it is really deleted."
                    )
            }

            Log.d(TAG, "... операция удаления завершена (${hashCode()}).")
        }
    }


    // TODO: проверять, что это каталог
    override fun deleteDir(basePath: String, dirName: String) {
        deleteFileSimple(basePath, dirName)
    }


    override fun deleteDirRecursively(basePath: String, dirName: String) {
        var timeElapsed = 0L
        try {
            deleteFileSimple(basePath, dirName)
        }
        catch (e: IndeterminateOperationException) {
            while(!operationIsFinished(e.operationStatusLink)) {
                TimeUnit.MILLISECONDS.sleep(OPERATION_WAITING_STEP_MILLIS)
                timeElapsed += OPERATION_WAITING_STEP_MILLIS
                if (timeElapsed > DIR_OPERATION_WAITING_TIMEOUT_MILLIS)
                    throw CloudWriter.OperationTimeoutException(
                        "Deletion of file '${
                            CloudWriter.composeFullPath(
                                basePath,
                                dirName
                            )
                        }' is timed out. Maybe it is really deleted."
                    )

            }
        }
    }

    override fun renameFileWithinOneStorage(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean
    ): Boolean {

        val url = MOVE_BASE_URL.toHttpUrl().newBuilder().apply {
            addQueryParameter("from", fromAbsolutePath)
            addQueryParameter("path", toAbsolutePath)
            addQueryParameter("overwrite", overwriteIfExists.toString())
            addQueryParameter("force_async", "false")
        }.build()

        val request: Request = Request.Builder()
            .header("Authorization", authToken)
            .url(url)
            .post(ByteArray(0).toRequestBody(null))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            when (response.code) {
                201 -> return true
                202 -> throw IndeterminateOperationException(linkFromResponse(response))
                else -> throw unsuccessfulResponseException(response)
            }
        }
    }

    @Throws(CloudWriter.OperationUnsuccessfulException::class)
    private fun operationIsFinished(operationStatusLink: String): Boolean {
        Log.d(TAG, "operationIsFinished() called with: operationStatusLink = $operationStatusLink")
        val request = Request.Builder()
            .url(operationStatusLink)
            .header("Authorization", authToken)
            .get()
            .build()
        return okHttpClient.newCall(request).execute().use { response ->
            when(response.code) {
                200 -> statusResponseToBoolean(response)
                else -> throw CloudWriter.OperationUnsuccessfulException(
                    response.code,
                    response.message
                )
            }
        }
    }

    private fun statusResponseToBoolean(response: Response): Boolean {
        val operationStatus = gson.fromJson(response.body?.string(), OperationStatus::class.java)
        return "success" == operationStatus.status
    }

    @Throws(IndeterminateOperationException::class)
    private fun deleteFileSimple(basePath: String, fileName: String) {

//        Log.d(TAG, "deleteFileSimple() called with: basePath = $basePath, fileName = $fileName")

        val path = CloudWriter.composeFullPath(basePath, fileName)

        val url = RESOURCES_BASE_URL.toHttpUrl().newBuilder().apply {
            addQueryParameter("path", path)
            addQueryParameter("permanently", path)
        }.build()

        val request: Request = Request.Builder()
            .header("Authorization", authToken)
            .url(url)
            .delete()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            when (response.code) {
                204 -> return
                202 -> throw IndeterminateOperationException(linkFromResponse(response))
                else -> throw unsuccessfulResponseException(response)
            }
        }
    }


    @Throws(IOException::class, CloudWriter.OperationUnsuccessfulException::class)
    private fun getURLForUpload(targetFilePath: String, overwriteIfExists: Boolean): String {

        Log.d(
            TAG,
            "getURLForUpload() called with: targetFilePath = $targetFilePath, overwriteIfExists = $overwriteIfExists"
        )

        val url = UPLOAD_BASE_URL.toHttpUrl().newBuilder()
            .apply {
                addQueryParameter("path", targetFilePath)
                addQueryParameter("overwrite", overwriteIfExists.toString())
            }.build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", authToken)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful)
                return linkFromResponse(response)
            else
                throw unsuccessfulResponseException(response)
        }
    }


    @Throws(IOException::class, CloudWriter.OperationUnsuccessfulException::class)
    private fun putFileReal(file: File, uploadURL: String) {

        Log.d(TAG, "putFileReal() called with: file = $file, uploadURL = $uploadURL")

        val requestBody: RequestBody = file.asRequestBody(defaultMediaType)

        performUploadRequest(requestBody, uploadURL)
    }

    @Throws(IOException::class, CloudWriter.OperationUnsuccessfulException::class)
    private fun putStreamReal(
        inputStream: InputStream,
        uploadURL: String,
        writingCallback: StreamWritingCallback? = null,
        finishCallback: StreamFinishCallback? = null,
    ) {

        val requestBody: RequestBody = object: RequestBody() {

            override fun contentType(): MediaType = defaultMediaType

            override fun writeTo(sink: BufferedSink) {
                copyBetweenStreamsWithCounting(
                    inputStream = inputStream,
                    outputStream = sink.outputStream(),
                    writingCallback = { count: Long ->
                        writingCallback?.onWriteCountChanged(count)
                    },
                    finishCallback = { readBytesCount, writtenBytesCount ->
                        finishCallback?.onFinish(readBytesCount, writtenBytesCount)
                    }
                )
            }
        }

        performUploadRequest(requestBody, uploadURL)
    }


    private fun performUploadRequest(requestBody: RequestBody, uploadURL: String) {

        val fileUploadRequest = Request.Builder()
            .put(requestBody)
            .url(uploadURL)
            .build()

        okHttpClient.newCall(fileUploadRequest).execute().use { response ->
            if (!response.isSuccessful) throw unsuccessfulResponseException(response)
        }
    }


    private fun unsuccessfulResponseException(response: Response): Throwable
        = CloudWriter.OperationUnsuccessfulException(response.code, response.message)


    private fun linkFromResponse(response: Response): String {
        return gson.fromJson(response.body?.string(), Link::class.java).href
    }


    companion object {
        val TAG: String = YandexDiskCloudWriter::class.java.simpleName

        const val OPERATION_WAITING_STEP_MILLIS = 100L
        const val FILE_OPERATION_WAITING_TIMEOUT_MILLIS = 30_000L
        const val DIR_OPERATION_WAITING_TIMEOUT_MILLIS = 30_000L

        private const val DISK_BASE_URL = "https://cloud-api.yandex.net/v1/disk"
        private const val RESOURCES_BASE_URL = "$DISK_BASE_URL/resources"
        private const val UPLOAD_BASE_URL = "$RESOURCES_BASE_URL/upload"
        private const val MOVE_BASE_URL = "$RESOURCES_BASE_URL/move"

        private const val DEFAULT_MEDIA_TYPE = "application/octet-stream"
    }

    class IndeterminateOperationException(val operationStatusLink: String) : Exception()

    inner class OperationStatus(val status: String)
}
