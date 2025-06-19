package com.github.aakumykov.cloud_writer_2.local

import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.cloud_writer_2.inheritance_probe.utils.randomName
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.File

/*
LocalCloudWriter2 имеет два варианта запуска методов:
1) с относительным путём;
2) с абсолютным путём.
"Относительные" методы используют внутри себя "абсолютные", поэтому вторые
тестирую полностью, а первые только в том, в чём они отличаются.

createDir()
createDirIfNotExists()
createDeepDir()
createDeepDirIfNotExists()
 */
class LocalCloudWriter2AbsolutePathTests : LocalCloudWriter2TestBaseOfTests() {

    // TODO: корректная проверка возвращаемого пути

    @Test
    fun creates_dir() = run {
        step("Создаю каталог") {
            runBlocking {
                localCloudWriter2.createDir(dirAbsolutePath, false)
            }
        }
        step("Проверяю, что он существует") {
            Assert.assertTrue(dir.exists())
        }
    }


    @Test
    fun returns_path_to_created_dir() = run {
        step("Возвращает путь к созданному каталогу") {
            runBlocking {
                Assert.assertEquals(
                    dirAbsolutePath,
                    localCloudWriter2.createDir(dirAbsolutePath, isRelative = false)
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
                    localCloudWriter2.createDir(dirAbsolutePath, false)
                }
            }
        }
    }


    @Test
    fun throws_exception_on_not_write_access() = run {

        val dir = File("/","123")
        val path = dir.absolutePath

        step("Проверяю, что каталога '$path', ВНЕЗАПНО, нет") {
            Assert.assertFalse(dir.exists())
        }
        step("Проверяю, что бросается исключение при попытке его создать") {
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    localCloudWriter2.createDir(path, false)
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
                    localCloudWriter2.createDirIfNotExist(dirAbsolutePath, false)
                )
            }
        }
    }


    @Test
    fun throws_exception_on_creating_deep_dir_with_not_deep_method() = run {

        val deepDirName = File(randomName, randomName).absolutePath
        val deepDir = File(basePath,deepDirName)
        val deepDirPath = deepDir.absolutePath

        step("Проверяю, что каталога '$deepDirPath' ещё нет") {
            Assert.assertFalse(deepDir.exists())
        }
        step("Проверяю, что бросает исключение при попытке создать"){
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    localCloudWriter2.createDir(deepDirPath, false)
                }
            }
        }
    }


    @Test
    fun creates_deep_dir() = run {
        createDeepDirWithChecks(
            checkingDir = deepDir,
            resultCheckingDirPath = deepDirAbsolutePath
        ) {
            runBlocking {
                localCloudWriter2.createDeepDir(deepDirAbsolutePath, false)
            }
        }
    }

    @Test
    fun creates_deep_dir_if_not_exists() = run {
        createDeepDirWithChecks(
            checkingDir = deepDir,
            resultCheckingDirPath = deepDirAbsolutePath
        ) {
            runBlocking {
                localCloudWriter2.createDeepDirIfNotExists(deepDirAbsolutePath, false)
            }
        }

        step("Повторное создание каталога '$deepDirAbsolutePath'") {
            runBlocking {
                localCloudWriter2.createDeepDirIfNotExists(deepDirAbsolutePath, false)
            }
        }
    }


    @Test
    fun checking_file_exists() = run {
        checkFileOrDirExists(
            checkedObjectCreatingBlock = { dir.createNewFile() },
            checkedObjectPreCheckingBlock = { dir.isFile },
            fileOrDirForPreCheck =  dir,
            pathToCheck = dirAbsolutePath,
            isRelative = false
        )
    }


    @Test
    fun checking_dir_exists() = run {
        checkFileOrDirExists(
            checkedObjectCreatingBlock = { dir.mkdir() },
            checkedObjectPreCheckingBlock = { dir.isDirectory },
            fileOrDirForPreCheck =  dir,
            pathToCheck = dirAbsolutePath,
            isRelative = false
        )
    }
}