package com.github.aakumykov.cloud_writer_2.inheritance_probe

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.cloud_writer_2.inheritance_probe.common.BaseOfTests
import org.junit.Assert
import org.junit.Test

class BaseOfTestsTest : BaseOfTests() {

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
}