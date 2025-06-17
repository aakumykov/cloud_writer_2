package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.cloud_writer_2.inheritance_probe.common.BaseOfTests
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class CloudWriter2Tests : BaseOfTests() {

    protected abstract val cloudWriter2: CloudWriter2
    protected abstract val isRelative: Boolean

    protected abstract val virtualRootPath: String

    protected val dirName: String = randomName
    protected val deepDirName: String = aggregateNamesToPath(randomName,randomName,randomName)

    protected abstract val dirPath: String
    protected abstract val deepDirPath: String

    protected val absoluteDirPath: String get() = cloudWriter2.virtualRootPlus(dirName)
    protected val deepDirAbsolutePath get() = aggregateNamesToPath(virtualRootPath, deepDirName)

    protected val dirRelativePath: String = absolutePathMinusVirtualRoot(absoluteDirPath)
    protected val deepDirRelativePath = absolutePathMinusVirtualRoot(deepDirAbsolutePath)

    /*@Before
    fun check_dirs_are_not_exists() {
        runTest {
            Assert.assertFalse(cloudWriter2.fileExists(dirAbsolutePath, false))
            Assert.assertFalse(cloudWriter2.fileExists(deepDirAbsolutePath, false))
        }
    }*/

    /*@After
    fun delete_dir() {
        runTest {
            cloudWriter2.deleteEmptyDir(dirAbsolutePath, false)
            cloudWriter2.deleteEmptyDir(deepDirAbsolutePath, false)
        }
    }*/


    @Test
    fun creates_dir() = run {
        step("Создаю каталог '${dirPath}'") {
            runTest {
                cloudWriter2.createDir(dirPath, isRelative).also { createdDirPath ->
                    step("Проверяю, что путь к нему соответствует '$absoluteDirPath'") {
                        Assert.assertEquals(absoluteDirPath, createdDirPath)
                    }
                }
            }
        }
    }


    /*@Test
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
}*/


    private fun absolutePathMinusVirtualRoot(absolutePath: String): String
        = absolutePath.replace(Regex("^$virtualRootPath"),"")
}