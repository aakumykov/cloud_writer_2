package com.github.aakumykov.cloud_writer_2

import android.os.Environment
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class BaseQwertyInstrumentedTest : StorageAccessTestCase() {

    abstract val dirName: String
    abstract val absoluteDirName: String
    /*protected*/ abstract val virtualRootPath: String
    /*protected*/ abstract val cloudWriter2: CloudWriter2

    fun create_relative_dir() = run {
        runBlocking {
            Assert.assertEquals(
                cloudWriter2.virtualRootPlus(dirName),
                cloudWriter2.createDir(dirName, true)
            )
        }
    }

    fun create_absolute_dir() = run {
        runBlocking {
            Assert.assertEquals(
                absoluteDirName,
                cloudWriter2.createDir(dirName, false)
            )
        }
    }
}

//@RunWith(AndroidJUnit4::class)
class QwertyTest : BaseQwertyInstrumentedTest() {

    override val dirName: String = randomName

    override val virtualRootPath: String = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath

    override val cloudWriter2: CloudWriter2 = LocalCloudWriter2(virtualRootPath)

    override val absoluteDirName: String = cloudWriter2.virtualRootPlus(dirName)


    @Test
    fun test_create_relative_dir() = runTest { create_relative_dir() }

    @Test
    fun test_create_absolute_dir() = runTest { create_absolute_dir() }
}