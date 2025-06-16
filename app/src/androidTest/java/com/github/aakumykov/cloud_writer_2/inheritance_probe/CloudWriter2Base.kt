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

    @Test
    fun file_exists() = run {
        val dirName = randomName
        step("Проверяю, что каталог '$dirName' отсутствует") {
            runBlocking {
                Assert.assertFalse(cloudWriter2.fileExists(dirName, isRelative))
            }
        }
        step("Создаю каталог '$dirName") {
            runBlocking { cloudWriter2.createDir(dirName, isRelative) }
        }
        step("Проверяю, что каталог '$dirName' существует") {
            runBlocking {
                Assert.assertTrue(cloudWriter2.fileExists(dirName, isRelative))
            }
        }
    }

    @Test
    fun creates_dir() = run {
        step("Создаю каталог '$creatingSimpleDirName'") {
            runTest {
                cloudWriter2.createDir(creatingSimpleDirName, isRelative).also { createdDirPath ->
                    step("Проверяю, что путь к нему соответствует '$absoluteDirPath'") {
                        Assert.assertEquals(absoluteDirPath, createdDirPath)
                    }
                }
            }
        }
    }
}