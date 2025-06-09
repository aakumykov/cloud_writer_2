package com.github.aakumykov.yandex_disk_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
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

    private suspend fun createAbsoluteDir(path: String): String = suspendCancellableCoroutine { cc ->

        val url = urlBuilder
            .addQueryParameter(PARAM_PATH, path)
            .build()

        val request = Request.Builder()
            .url(url)
            .put("".toRequestBody(null))
            .build()

        val call = yandexDiskClient.newCall(request)

        try {
            call.execute().use { response: Response ->
                when(response.code) {
                    201 -> cc.resume(path)
                    else -> cc.resumeWithException(response.toCloudWriterException)
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

    override fun fileExists(path: String, isRelative: Boolean): Boolean {
        TODO("Not yet implemented")
    }

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

    private val urlBuilder by lazy {
        YANDEX_API_BASE.toHttpUrl().newBuilder()
    }

    companion object {
        private const val YANDEX_API_BASE = "https://cloud-api.yandex.net/v1/disk/resources"
        private const val PARAM_PATH = "path"
    }
}

val Response.toCloudWriterException: CloudWriterException get() {
    return CloudWriterException("${code}: $message")
}