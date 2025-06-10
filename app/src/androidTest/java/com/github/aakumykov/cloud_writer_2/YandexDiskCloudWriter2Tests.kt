package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class YandexDiskCloudWriter2Tests : TestCase() {

    private val yandexCloudWriter by lazy {
        YandexDiskCloudWriter2(authToken = yandexAuthToken)
    }


    private val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)


    @Test
    fun yandex_auth_token_for_tests_is_not_empty() {
        device.targetContext.resources.apply {
            yandexAuthToken.apply {
                Assert.assertTrue(this.isNotEmpty())
                Assert.assertEquals(61, this.length)
            }
        }
    }


    @Test
    fun creates_dir() = run {
        val dirName = randomName
        runBlocking {
            yandexCloudWriter
                .createDir(dirName, false)
                .also {
                    Assert.assertEquals(dirName, it)
                }
        }
    }


    @Test
    fun throws_exception_on_second_dir_creation_with_same_name() = run {

        val dirName = randomName

        suspend fun create_dir_with_name(name: String) {
            yandexCloudWriter
                .createDir(name, false)
                .also {
                    Assert.assertEquals(name, it)
                }
        }

        runBlocking {
            create_dir_with_name(dirName)
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    create_dir_with_name(dirName)
                }
            }
        }
    }


    // TODO: тестировать отмену создания каталога


    /*@Test
    fun create_dir_in_async_coroutine() {
        CoroutineScope(Dispatchers.IO).launch {
            val dirName = randomName
            try {
                yandexCloudWriter
                    .createDir(dirName, false)
                    .also {
                        Assert.assertEquals(dirName, it)
                    }
            } catch (e: Exception) {
                println(e.errorMsg)
            }
        }.also {
            runBlocking {
                val cancellationTimeoutMs: Long = 100
                delay(cancellationTimeoutMs)
                it.cancel(CancellationException("Прервано после $cancellationTimeoutMs мс"))
            }
        }
    }*/


}