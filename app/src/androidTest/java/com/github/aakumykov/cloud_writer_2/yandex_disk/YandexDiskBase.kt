package com.github.aakumykov.cloud_writer_2.yandex_disk

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.CloudWriter2Tests
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import org.junit.Assert
import org.junit.Test

abstract class YandexDiskBase : CloudWriter2Tests()  {

    private val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)

    override val virtualRootPath: String
        get() = "/"

    override val cloudWriter2: CloudWriter2
        get() = YandexDiskCloudWriter2(
            authToken = yandexAuthToken,
            virtualRootPath = virtualRootPath
        )


    // FIXME: этот тест не запускается
    /*@Test
    fun yandex_auth_token_for_tests_is_not_empty(): Unit = run {
        step("Проверяю наличие токена авторизации в API Яндекс.Диск") {
            device.targetContext.resources.apply {
                yandexAuthToken.apply {
                    Assert.assertTrue(this.isNotEmpty())
                }
            }
        }
    }*/
}