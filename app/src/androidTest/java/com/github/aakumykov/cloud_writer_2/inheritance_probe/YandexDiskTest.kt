package com.github.aakumykov.cloud_writer_2.inheritance_probe

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class YandexDiskTest : BaseTest() {

    override val virtualRootPath: String
        get() = "/"

    override val absoluteDirPath: String
        get() = File(virtualRootPath, dirName).absolutePath

    private val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)

    override val relativeDirPath: String
        get() = dirName

    override val cloudWriter2: CloudWriter2
        get() = YandexDiskCloudWriter2(
            authToken = yandexAuthToken,
            virtualRootPath = virtualRootPath
        )
}