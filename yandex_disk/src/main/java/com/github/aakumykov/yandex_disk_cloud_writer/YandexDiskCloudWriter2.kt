package com.github.aakumykov.yandex_disk_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.yandex_disk_cloud_writer.ext.toCloudWriterException
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
        return if (isRelative) createAbsoluteDir(virtualRootPlus(path))
        else createAbsoluteDir(path)
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


    override suspend fun createDirIfNotExist(path: String, isRelative: Boolean): String {
        return if (isRelative) createDirIfNotExistAbsolute(virtualRootPlus(path))
        else createDirIfNotExistAbsolute(path)
    }

    private suspend fun createDirIfNotExistAbsolute(path: String): String {
        return if (!fileExistsAbsolute(path)) createAbsoluteDir(path)
        else path
    }


    override fun createDeepDir(path: String, isRelative: Boolean): String {
        return if (isRelative) createDeepDirAbsolute(virtualRootPlus(path))
        else createDeepDirAbsolute(path)
    }

    private fun createDeepDirAbsolute(path: String): String {
        TODO("Not yet implemented")
    }


    override fun createDeepDirIfNotExists(path: String, isRelative: Boolean): String {
        return if (isRelative) createDeepDirIfNotExistAbsolute(virtualRootPlus(path))
        else createDeepDirIfNotExistAbsolute(path)
    }

    private fun createDeepDirIfNotExistAbsolute(path: String): String {
        TODO("Not yet implemented")
    }


    override suspend fun fileExists(path: String, isRelative: Boolean): Boolean {
        return if (isRelative) fileExistsAbsolute(virtualRootPlus(path))
        else fileExistsAbsolute(path)
    }

    private suspend fun fileExistsAbsolute(path: String): Boolean = suspendCancellableCoroutine { cc ->

        val url = pathApiURL(path)

        val request = apiRequest(url) {}

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



    private fun apiURL(vararg queryPairs: Pair<String, String>): HttpUrl {
        return YANDEX_API_BASE.toHttpUrl().newBuilder().apply {
            for ((key,value) in queryPairs) {
                addQueryParameter(key, value)
            }
        }.build()
    }


    private fun pathApiURL(path: String): HttpUrl = apiURL(PARAM_PATH to path)


    private inline fun apiRequest(url: HttpUrl, requestMethodBlock: Request.Builder.() -> Unit): Request {
        return Request.Builder()
            .url(url)
            .apply {
                requestMethodBlock.invoke(this)
            }.build()
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


    companion object {
        private const val YANDEX_API_BASE = "https://cloud-api.yandex.net/v1/disk/resources"
        private const val PARAM_PATH = "path"
    }
}