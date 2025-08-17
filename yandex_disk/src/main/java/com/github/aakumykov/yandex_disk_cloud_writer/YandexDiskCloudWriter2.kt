package com.github.aakumykov.yandex_disk_cloud_writer

import android.util.Log
import com.github.aakumykov.cloud_writer.BasicCloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.copy_between_streams_with_counting.copyBetweenStreamsWithCounting
import com.github.aakumykov.yandex_disk_cloud_writer.ext.toCloudWriterException
import com.google.gson.Gson
import com.yandex.disk.rest.json.Link
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class YandexDiskCloudWriter2(
    private val authToken: String,
    private val yandexDiskClientCreator: YandexDiskOkHttpClientCreator,
    override val virtualRootPath: String = "/",
    private val gson: Gson = Gson()
)
    : BasicCloudWriter2()
{
    private val yandexDiskClient: OkHttpClient by lazy {
        yandexDiskClientCreator.create(authToken)
    }

    override suspend fun createDir(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createAbsoluteDir(virtualRootPlus(dirPath))
        else createAbsoluteDir(dirPath)
    }

    private suspend fun createAbsoluteDir(path: String): String = suspendCancellableCoroutine { cc ->

        val url = apiURL(queryPairs = arrayOf(
            PARAM_PATH to path
        ))

        val request = apiRequest(url) {
            put(EMPTY_REQUEST_BODY)
        }

        val call = yandexDiskClient.newCall(request)


    }


    override suspend fun createDirIfNotExist(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createDirIfNotExistAbsolute(virtualRootPlus(dirPath))
        else createDirIfNotExistAbsolute(dirPath)
    }

    private suspend fun createDirIfNotExistAbsolute(path: String): String {
        return if (!fileExistsAbsolute(path)) createAbsoluteDir(path)
        else path
    }


    override suspend fun createDeepDir(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createDeepDirAbsolute(virtualRootPlus(dirPath))
        else createDeepDirAbsolute(dirPath)
    }


    // TODO: унифицировать с [LocalCloudWriter2.createDeepDir]
    private suspend fun createDeepDirAbsolute(path: String): String {

        // Так как идёт пошаговое создание
        // каталогов из пути вглубь,
        // то нужно отрезать от него "системную"
        // незаписываемую часть.

        val pathToOperate = path.replace(Regex("^${virtualRootPath}/+"),"")

        return iterateOverDirsInPathFromRoot(pathToOperate) { partialPath ->
            createDirIfNotExist(partialPath, true)
        }.let {
            path
        }
    }


    override suspend fun createDeepDirIfNotExists(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createDeepDirIfNotExistAbsolute(virtualRootPlus(dirPath))
        else createDeepDirIfNotExistAbsolute(dirPath)
    }

    override suspend fun deleteFileOrEmptyDir(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) deleteEmptyDirAbsolute(virtualRootPlus(dirPath))
        else deleteEmptyDirAbsolute(dirPath)
    }

    private suspend fun deleteEmptyDirAbsolute(absolutePath: String): String = suspendCancellableCoroutine{ cc ->

        val url = apiURL(
            PARAM_PATH to absolutePath,
            PARAM_PERMANENTLY to VALUE_TRUE
        )

        val request = apiRequest(url) {
            delete()
        }

        val call = yandexDiskClient.newCall(request)

        executeCall(call, cc) { response ->
            when(response.code) {
                204 -> {
                    cc.resume(absolutePath)
                }
                else -> {
                    throw response.toCloudWriterException
                }
            }
        }
    }

    private fun <T> executeCall(
        call: Call,
        cancellableContinuation: CancellableContinuation<T>,
        responseProcessingBlock: (response: Response) -> Unit
    ) {
        try {
            call.execute().use { response: Response ->
                responseProcessingBlock.invoke(response)
            }
        } catch (e: CancellationException) {
            call.cancel()
        } catch (e: Exception) {
            cancellableContinuation.resumeWithException(e)
        }
    }

    private suspend fun createDeepDirIfNotExistAbsolute(path: String): String {
        return if (fileExists(path, false)) path
        else createDeepDirAbsolute(path)
    }


    override suspend fun fileExists(dirPath: String, isRelative: Boolean): Boolean {
        return if (isRelative) fileExistsAbsolute(virtualRootPlus(dirPath))
        else fileExistsAbsolute(dirPath)
    }

    private suspend fun fileExistsAbsolute(path: String): Boolean = suspendCancellableCoroutine { cc ->

        val url = pathApiURL(path)

        val request = apiRequest(url) {}

        val call = yandexDiskClient.newCall(request)

        executeCall(call, cc) { response ->
            when(response.code) {
                200 -> cc.resume(true)
                404 -> cc.resume(false)
                else -> throw response.toCloudWriterException
            }
        }
    }



    private fun apiURL(vararg queryPairs: Pair<String, String>): HttpUrl {
        return apiURL(url = YANDEX_API_BASE, queryPairs = queryPairs)
    }


    private fun apiURL(
        url: String,
        vararg queryPairs: Pair<String, String>
    ): HttpUrl {
        return url.toHttpUrl().newBuilder().apply {
            for ((key,value) in queryPairs) {
                addQueryParameter(key, value)
            }
        }.build()
    }


    private fun pathApiURL(path: String): HttpUrl = apiURL(PARAM_PATH to path)


    private inline fun apiRequest(
        url: HttpUrl,
        requestMethodBlock: Request.Builder.() -> Unit
    ): Request {
        return Request.Builder()
            .url(url)
            .apply {
                requestMethodBlock.invoke(this)
            }.build()
    }


    @Throws(IOException::class, CloudWriterException::class)
    private suspend fun getURLForUpload(targetFilePath: String, overwriteIfExists: Boolean): String = suspendCancellableCoroutine{ cc ->

        val url = apiURL(UPLOAD_BASE_URL,
            PARAM_PATH to targetFilePath,
            PARAM_OVERWRITE to overwriteIfExists.toString()
        )

        val request = apiRequest(url) {}

        val call = yandexDiskClient.newCall(request)

        executeCall(call, cc) { response ->
            when(response.code) {
                200 -> cc.resume(linkFromResponse(response))
                else -> throw response.toCloudWriterException
            }
        }
    }


    @Throws(IOException::class, CloudWriterException::class)
    override suspend fun putStream(
        inputStream: InputStream,
        targetPath: String,
        isRelative: Boolean,
        overwriteIfExists: Boolean,
        readingCallback: ((Long) -> Unit)?,
        writingCallback: ((Long) -> Unit)?,
        finishCallback: ((Long, Long) -> Unit)?
    ) {
        val path = if (isRelative) virtualRootPlus(targetPath) else targetPath

        val uploadURL = getURLForUpload(path, overwriteIfExists)

        return suspendCancellableCoroutine { cc ->

            val requestBody: RequestBody = object: RequestBody() {

                override fun contentType(): MediaType = DEFAULT_MEDIA_TYPE

                override fun writeTo(sink: BufferedSink) {

                    // TODO: вызывать здесь или в copyBetweenStreamsWithCounting
                    //  ошибку и смотреть, что будет.

                    copyBetweenStreamsWithCounting(
                        inputStream = inputStream,
                        outputStream = sink.outputStream(),
                        readingCallback = readingCallback,
                        writingCallback = writingCallback,
                        finishCallback = finishCallback,
                    )
                }
            }

            val url = apiURL(uploadURL)

            val request = apiRequest(url) { put(requestBody) }

            val call = yandexDiskClient.newCall(request)

            cc.invokeOnCancellation { cause ->
                Log.i(TAG, cause?.message ?: "cc.invokeOnCancellation")
                call.cancel()
            }

            executeCall(call, cc) { response: Response ->
                when(response.code) {
                    201 -> {
                        cc.resume(Unit)
                    }
                    else -> throw response.toCloudWriterException
                }
            }
        }
    }


    override suspend fun renameFileOrEmptyDir(
        fromPath: String,
        toPath: String,
        isRelative: Boolean,
        overwriteIfExists: Boolean
    ): Boolean = suspendCancellableCoroutine { cc ->

        val realFromPath = if (isRelative) virtualRootPlus(fromPath) else fromPath
        val realToPath = if (isRelative) virtualRootPlus(toPath) else toPath

        val url = apiURL(MOVE_BASE_URL,
            PARAM_FROM to realFromPath,
            PARAM_PATH to realToPath,
            PARAM_OVERWRITE to overwriteIfExists.toString(),
            PARAM_FORCE_ASYNC to VALUE_FALSE
        )

        val request = apiRequest(url) {
            post(EMPTY_REQUEST_BODY)
        }

        val call = yandexDiskClient.newCall(request)

        try {
            call.execute().use { response: Response ->
                when(response.code) {
                    201 -> cc.resume(true)
                    else -> throw response.toCloudWriterException
                }
            }
        } catch (e: CancellationException) {
            //FIXME: здесь нужно возвращать false?
            call.cancel()
        } catch (e: Exception) {
            cc.resumeWithException(e)
        }
    }

    private fun linkFromResponse(response: Response): String {
        return gson.fromJson(response.body?.string(), Link::class.java).href
    }



    companion object {
        val TAG: String = YandexDiskCloudWriter2::class.java.simpleName

        private const val YANDEX_API_BASE = "https://cloud-api.yandex.net/v1/disk/resources"
        private const val UPLOAD_BASE_URL = "${YANDEX_API_BASE}/upload"
        private const val MOVE_BASE_URL = "${YANDEX_API_BASE}/move"

        private const val PARAM_PATH = "path"
        private const val PARAM_FROM = "from"
        private const val PARAM_FORCE_ASYNC = "force_async"
        private const val PARAM_OVERWRITE = "overwrite"
        private const val PARAM_PERMANENTLY = "permanently"

        private const val VALUE_TRUE = "true"
        private const val VALUE_FALSE = "false"

        private val DEFAULT_MEDIA_TYPE: MediaType = "application/octet-stream".toMediaType()
        private val EMPTY_REQUEST_BODY by lazy { "".toRequestBody(null) }
    }
}