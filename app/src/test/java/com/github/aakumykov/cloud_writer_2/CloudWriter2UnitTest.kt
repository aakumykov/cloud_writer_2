package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import org.junit.Assert
import org.junit.Test

class CloudWriter2UnitTest {

    private fun prepareTestData(virtualRoot: String): Map<String,String> = mapOf(
        // Вырожденные случаи
        "" to "",
        " " to " ",
        "   " to "  ",
        // Относительные пути
        "a" to "a",
        "qwerty" to "qwerty",
        "qwerty/" to "qwerty/",
        "a//b" to "a/b",
        "a/b///c" to "a/b/c",
        "абв гд//qwe rty/" to "абв гд/qwe rty/",
        // Абсолютные пути
        "//" to "/",
        "/////" to "/",
        "/a//b///c" to "/a/b/c",
        "//a b//c d/e f///" to "/a/b/c/d/e/f/",
        "/qwerty/" to "/qwerty/",
        "/qwe///rty/" to "/qwe/rty/",
    ).mapValues {

    }

    @Test
    fun correctly_operates_with_paths() {

        val virtualRoot = "/"

        val cloudWriter = LocalCloudWriter2(virtualRoot)

        prepareTestData(virtualRoot)
            .let { it }.forEach { (testData: String, expected: String) ->
            Assert.assertEquals(
                cloudWriter.virtualRootPlus(testData),
                expected
            )
        }
    }
}