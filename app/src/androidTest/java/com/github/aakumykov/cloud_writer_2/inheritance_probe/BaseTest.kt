package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase
import com.github.aakumykov.cloud_writer_2.common.randomName
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

abstract class BaseTest : StorageAccessTestCase() {

    protected abstract val cloudWriter2: CloudWriter2

    protected val dirName: String = randomName

    abstract val isRelative: Boolean
    protected abstract val virtualRootPath: String
    protected abstract val absoluteDirPath: String
    protected abstract val creatingSimpleDirName: String
    protected abstract val creatingDeepDirName: String

    protected fun aggregateNamesToPath(vararg dirName: String): String
        = dirName
            .filterNot { it.isEmpty() }
            .joinToString(CloudWriter2.DS)

    @Test
    fun names_to_path_aggregation_test() = run {
        step("Пустая строка собирается в пустую строку") {
            Assert.assertEquals("", aggregateNamesToPath(""))
        }
        step("Несколько пустых строк строка собирается в разделитель") {
            Assert.assertEquals("", aggregateNamesToPath("","",""))
        }
        step("Одна строка собирается в себя же") {
            val name = randomName
            Assert.assertEquals(name, aggregateNamesToPath(name))
        }
        step("Несколько строк собирается....") {
            for (i in 2..10) {
                step("При количестве число строк: $i") {
                    val stringArray = buildList { repeat(i) { add(randomName) } }
                    val resultString = aggregateNamesToPath(*stringArray.toTypedArray())

                    step("...первая строка в начале") {
                        Assert.assertTrue(resultString.startsWith(stringArray.first()))
                    }
                    step("...третья строка в конце") {
                        Assert.assertTrue(resultString.endsWith(stringArray.last()))
                    }
                    step("...количество разделителей на единицу меньше числа соединяемых элементов") {
                        Assert.assertEquals(
                            stringArray.size-1,
                            resultString.count { CloudWriter2.DS == it.toString() }
                        )
                    }
                }
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