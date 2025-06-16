package com.github.aakumykov.cloud_writer_2.inheritance_probe.local

import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.cloud_writer_2.inheritance_probe.BaseTest
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class LocalRelativeTest : BaseTest() {

    override val isRelative: Boolean = true

    override val virtualRootPath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    override val cloudWriter2: CloudWriter2
        get() = LocalCloudWriter2(virtualRootPath)

    override val absoluteDirPath: String
        get() = File(virtualRootPath, dirName).absolutePath

    override val creatingSimpleDirName: String
        get() = dirName

    override val creatingDeepDirName: String = aggregateNamesToPath(randomName, randomName, randomName)
}
