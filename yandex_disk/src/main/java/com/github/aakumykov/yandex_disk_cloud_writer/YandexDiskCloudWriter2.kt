package com.github.aakumykov.yandex_disk_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class YandexDiskCloudWriter2(
    private val authToken: String,
    override val virtualRootPath: String = "/",
)
    : CloudWriter2
{
    override suspend fun createDir(path: String, isRelative: Boolean): String {
        return if (isRelative) createRelativeDir(path)
        else createAbsoluteDir(path)
    }


    private suspend fun createRelativeDir(path: String): String
        = createAbsoluteDir(virtualRootPlus(path))


    private fun apiURL(vararg queryPairs: Pair<String, String>): HttpUrl {
        return YANDEX_API_BASE.toHttpUrl().newBuilder().apply {
            for ((key,value) in queryPairs) {
                addQueryParameter(key, value)
            }
        }.build()
    }

    private inline fun apiRequest(url: HttpUrl, requestMethodBlock: Request.Builder.() -> Unit): Request {
        return Request.Builder()
            .url(url)
            .apply {
                requestMethodBlock.invoke(this)
            }.build()
    }

    private suspend fun createAbsoluteDir(path: String): String = suspendCancellableCoroutine { cc ->

        val url = apiURL(PARAM_PATH to path)

        val request = apiRequest(url) {
            put("".toRequestBody(null))
        }

        val call = yandexDiskClient.newCall(request)

        try {
            call.execute().use { response: Response ->
                when(response.code) {
                    201 -> {
                        cc.resume(path)
                    }
                    else -> {
                        throw response.toCloudWriterException
                    }
                }
            }
        } catch (e: CancellationException) {
            call.cancel()
        } catch (e: Exception) {
            cc.resumeWithException(e)
        }
    }


    override fun createDirIfNotExist(path: String, isRelative: Boolean): String {
        TODO("Not yet implemented")
    }

    override fun createDeepDir(path: String, isRelative: Boolean): String {
        TODO("Not yet implemented")
    }

    override fun createDeepDirIfNotExists(path: String, isRelative: Boolean): String {
        TODO("Not yet implemented")
    }

    override suspend fun fileExists(path: String, isRelative: Boolean): Boolean {
        return if (isRelative) fileExistsRelative(path)
        else fileExistsAbsolute(path)
    }

    private suspend fun fileExistsRelative(path: String): Boolean {
        return fileExistsAbsolute(virtualRootPlus(path))
    }

    private suspend fun fileExistsAbsolute(path: String): Boolean = suspendCancellableCoroutine { cc ->

        val url = pathApiURL(path)

        val request = apiRequest(url) { put("".toRequestBody(null)) }

        val call = yandexDiskClient.newCall(request)

        try {
            call.execute().use { response: Response ->
                when(response.code) {
                    200 -> cc.resume(true)
                    404 -> cc.resume(false)
                    else -> throw response.toCloudWriterException
                }
            }
        } catch (e: CancellationException) {
            call.cancel()
        } catch (e: Exception) {
            cc.resumeWithException(e)
        }
    }

    /*@Throws(Exception::class, CloudWriterException::class)
    private suspend fun <T> tryExecuteCall(call: Call): T = suspendCancellableCoroutine { cc ->
        try {
            call.execute().use { response: Response ->
                when(response.code) {
                    200 -> cc.resume(true)
                    else -> throw response.toCloudWriterException
                }
            }
        } catch (e: CancellationException) {
            call.cancel()
        } catch (e: Exception) {
            cc.resumeWithException(e)
        }
    }*/

    private fun pathApiURL(path: String): HttpUrl = apiURL(PARAM_PATH to path)

    private val yandexDiskClient: OkHttpClient by lazy {
        OkHttpClient
            .Builder()
            .addNetworkInterceptor { chain: Interceptor.Chain ->
                val builder = chain.request().newBuilder()
                    .header("Authorization", authToken)
                chain.proceed(builder.build())
            }
            .build()
    }

    /*private fun urlBuilder(map: Map<String, String>): HttpUrl.Builder {

    }*/

    companion object {
        private const val YANDEX_API_BASE = "https://cloud-api.yandex.net/v1/disk/resources"
        private const val PARAM_PATH = "path"
    }
}

val Response.toCloudWriterException: CloudWriterException get() {
    return CloudWriterException("${code}: $message")
}