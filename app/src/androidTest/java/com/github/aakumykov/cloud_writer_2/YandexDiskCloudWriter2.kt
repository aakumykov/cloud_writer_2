package com.github.aakumykov.cloud_writer_2

import androidx.test.platform.app.InstrumentationRegistry
import com.github.aakumykov.extensions.errorMsg
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class YandexDiskCloudWriter2 : TestCase() {

    private val yandexCloudWriter by lazy {
        YandexDiskCloudWriter2(
            authToken = "y0__xDZlpzOBxjblgMgz_iYuxMNkWZCkFERHniMPYHOW2lqIGD79A",
        )
    }

    @Test
    fun yandex_auth_token_for_tests_is_not_empty() {
        InstrumentationRegistry.getInstrumentation().targetContext.resources.apply {
            getString(R.string.yandex_disk_auth_token_for_tests).apply {
                Assert.assertTrue(this.isNotEmpty())
                Assert.assertEquals(61, this.length)
            }
        }
    }

    @Test
    fun creates_dir() = run {
        val dirName = "randomName225"
        runBlocking {
            yandexCloudWriter
                .createDir(dirName, false)
                .also {
                    Assert.assertEquals(dirName, it)
                }
        }
    }

    @Test
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
    }
}