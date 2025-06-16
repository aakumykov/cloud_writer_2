package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.randomName
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class CloudWriter2Base : Base() {

    protected abstract val cloudWriter2: CloudWriter2

    protected val dirName: String = randomName

    abstract val isRelative: Boolean
    protected abstract val virtualRootPath: String
    protected abstract val absoluteDirPath: String
    protected abstract val creatingSimpleDirName: String
    protected abstract val creatingDeepDirName: String

    /**
     *
     */
    abstract fun checkFileExistsNative(path: String, isRelative: Boolean): Boolean


    @Test
    fun returns_true_or_false_if_dir_exists_or_not() = run {

        step("") {
            runBlocking {
                Assert.assertFalse(checkFileExistsNative(creatingSimpleDirName, isRelative))
            }
        }

        creates_dir()

        step("") {
            runBlocking {
                Assert.assertTrue(checkFileExistsNative(creatingSimpleDirName, isRelative))
            }
        }
    }



    // TODO: проверять, что каталог существует
    @Test
    fun creates_dir() = run {
        step("Создаю каталог '$creatingSimpleDirName'") {
            runTest {
                val createdDirPath = cloudWriter2.createDir(creatingSimpleDirName, isRelative)
                step("Проверяю, что путь к нему соответствует '$absoluteDirPath'") {
                    Assert.assertEquals(absoluteDirPath, createdDirPath)
                }
                step("Проверяю, что каталог '$creatingSimpleDirName' существует") {
                    Assert.assertTrue(checkFileExistsNative(creatingSimpleDirName, isRelative))
                }
            }
        }
    }



/*
    @Test
    fun throws_exception_if_dir_exists() = run {
        creates_dir()

        step("Пробую создать каталог '$creatingSimpleDirName' ещё раз"){
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    cloudWriter2.createDir(creatingSimpleDirName, isRelative)
                }
            }
        }
    }
*/


    // TODO: как проверять это?
    /*@Test
    fun throws_exception_on_not_write_access() = run {

        val dir = File("/","123")
        val path = dir.absolutePath

        step("Проверяю, что каталога '$path', ВНЕЗАПНО, нет") {
            Assert.assertFalse(dir.exists())
        }

        step("Проверяю, что бросается исключение при попытке его создать") {
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    cloudWriter2.createDir(path, false)
                }
            }
        }
    }*/


/*
    @Test
    fun creates_dir_if_not_exists() = run {
        creates_dir()

        step("Не бросает исключения при повторном создании каталога '$creatingSimpleDirName' условным методом") {
            runBlocking {
                Assert.assertEquals(
                    creatingSimpleDirName,
                    cloudWriter2.createDirIfNotExist(creatingSimpleDirName, isRelative)
                )
            }
        }
    }
*/


    /*@Test
    fun throws_exception_on_creating_deep_dir_with_not_deep_method() = run {

        val deepDirName = File(randomName, randomName).absolutePath
        val deepDir = File(virtualRootPath, deepDirName)
        val deepDirPath = deepDir.absolutePath

        step("Проверяю, что каталога '$deepDirPath' ещё нет") {
            Assert.assertFalse(deepDir.exists())
        }
        step("Проверяю, что бросает исключение при попытке создать"){
            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    cloudWriter2.createDir(deepDirPath, isRelative)
                }
            }
        }
    }*/


    /*@Test
    fun creates_deep_dir() = run {
        step("Проверяю, что глубокого каталога '$creatingDeepDirName' ещё нет") {

        }
    }*/


    /*private fun createDeepDirWithChecks(
        checkingDir: File,
        resultCheckingDirPath: String,
        dirCreationBlock: () -> String,
    ) = run {
        step("Проверяю, что глубокого каталога '${checkingDir.absolutePath}' ещё нет") {
            Assert.assertFalse(checkingDir.exists())
        }
        step("Создаю глубокий каталог '${checkingDir.absolutePath}'") {
            val createdDirPath = dirCreationBlock.invoke()

            step("Проверяю, что глубокий каталог '${checkingDir.absolutePath}' создан") {
                Assert.assertTrue(checkingDir.exists())
            }
            step("Проверяю, что возвращённый путь соответствует заданному исходному '${checkingDir.absolutePath}'") {
                Assert.assertEquals(
                    resultCheckingDirPath,
                    createdDirPath
                )
            }
        }
    }*/
}