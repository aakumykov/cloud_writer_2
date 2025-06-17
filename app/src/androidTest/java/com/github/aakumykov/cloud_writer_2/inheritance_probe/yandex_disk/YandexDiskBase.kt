package com.github.aakumykov.cloud_writer_2.inheritance_probe.yandex_disk

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.inheritance_probe.CloudWriter2Tests
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2

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
}