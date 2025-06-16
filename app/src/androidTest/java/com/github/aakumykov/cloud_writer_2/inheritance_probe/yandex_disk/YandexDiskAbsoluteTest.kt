package com.github.aakumykov.cloud_writer_2.inheritance_probe.yandex_disk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class YandexDiskAbsoluteTest : YandexDiskBase() {

    override val isRelative: Boolean = false

    override val cloudWriter2: CloudWriter2
        get() = YandexDiskCloudWriter2(
            authToken = yandexAuthToken,
            virtualRootPath = virtualRootPath
        )

    override val virtualRootPath: String
        get() = "/"

    override val absoluteDirPath: String
        get() = File(virtualRootPath, dirName).absolutePath

    override val creatingSimpleDirName: String
        get() = absoluteDirPath

    override val creatingDeepDirName: String
        get() = aggregateNamesToPath(dirName, randomName, randomName)
}