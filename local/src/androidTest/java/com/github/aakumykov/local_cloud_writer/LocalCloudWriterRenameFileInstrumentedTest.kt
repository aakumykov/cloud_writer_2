package com.github.aakumykov.local_cloud_writer

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test

class LocalCloudWriterRenameFileInstrumentedTest : LocalCloudWriterInstrumentedTest() {

    // TODO: overwriteIfExists

    @Test
    fun when_rename_file_then_it_renamed() {
        createSourceFile()
        val fileContents = sourceFileContents
        localCloudWriter.renameFileOrEmptyDir(
            fromAbsolutePath = sourceFile.absolutePath,
            toAbsolutePath = targetFile.absolutePath,
            overwriteIfExists = true
        )
        assertFalse(sourceFile.exists())
        assertTrue(targetFile.exists())
        assertEquals(fileContents, targetFileContents)
    }

}