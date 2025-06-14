package com.github.aakumykov.cloud_writer_2.universal

import android.os.Environment
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class CloudWriter2UniversalInstrumentedTest() : StorageAccessTestCase() {
    protected abstract val cloudWriter2: CloudWriter2
    protected abstract val virtualRootPath: String
}


open class LocalCloudWriter2InstrumentedTest() : CloudWriter2UniversalInstrumentedTest() {

    override val cloudWriter2: CloudWriter2
        get() = LocalCloudWriter2(virtualRootPath)

    override val virtualRootPath: String
        get() = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .absolutePath

    @Test
    fun creates_relative_dir() = run {
        val dirName = randomName
        runTest {
            Assert.assertEquals(
                cloudWriter2.virtualRootPlus(dirName),
                cloudWriter2.createDir(dirName, true)
            )
        }
    }
}