package com.github.aakumykov.yandex_disk_cloud_writer

import android.util.Log
import com.github.aakumykov.cloud_writer.CloudWriter
import com.github.aakumykov.cloud_writer.CloudWriter.OperationTimeoutException
import com.github.aakumykov.cloud_writer.CloudWriter.OperationUnsuccessfulException
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
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDir(basePath: String, dirName: String): String {
        Log.d(TAG, "createDir(basePath = $basePath, dirName = $dirName)")
        return CloudWriter.composeFullPath(basePath, dirName).let {
            createDir(it)
            it
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDir(absoluteDirPath: String) {
        Log.d(TAG, "createDir($absoluteDirPath)")
        createOneLevelDir(absoluteDirPath)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDeepDirIfNotExists(absoluteDirPath: String, force: Boolean) {
        Log.d(TAG, "createDeepDirIfNotExists(absoluteDirPath = $absoluteDirPath, force = $force)")
        absoluteDirPath
            .split(CloudWriter.DS)
            .reduce { acc, s ->
                createDirIfNotExists(acc, force)
                acc + CloudWriter.DS + s
            }.also { tailDir: String ->
                createDirIfNotExists(tailDir, force)
            }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDirResult(basePath: String, dirName: String): Result<String> {
        Log.d(TAG, "createDirResult(basePath = $basePath, dirName = $dirName)")
        return try {
            createDir(basePath, dirName)
            Result.success(File(basePath, dirName).absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    private fun createMultiLevelDir(parentDirName: String, childDirName: String): String {

        Log.d(TAG, "createMultiLevelDir(parentDirName = $parentDirName, childDirName = $childDirName)")

        val absoluteDirPath = CloudWriter.composeFullPath(parentDirName, childDirName)

        if (fileExists(parentDirName, childDirName))
            return absoluteDirPath

        var pathToCreate = ""

        childDirName.split(CloudWriter.DS).forEach { dirName ->

            pathToCreate += CloudWriter.DS + dirName

            createOneLevelDir(parentDirName, pathToCreate)
        }

        return absoluteDirPath
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    private fun createOneLevelDir(parentDirName: String, childDirName: String) {
        Log.d(TAG, "createOneLevelDir(parentDirName = $parentDirName, childDirName = $childDirName)")
        createOneLevelDir(CloudWriter.composeFullPath(parentDirName, childDirName))
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    private fun createOneLevelDir(absoluteDirPath: String): String {

        Log.d(TAG, "createOneLevelDir($absoluteDirPath)")

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
                201 -> return absoluteDirPath
                else -> throw unsuccessfulResponseException(response, "Failed creating one level dir '$absoluteDirPath'")
            }
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDirIfNotExists(basePath: String, dirName: String, force: Boolean): String {
        Log.d(TAG, "createDirIfNotExists(basePath = $basePath, dirName = $dirName, force = $force)")
        if (!force && !fileExists(basePath, dirName))
            createOneLevelDir(basePath, dirName)
        return CloudWriter.composeFullPath(basePath, dirName)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDirIfNotExists(absoluteDirPath: String, force: Boolean) {
        Log.d(TAG, "createDirIfNotExists(absoluteDirPath = $absoluteDirPath, force = $force)")
        if (!force && !fileExists(absoluteDirPath))
            createOneLevelDir(absoluteDirPath)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun putFile(sourceFile: File, targetAbsolutePath: String, overwriteIfExists: Boolean) {
        Log.d(TAG, "putFile(sourceFile = '$sourceFile', targetAbsolutePath = '$targetAbsolutePath', overwriteIfExists = $overwriteIfExists)")
        val uploadURL = getURLForUpload(targetAbsolutePath, overwriteIfExists)
        putFileReal(sourceFile, uploadURL)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun putStream(
        inputStream: InputStream,
        targetPath: String,
        overwriteIfExists: Boolean,
        writingCallback: ((Long) -> Unit)?,
        finishCallback: ((Long,Long) -> Unit)?,
    ) {
        Log.d(TAG, "putStream(inputStream = $inputStream, targetPath = $targetPath, overwriteIfExists = $overwriteIfExists, writingCallback = $writingCallback, finishCallback = $finishCallback)")
        val uploadURL = getURLForUpload(targetPath, overwriteIfExists)
        putStreamReal(inputStream, uploadURL, writingCallback, finishCallback)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun fileExists(parentDirName: String, childName: String): Boolean {
        Log.d(TAG, "fileExists(parentDirName='$parentDirName', childName='$childName')")
        return fileExists(CloudWriter.composeFullPath(parentDirName, childName))
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun fileExists(absolutePath: String): Boolean {

        Log.d(TAG, "fileExists('$absolutePath')")

        val url = RESOURCES_BASE_URL.toHttpUrl().newBuilder().apply {
            addQueryParameter("path", absolutePath)
        }.build()

        val request: Request = Request.Builder()
            .header("Authorization", authToken)
            .url(url)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            return when (response.code) {
                200 -> true
                404 -> false
                else -> throw unsuccessfulResponseException(response, "File checking file existence: '$absolutePath'")
            }
        }
    }


    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    override fun deleteFile(basePath: String, fileName: String) {

        Log.d(TAG, "deleteFile(basePath = $basePath, fileName = $fileName)")

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
                    throw OperationTimeoutException(
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
        Log.d(TAG, "deleteDir(basePath = $basePath, dirName = $dirName)")
        deleteFileSimple(basePath, dirName)
    }


    override fun deleteDirRecursively(basePath: String, dirName: String) {
        Log.d(TAG, "deleteDirRecursively(basePath = $basePath, dirName = $dirName)")
        var timeElapsed = 0L
        try {
            deleteFileSimple(basePath, dirName)
        }
        catch (e: IndeterminateOperationException) {
            while(!operationIsFinished(e.operationStatusLink)) {
                TimeUnit.MILLISECONDS.sleep(OPERATION_WAITING_STEP_MILLIS)
                timeElapsed += OPERATION_WAITING_STEP_MILLIS
                if (timeElapsed > DIR_OPERATION_WAITING_TIMEOUT_MILLIS)
                    throw OperationTimeoutException(
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


    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    override fun renameFileOrEmptyDir(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean
    ): Boolean {

        Log.d(TAG, "renameFileOrEmptyDir(fromAbsolutePath = $fromAbsolutePath, toAbsolutePath = $toAbsolutePath, overwriteIfExists = $overwriteIfExists)")

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
                else -> throw unsuccessfulResponseException(response, "Fail renaming file from '$fromAbsolutePath' to '$toAbsolutePath' with overwriteIfExists='$overwriteIfExists'")
            }
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun moveFileOrEmptyDir(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean
    ): Boolean {
        Log.d(TAG, "moveFileOrEmptyDir(fromAbsolutePath = $fromAbsolutePath, toAbsolutePath = $toAbsolutePath, overwriteIfExists = $overwriteIfExists)")
        return renameFileOrEmptyDir(fromAbsolutePath, toAbsolutePath, overwriteIfExists)
    }


    @Throws(OperationUnsuccessfulException::class)
    private fun operationIsFinished(operationStatusLink: String): Boolean {
        Log.d(TAG, "operationIsFinished(operationStatusLink = $operationStatusLink)")
        val request = Request.Builder()
            .url(operationStatusLink)
            .header("Authorization", authToken)
            .get()
            .build()
        return okHttpClient.newCall(request).execute().use { response ->
            when(response.code) {
                200 -> statusResponseToBoolean(response)
                else -> throw OperationUnsuccessfulException(
                    response.code,
                    response.message
                )
            }
        }
    }


    @Deprecated("Избавиться")
    private fun statusResponseToBoolean(response: Response): Boolean {
        Log.d(TAG, "statusResponseToBoolean(response = $response)")
        val operationStatus = gson.fromJson(response.body?.string(), OperationStatus::class.java)
        return "success" == operationStatus.status
    }


    @Throws(IndeterminateOperationException::class)
    private fun deleteFileSimple(basePath: String, fileName: String) {

        Log.d(TAG, "deleteFileSimple(basePath = $basePath, fileName = $fileName)")

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
                else -> throw unsuccessfulResponseException(response, "Fail deleting file '$path'")
            }
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    private fun getURLForUpload(targetFilePath: String, overwriteIfExists: Boolean): String {

        Log.d(TAG, "getURLForUpload(targetFilePath = $targetFilePath, overwriteIfExists = $overwriteIfExists)")

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
                throw unsuccessfulResponseException(response, "Fail getting url for upload for path '$targetFilePath' with overwriteIfExists='$overwriteIfExists'")
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    private fun putFileReal(file: File, uploadURL: String) {

        Log.d(TAG, "putFileReal(file = $file, uploadURL = $uploadURL)")

        val requestBody: RequestBody = file.asRequestBody(defaultMediaType)

        performUploadRequest(requestBody, uploadURL)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    private fun putStreamReal(
        inputStream: InputStream,
        uploadURL: String,
        writingCallback: ((Long) -> Unit)? = null,
        finishCallback: ((Long, Long) -> Unit)? = null,
    ) {
        Log.d(TAG, "putStreamReal(inputStream = $inputStream, uploadURL = $uploadURL, writingCallback = $writingCallback, finishCallback = $finishCallback)")

        val requestBody: RequestBody = object: RequestBody() {

            override fun contentType(): MediaType = defaultMediaType

            override fun writeTo(sink: BufferedSink) {
                copyBetweenStreamsWithCounting(
                    inputStream = inputStream,
                    outputStream = sink.outputStream(),
                    writingCallback = writingCallback,
                    finishCallback = finishCallback,
                )
            }
        }

        performUploadRequest(requestBody, uploadURL)
    }


    override fun copyFile(fromAbsolutePath: String, toAbsolutePath: String, overwriteIfExists: Boolean) {

        Log.d(TAG, "copyFile(fromAbsolutePath = $fromAbsolutePath, toAbsolutePath = $toAbsolutePath, overwriteIfExists = $overwriteIfExists)")

        val url = COPY_BASE_URL.toHttpUrl().newBuilder().apply {
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
                201 -> return
                202 -> throw IndeterminateOperationException(linkFromResponse(response))
                else -> throw unsuccessfulResponseException(response, "Fail copying file from '$fromAbsolutePath' to '$toAbsolutePath' with overwriting='$overwriteIfExists'")
            }
        }
    }


    private fun performUploadRequest(requestBody: RequestBody, uploadURL: String) {

        Log.d(TAG, "performUploadRequest(requestBody = $requestBody, uploadURL = $uploadURL)")

        val fileUploadRequest = Request.Builder()
            .put(requestBody)
            .url(uploadURL)
            .build()

        okHttpClient.newCall(fileUploadRequest).execute().use { response ->
            if (!response.isSuccessful) throw unsuccessfulResponseException(response, "Perform upload request failed.")
        }
    }


    private fun unsuccessfulResponseException(response: Response, extraText: String? = null): Throwable {
        return OperationUnsuccessfulException(
            response.code,
            extraText?.let { "${response.message}; $extraText" } ?: response.message,
        )
    }


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
        private const val COPY_BASE_URL = "$RESOURCES_BASE_URL/copy"

        private const val DEFAULT_MEDIA_TYPE = "application/octet-stream"
    }

    @Deprecated("Избавиться")
    class IndeterminateOperationException(val operationStatusLink: String) : Exception()

    inner class OperationStatus(val status: String)
}
