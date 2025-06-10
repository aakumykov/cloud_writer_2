package com.github.aakumykov.cloud_writer_2

import android.os.Environment
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

abstract class LocalCloudWriter2TestBase : TestCase() {

    protected val basePath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    protected val dirName: String = randomName

    protected val dir = File(basePath, dirName)
    protected val deepDir = File(File(File(dir, randomName), randomName), randomName)

    protected val dirAbsolutePath: String = File(basePath,dirName).absolutePath
    protected val deepDirAbsolutePath: String = deepDir.absolutePath

    protected val localCloudWriter2 by lazy { LocalCloudWriter2(basePath) }


    @Before
    fun check_dir_not_exists() {
        Assert.assertFalse(dir.exists())
    }

    @After
    fun deleting_dir() {
        dir.deleteRecursively()
    }

    @Test
    fun storage_writeability_check() = run {
        step("Создаю каталог '$dirAbsolutePath' не библиотечным методом") {
            Assert.assertTrue(dir.mkdir())
        }
        step("Проверяю, что он создался") {
            Assert.assertTrue(dir.exists())
        }
    }


    protected fun createDeepDirWithChecks(
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
    }


    protected fun checkFileOrDirExists(
        checkedObjectCreatingBlock: () -> Boolean,
        checkedObjectPreCheckingBlock: () -> Boolean,
        fileOrDirForPreCheck: File,
        pathToCheck: String,
        isRelative: Boolean
    ) = run {
        step("Проверка существования каталога") {
            step("Создание файла и проверка, что он создался силами объекта File") {
                Assert.assertTrue(checkedObjectCreatingBlock.invoke())
                Assert.assertTrue(fileOrDirForPreCheck.exists())
                Assert.assertTrue(checkedObjectPreCheckingBlock.invoke())
            }
            step("Проверка, что файл существует силами библиотеки") {
                runBlocking {
                    Assert.assertTrue(localCloudWriter2.fileExists(pathToCheck, isRelative))
                }
            }
            step("Удаление файла и проверка, что удалился, силами объекта File") {
                Assert.assertTrue(fileOrDirForPreCheck.delete())
                Assert.assertFalse(fileOrDirForPreCheck.exists())
            }
            step("Проверка, что файла не существует, силами библиотеки") {
                runBlocking {
                    Assert.assertFalse(localCloudWriter2.fileExists(pathToCheck, isRelative))
                }
            }
        }
    }
}