package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.BaseOfTests
import com.github.aakumykov.cloud_writer_2.utils.aggregateNamesToPath
import com.github.aakumykov.cloud_writer_2.utils.randomName
import com.github.aakumykov.utils.random
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream

abstract class CloudWriter2Tests : CloudWriter2TestsCommon() {


    @Before
    fun check_dirs_are_not_exists_before_test() {
        runTest {
            Assert.assertFalse(cloudWriter2.fileExists(dirPath, isRelative))
            Assert.assertFalse(cloudWriter2.fileExists(deepDirPath, isRelative))
        }
    }


    @After
    fun delete_dir_after_test() {
        runTest {
            cloudWriter2.apply {
                if (fileExists(dirPath, isRelative))
                    deleteFileOrEmptyDir(dirPath, isRelative)

                if (fileExists(deepDirPath, isRelative))
                    deleteFileOrEmptyDir(deepDirPath, isRelative)
            }
        }
    }


    //
    // -------------------------------------------------------------------------------------------
    //

    // TODO: file exists, dir exists
    @Test fun checks_file_exists() = run {
        step("Проверяю, что каталог '$dirPath' отсутствует") {
            runBlocking {
                Assert.assertFalse(cloudWriter2.fileExists(dirPath, isRelative))
            }
        }
        step("Создаю каталог '$dirPath") {
            runBlocking { cloudWriter2.createDir(dirPath, isRelative) }
        }
        step("Проверяю, что каталог '$dirPath' существует") {
            runBlocking {
                Assert.assertTrue(cloudWriter2.fileExists(dirPath, isRelative))
            }
        }
    }




    //
    // Удаление
    //
    @Test fun deletes_empty_dir() = run {
        step("Создаю каталог '$dirPath'") {
            runBlocking { createDir(dirPath, isRelative) }
        }
        step("Удаляю созданный каталог '$dirPath'") {
            runBlocking {
                Assert.assertEquals(
                    absoluteDirPath,
                    cloudWriter2.deleteFileOrEmptyDir(dirPath, isRelative)
                )
            }
        }
        step("Проверяю, что каталог '$dirPath' действительно удалён") {
            runBlocking {
                Assert.assertFalse(cloudWriter2.fileExists(dirPath, isRelative))
            }
        }
    }


    //
    // Загрузка файла.
    // План тестирования:
    //
    @Test fun puts_stream() = run {
        fileWithData.inputStream().use { inputStream: FileInputStream ->
            step("Отправляет поток в файл") {
                runBlocking {
                    cloudWriter2.putStream(
                        inputStream = inputStream,
                        targetPath = filePath,
                        isRelative = isRelative,
                        overwriteIfExists = true
                    )
                }
            }
        }
    }
}