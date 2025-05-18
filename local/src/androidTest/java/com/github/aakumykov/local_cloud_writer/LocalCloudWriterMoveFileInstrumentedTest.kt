package com.github.aakumykov.local_cloud_writer

import org.junit.Assert
import org.junit.Test

class LocalCloudWriterMoveFileInstrumentedTest : LocalCloudWriterInstrumentedTest() {

    @Test
    fun move_file_with_overwrite_when_target_file_not_exists() {
        createSourceFile()
        localCloudWriter.moveFileOrEmptyDir(
            fromAbsolutePath = sourceFile.absolutePath,
            toAbsolutePath = targetFile.absolutePath,
            overwriteIfExists = true
        ).also {
            Assert.assertFalse(sourceFile.exists())
            Assert.assertTrue(targetFile.exists())
        }
    }


    @Test
    fun move_file_with_overwrite_when_target_file_exists() {
        createSourceFile()
        createTargetFile()
        localCloudWriter.moveFileOrEmptyDir(
            fromAbsolutePath = sourceFile.absolutePath,
            toAbsolutePath = targetFile.absolutePath,
            overwriteIfExists = true
        ).also {
            Assert.assertFalse(sourceFile.exists())
            Assert.assertTrue(targetFile.exists())
        }
    }


    @Test
    fun move_file_without_overwrite_when_target_file_not_exists() {
        createSourceFile()
        localCloudWriter.moveFileOrEmptyDir(
            fromAbsolutePath = sourceFile.absolutePath,
            toAbsolutePath = targetFile.absolutePath,
            overwriteIfExists = false
        ).also {
            Assert.assertFalse(sourceFile.exists())
            Assert.assertTrue(targetFile.exists())
        }
    }


    @Test
    fun move_file_without_overwrite_when_target_file_exists() {
        createSourceFile()
        createTargetFile()
        localCloudWriter.moveFileOrEmptyDir(
            fromAbsolutePath = sourceFile.absolutePath,
            toAbsolutePath = targetFile.absolutePath,
            overwriteIfExists = false
        ).also {
            Assert.assertTrue(sourceFile.exists())
            Assert.assertTrue(targetFile.exists())
            Assert.assertNotEquals(sourceFileContents, targetFileContents)
        }
    }

}