package com.github.aakumykov.yandex_disk_cloud_writer

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class YandexDiskCloudWriterCreateDirSomeUnitTests : YandexDiskUnitTestBase("YANDEX_AUTH_ID_AT2024") {

    companion object {
        private const val ROOT_DIR = "/"
        private const val TEST_DIR_PATH = "tests_dir_10.2026"
    }

    private val yandexDiskCloudWriter by lazy { YandexDiskCloudWriter(yandexAuthId) }


    /*@Before
    fun prepare_tests_dir() {
        if (yandexDiskCloudWriter.fileExists(TEST_DIR_PATH)) {
            yandexDiskCloudWriter.deleteDirRecursively(ROOT_DIR, TEST_DIR_PATH)
            Assert.assertFalse(yandexDiskCloudWriter.fileExists(TEST_DIR_PATH))
        }
        Assert.assertTrue(testDir.mkdirs())
        Assert.assertTrue(testDir.exists())
    }*/


    /*@Test
    fun `виртуальный корневой каталог существует`() {
        Assert.assertTrue(testDir.exists())
    }*/


    @Test
    fun `создание уже существующаго каталога`() {
        val dirName = "dir1"

        val createdDirPath = yandexDiskCloudWriter.createDirIfNotExists(dirName)
        Assert.assertTrue(yandexDiskCloudWriter.fileExists(createdDirPath))

        Assert.assertThrows(Exception::class.java) {
            yandexDiskCloudWriter.createDir(dirName)
        }

        val dirPath2 = yandexDiskCloudWriter.createDirIfNotExists(dirName)
        Assert.assertTrue(yandexDiskCloudWriter.fileExists(dirPath2))
    }
}