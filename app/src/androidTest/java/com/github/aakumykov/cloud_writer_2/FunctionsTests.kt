package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.utils.randomName
import com.github.aakumykov.cloud_writer_2.common.BaseOfTests
import com.github.aakumykov.cloud_writer_2.utils.aggregateNamesToPath
import org.junit.Assert
import org.junit.Test

class FunctionsTests : BaseOfTests() {

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
    fun random_name_test() = run {
        val list = buildList<String> {
            repeat(10000) { add(randomName) }
        }
        Assert.assertEquals(
            list,
            list.distinct()
        )
    }
}