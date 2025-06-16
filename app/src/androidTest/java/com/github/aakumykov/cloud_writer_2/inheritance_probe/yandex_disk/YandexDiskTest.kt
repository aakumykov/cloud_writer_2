package com.github.aakumykov.cloud_writer_2.inheritance_probe.yandex_disk

import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.inheritance_probe.CloudWriter2Base

abstract class YandexDiskTest : CloudWriter2Base()  {

    protected val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)

}