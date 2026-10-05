package com.github.aakumykov.yandex_disk_cloud_writer

import android.util.Log
import com.github.aakumykov.copy_between_streams_with_speed.utils.MEGABYTES
import com.github.aakumykov.copy_between_streams_with_speed.utils.humanDecimalPlaces
import com.github.aakumykov.copy_between_streams_with_speed.utils.humanSizeBinary
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

class FileUploadingTimeoutTest {

    private val localPropertiesFilePath = "../local.properties"

    private val testRemoteFileName = "test.file"

    private val defaultReadTimeoutSec: Long = 60


    private val yandexAuthId: String by lazy {
        val localPropertiesFile = File(localPropertiesFilePath)
        val keyYandexAuthId = "YANDEX_AUTH_ID"
        Properties().apply {
            load(localPropertiesFile.inputStream())
        }.getProperty(keyYandexAuthId)
    }

    private fun okHttpClient(connectTimeoutSec: Long = 60,
                             writeTimeoutSec: Long = 120,
                             readTimeoutSec: Long = defaultReadTimeoutSec): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .build()
    }

    private val gson: Gson by lazy { Gson() }


    @Test
    fun yandex_auth_id_exists() {
        yandexAuthId.also {
            Assert.assertTrue(it.isNotEmpty())
        }
    }

    @Test
    fun test_getting_url_for_upload() {
        getUrlForUpload(testRemoteFileName).also {
            Assert.assertTrue(it.isNotEmpty())
        }
    }

    @Test
    fun simple_test() {
        val size = 12000.MEGABYTES
        val readTimeoutSec: Long = 180
        uploadDataToURL(
            size,
            getUrlForUpload(testRemoteFileName),
            readTimeoutSec
        ) { b,t,s ->
            Log.d(TAG, "Передано ${b.humanSizeBinary()}/${size.humanSizeBinary()} за ${t.humanDecimalPlaces} секунд со скоростью ${s.humanSizeBinary()}/с")
        }
    }


    @Test
    fun test_diff_read_timeouts_with_diff_file_sizes() {
        /*println("==== test_diff_read_timeouts_with_diff_file_sizes ====")

        test_range_of_sizes(6, 10, 1,
            1, 60, 10)

        test_range_of_sizes(10, 100, 10,
            10, 120, 20)

        test_range_of_sizes(100, 1000, 100,
            10, 120, 20)

        test_range_of_sizes(1000, 5000, 1000,
            30, 60, 10)

        test_range_of_sizes(1000, 5000, 1000,
            60, 120, 30)

        test_range_of_sizes(1000, 5000, 1000,
            130, 300, 50)

        test_range_of_sizes(3000, 3000, 1000,
            300, 300, 200) { b:Long, t:Long, s:Long ->
            Log.d(TAG, "bytes:${b.humanSizeBinary()}, " +
                    "time:${t.humanDecimalPlaces}, " +
                    "speed:${s.humanSizeBinary()}/с")
        }*/
    }


    private fun test_range_of_sizes(
        sizeRangeStart:Int, sizeRangeEnd: Int, sizeRangeStep: Int,
        timeoutRangeStart:Int, timeoutRangeEnd: Int, timeoutRangeStep: Int,
        finishCallback: ((transferredBytes:Long, timeElapsedMs:Long, speedBytesPerSec:Long) -> Unit)? = null,
    ) {
        for(dataSizeMb in sizeRangeStart..sizeRangeEnd step sizeRangeStep) {
            for(readTimeout in timeoutRangeStart..timeoutRangeEnd step timeoutRangeStep) {
                uploadDataToURL(
                    dataSizeMb.MEGABYTES.toLong(),
                    getUrlForUpload(testRemoteFileName),
                    readTimeout.toLong(),
                    finishCallback
                )
                TimeUnit.SECONDS.sleep(Random.nextLong(10, 120))
            }
        }
    }

    private fun getUrlForUpload(remoteFilePath: String): String {

        val url = "https://cloud-api.yandex.net/v1/disk/resources/upload?path=%2F${remoteFilePath}&overwrite=true"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", yandexAuthId)
            .build()

        okHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return response.body?.string().let {
                gson.fromJson(it, YandexUploadLinkData::class.java).href
            }
        }
    }

    private fun uploadDataToURL(
        uploadingDataSize: Long,
        uploadingURL: String,
        readTimeoutSec: Long = defaultReadTimeoutSec,
        finishCallback: ((transferredBytes:Long, timeElapsedMs:Long, speedBytesPerSec:Long) -> Unit)? = null,
        ) {
        val paramsMsg = "uploadDataToURL(size:${uploadingDataSize.humanSizeBinary()}, readTimeout:$readTimeoutSec)"
        println(paramsMsg)

        val bufferSize = min(uploadingDataSize, DEFAULT_BUFFER_SIZE.toLong()).toInt()
        var totalDataWritten: Long = 0

        var durationSec: Long = 0
        var speed: Long = 0

        val requestBody = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()

            override fun writeTo(sink: BufferedSink) {
                val timeStart = System.nanoTime()

                while(totalDataWritten < uploadingDataSize) {

                    val nextTotalWritten = totalDataWritten + bufferSize
                    val dataPieceSize = if (nextTotalWritten > uploadingDataSize) (nextTotalWritten - uploadingDataSize).toInt()
                                    else bufferSize
                    val nextData = Random.nextBytes(dataPieceSize)
                    sink.write(nextData, 0, dataPieceSize)
                    totalDataWritten += dataPieceSize
                }

                durationSec = ((System.nanoTime() - timeStart) / 1000_000_000.0).toLong()
                speed = (uploadingDataSize.toDouble() / durationSec).toLong()
            }
        }

        val request = Request.Builder()
            .url(uploadingURL)
            .put(requestBody)
            .build()

        val call = okHttpClient(readTimeoutSec = readTimeoutSec).newCall(request)

        try {
            call.execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
            }

            finishCallback?.invoke(uploadingDataSize, durationSec, speed)

        } catch (t: Throwable) {
            Log.e(TAG, paramsMsg, t)
        }
    }

    /*private fun getRemoteFileSize(filePath: String) {
        *//*
        https://cloud-api.yandex.net/v1/disk/resources?path=upload_test.file
         *//*
    }*/


    data class YandexUploadLinkData(
        val method: String,
        val href: String,
        val templated: Boolean,
        val operationId: String,
    )

    companion object {
        val TAG: String = FileUploadingTimeoutTest::class.java.simpleName
    }
}