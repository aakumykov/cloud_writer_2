package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.File

class LocalCloudWriter2RelativePathTests : LocalCloudWriter2TestBase() {

    private val dirRelativePath: String = subtractBasePath(dirAbsolutePath)
    private val deepDirRelativePath = subtractBasePath(deepDir.absolutePath)


    private fun subtractBasePath(fromPath: String): String {
        return fromPath
            .replace(basePath,"")
            .replace(Regex("^/"),"")
    }

    @Test
    fun creates_dir() = run {
        step("Создаю относительный каталог '$dirRelativePath'") {
            runBlocking {
                localCloudWriter2.createDir(dirRelativePath, true)
            }
        }
        step("Проверяю, что каталог '${dirRelativePath}' создан как '$dirAbsolutePath'") {
            Assert.assertTrue(dir.exists())
        }
    }


    @Test
    fun returns_absolute_path_to_created_dir() = run {
        step("Возвращает путь к созданному каталогу") {
            runBlocking {
                Assert.assertEquals(
                    dirAbsolutePath,
                    localCloudWriter2.createDir(dirRelativePath, isRelative = true)
                )
            }
        }
    }


    @Test
    fun throws_exception_if_dir_exists() = run {
        creates_dir()
        step("Пробую создать каталог '$dirAbsolutePath' ещё раз"){
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    localCloudWriter2.createDir(dirRelativePath, true)
                }
            }
        }
    }


    @Test
    fun throws_exception_on_not_write_access() = run {

        val virtualRootPath = "/"
        val specialLocalCloudWriter = LocalCloudWriter2(virtualRootPath)

        val relativePath = dirName
        val testDir = File(virtualRootPath, relativePath)
        val testDirAbsolutePath = testDir.absolutePath

        step("Проверяю, что каталога '$testDirAbsolutePath', ВНЕЗАПНО, нет") {
            Assert.assertFalse(dir.exists())
        }
        step("Проверяю, что бросается исключение при попытке его создать") {
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    specialLocalCloudWriter.createDir(relativePath, true)
                }
            }
        }
    }


    @Test
    fun creates_dir_if_not_exists() = run {
        creates_dir()
        step("Не бросает исключения при повторном создании каталога условным методом") {
            runBlocking {
                Assert.assertEquals(
                    dirAbsolutePath,
                    localCloudWriter2.createDirIfNotExist(dirRelativePath, true)
                )
            }
        }
    }


    @Test
    fun throws_exception_on_creating_deep_dir_with_not_deep_method() = run {
        step("Проверяю, что каталога '$deepDirAbsolutePath' ещё нет") {
            Assert.assertFalse(deepDir.exists())
        }
        step("Проверяю, что бросает исключение при попытке создать"){
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    localCloudWriter2.createDir(deepDirRelativePath, false)
                }
            }
        }
    }


    @Test
    fun creates_deep_dir() = run {
        createDeepDirWithChecks(
            checkingDir = deepDir,
            resultCheckingDirPath = deepDirRelativePath,
        ) {
            localCloudWriter2.createDeepDir(deepDirRelativePath, true)
        }
    }


    @Test
    fun creates_deep_dir_if_not_exists() = run {
        creates_deep_dir()
        step("Повторное создание глубокого каталога '$deepDirRelativePath'") {
            localCloudWriter2.createDeepDirIfNotExists(deepDirRelativePath, true)
        }
    }


    /*@Test
    fun checking_file_exists() = run {

        val relativeFilePath = dirName // Создаётся файл с именем как у каталога.
        val file = File(basePath, relativeFilePath)

        step("Проверка существования файла (не каталога)") {
            step("Создание файла и проверка, что он создался силами объекта File") {
                Assert.assertTrue(file.createNewFile())
                Assert.assertTrue(file.exists())
                Assert.assertFalse(file.isDirectory)
            }
            step("Проверка, что файл существует силами библиотеки") {
                Assert.assertTrue(localCloudWriter2.fileExists(relativeFilePath, true))
            }
            step("Удаление файла и проверка, что удалился, силами объекта File") {
                Assert.assertTrue(file.delete())
                Assert.assertFalse(file.exists())
            }
            step("Проверка, что файла не существует, силами библиотеки") {
                Assert.assertFalse(localCloudWriter2.fileExists(relativeFilePath, true))
            }
        }
    }


    @Test
    fun checking_dir_exists() = run {

        val relativeFilePath = dirName
        val file = File(basePath, relativeFilePath)

        step("Проверка существования каталога") {
            step("Создание файла и проверка, что он создался силами объекта File") {
                Assert.assertTrue(dir.mkdir())
                Assert.assertTrue(dir.exists())
                Assert.assertTrue(dir.isDirectory)
            }
            step("Проверка, что файл существует силами библиотеки") {
                Assert.assertTrue(localCloudWriter2.fileExists(relativeFilePath, true))
            }
            step("Удаление файла и проверка, что удалился, силами объекта File") {
                Assert.assertTrue(dir.delete())
                Assert.assertFalse(dir.exists())
            }
            step("Проверка, что файла не существует, силами библиотеки") {
                Assert.assertFalse(localCloudWriter2.fileExists(relativeFilePath, true))
            }
        }
    }*/


    @Test
    fun checking_file_exists() = run {
        checkFileOrDirExists(
            checkedObjectCreatingBlock = { dir.createNewFile() },
            checkedObjectPreCheckingBlock = { dir.isFile },
            fileOrDirForPreCheck =  dir,
            pathToCheck = dirName,
            isRelative = true
        )
    }


    @Test
    fun checking_dir_exists() = run {
        checkFileOrDirExists(
            checkedObjectCreatingBlock = { dir.mkdir() },
            checkedObjectPreCheckingBlock = { dir.isDirectory },
            fileOrDirForPreCheck = dir,
            pathToCheck = dirName,
            isRelative = true
        )
    }
}