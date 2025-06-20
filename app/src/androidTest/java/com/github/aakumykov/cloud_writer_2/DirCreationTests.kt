package com.github.aakumykov.cloud_writer_2

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class DirCreationTests : CloudWriter2TestsCommon() {


    @Test
    fun creates_dir() = run {
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


}