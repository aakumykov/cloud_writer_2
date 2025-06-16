package com.github.aakumykov.cloud_writer_2.probes.qwerty

import android.os.Environment
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import kotlinx.coroutines.test.runTest
import org.junit.Test

//@RunWith(AndroidJUnit4::class)
class QwertyTest : BaseQwertyInstrumentedTest() {

    override val dirName: String = randomName

    override val virtualRootPath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath

    override val cloudWriter2: CloudWriter2 = LocalCloudWriter2(virtualRootPath)

    override val absoluteDirName: String = cloudWriter2.virtualRootPlus(dirName)


    @Test
    fun test_create_relative_dir() = runTest { create_relative_dir() }

    @Test
    fun test_create_absolute_dir() = runTest { create_absolute_dir() }
}