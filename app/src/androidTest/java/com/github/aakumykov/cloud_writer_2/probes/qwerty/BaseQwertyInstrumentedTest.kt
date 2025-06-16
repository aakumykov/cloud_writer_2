package com.github.aakumykov.cloud_writer_2.probes.qwerty

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert

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

