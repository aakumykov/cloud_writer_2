package com.github.aakumykov.local_cloud_writer

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test

class LocalCloudWriterCopyFileInstrumentedTest : LocalCloudWriterInstrumentedTest() {

    // TODO: overwriteIfExists

    @Test
    fun when_copy_file_then_it_copied_with_its_content() {
        createSourceFile()
        localCloudWriter.copyFile(
            sourceFile.absolutePath,
            targetFile.absolutePath,
            overwriteIfExists = true
        )
        assertTrue(sourceFile.exists())
        assertTrue(targetFile.exists())
        assertEquals(sourceFileContents, targetFileContents)
    }

}