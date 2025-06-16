package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase
import com.github.aakumykov.cloud_writer_2.common.randomName
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class BaseTest : StorageAccessTestCase() {

    protected val dirName: String = randomName

    protected abstract val cloudWriter2: CloudWriter2
    protected abstract val virtualRootPath: String
    protected abstract val absoluteDirPath: String
    protected abstract val relativeDirPath: String


    @Test
    fun creates_relative_local_dir() = run {
        step("Создаю каталог '$dirName'") {
            runTest {
                cloudWriter2.createDir(relativeDirPath, true).also { createdDirPath ->
                    step("Проверяю, что путь к нему соответствует '$absoluteDirPath'") {
                        Assert.assertEquals(absoluteDirPath, createdDirPath)
                    }
                }
            }
        }
    }


    @Test
    fun creates_absolute_local_dir() = run {
        step("Создаю каталог '$absoluteDirPath'") {
            runTest {
                cloudWriter2.createDir(dirName, true).also { createdDirPath ->
                    step("Проверяю, что путь к нему соответствует '$absoluteDirPath'") {
                        Assert.assertEquals(absoluteDirPath, createdDirPath)
                    }
                }
            }
        }
    }
}