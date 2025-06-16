package com.github.aakumykov.cloud_writer_2.inheritance_probe.yandex_disk

import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.inheritance_probe.BaseTest

abstract class YandexDiskTest : BaseTest()  {

    protected val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)

}