package com.github.aakumykov.local_cloud_writer

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class LocalCloudWriterUnitTest {

    companion object {
        private const val ROOT_DIR_PATH = "tests_dir"
        private val rootDir = File(ROOT_DIR_PATH)
    }

    @Before
    fun prepare_tests_dir() {
        if (rootDir.exists()) {
            rootDir.deleteRecursively()
            Assert.assertFalse(rootDir.exists())
        }
        Assert.assertTrue(rootDir.mkdirs())
        Assert.assertTrue(rootDir.exists())
    }

    private val localCloudWriter by lazy { LocalCloudWriter(
        ROOT_DIR_PATH
    ) }

    @Test
    fun `виртуальный корневой каталог существует`() {
        Assert.assertTrue(rootDir.exists())
    }

    @Test
    fun `создание уже существующаго каталога`() {
        val dirName = "dir1"

        val createdDirPath = localCloudWriter.createDir(dirName)
        Assert.assertTrue(File(createdDirPath).exists())

        Assert.assertThrows(Exception::class.java) {
            localCloudWriter.createDir(dirName)
        }

        val dirPath2 = localCloudWriter.createDirIfNotExists(dirName)
        Assert.assertTrue(File(dirPath2).exists())
    }
}