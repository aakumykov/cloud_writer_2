package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.inheritance_probe.common.randomName
import com.github.aakumykov.cloud_writer_2.inheritance_probe.common.BaseOfTests
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

abstract class CloudWriter2Tests : BaseOfTests() {

    protected abstract val cloudWriter2: CloudWriter2
    protected abstract val isRelative: Boolean

    protected abstract val virtualRootPath: String

    protected val dirName: String = randomName
    protected val deepDirName: String = aggregateNamesToPath(randomName, randomName, randomName)

    protected abstract val dirPath: String
    protected abstract val deepDirPath: String

    protected val absoluteDirPath: String get() = cloudWriter2.virtualRootPlus(dirName)
    protected val deepDirAbsolutePath get() = aggregateNamesToPath(virtualRootPath, deepDirName)

    protected val dirRelativePath: String = absolutePathMinusVirtualRoot(absoluteDirPath)
    protected val deepDirRelativePath = absolutePathMinusVirtualRoot(deepDirAbsolutePath)


    private fun absolutePathMinusVirtualRoot(absolutePath: String): String
            = absolutePath.replace(Regex("^$virtualRootPath"),"")


    private suspend fun createDir(dirPath: String, isRelative: Boolean, action: (suspend (String) -> Unit)? = null) {
        cloudWriter2.createDir(dirPath, isRelative).also { createdDirPath ->
            action?.invoke(createdDirPath)
        }
    }


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
                    deleteEmptyDir(dirPath, isRelative)

                if (fileExists(deepDirPath, isRelative))
                    deleteEmptyDir(deepDirPath, isRelative)
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


    @Test fun creates_dir() = run {
        step("Создаю каталог '${dirPath}'") {
            runTest {
                createDir(dirPath, isRelative) { createdDirPath ->
                    step("Проверяю, что путь к нему соответствует '$absoluteDirPath'") {
                        Assert.assertEquals(absoluteDirPath, createdDirPath)
                    }
                }
            }
        }
    }


    @Test
    open fun creates_deep_dir() = run {
        step("Создаю глубокий каталог '$deepDirPath'") {
            runBlocking {
                Assert.assertEquals(
                    deepDirAbsolutePath,
                    cloudWriter2.createDeepDir(deepDirPath, isRelative)
                )
            }
        }
    }


    @Test fun creates_dir_if_not_exists() = run {

        step("Создаю каталог '$dirPath', если не существует") {
            runBlocking {
                Assert.assertEquals(
                    absoluteDirPath,
                    cloudWriter2.createDirIfNotExist(dirPath, isRelative)
                )
            }
        }

        step("Создаю каталог (если не существует) '$dirPath' ещё раз") {
            runBlocking {
                Assert.assertEquals(
                    absoluteDirPath,
                    cloudWriter2.createDirIfNotExist(dirPath, isRelative)
                )
            }
        }
    }


    @Test fun creates_deep_dir_if_not_exists() = run {
        step("Создаю глубокий каталог '$deepDirPath', если его нет") {
            runBlocking {
                Assert.assertEquals(
                    deepDirAbsolutePath,
                    cloudWriter2.createDeepDirIfNotExists(deepDirPath, isRelative)
                )
            }
        }
        step("Создаю глубокий каталог '$deepDirPath' (если его нет) ещё раз") {
            runBlocking {
                Assert.assertEquals(
                    deepDirAbsolutePath,
                    cloudWriter2.createDeepDirIfNotExists(deepDirPath, isRelative)
                )
            }
        }
    }


    @Test fun deletes_empty_dir() = run {
        step("Создаю каталог '$dirPath'") {
            runBlocking { createDir(dirPath, isRelative) }
        }
        step("Удаляю созданный каталог '$dirPath'") {
            runBlocking {
                Assert.assertEquals(
                    absoluteDirPath,
                    cloudWriter2.deleteEmptyDir(dirPath, isRelative)
                )
            }
        }
        step("Проверяю, что каталог '$dirPath' действительно удалён") {
            runBlocking {
                Assert.assertFalse(cloudWriter2.fileExists(dirPath, isRelative))
            }
        }
    }

}