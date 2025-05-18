package com.github.aakumykov.local_cloud_writer

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test
import java.io.File

class LocalCloudWriterCreateDirInstrumentedTest : LocalCloudWriterInstrumentedTest() {

    // TODO: создание вложенных каталогов.

    @Test
    fun when_create_dir_then_it_created() {
        localCloudWriter.createDir(
            testDirParentPath,
            testDirName
        ).also { createdDirPath ->
            assertTrue(File(createdDirPath).exists())
            assertEquals(
                testDir.absolutePath,
                createdDirPath
            )
        }
    }

}