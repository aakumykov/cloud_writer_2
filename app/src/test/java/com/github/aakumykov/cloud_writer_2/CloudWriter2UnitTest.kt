package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import org.junit.Assert
import org.junit.Test

class CloudWriter2UnitTest {

    private fun prepareTestData(virtualRoot: String): Iterable<String> = listOf(
        // Вырожденные случаи
        "",
        " ",
        "\t",

        // Относительные пути
        "a",
        "qwerty",
        "qwerty/",
        "a//b",
        "a/b///c",
        "абв гд//qwe rty/",

        // Абсолютные пути
        "//",
        "/////",
        "/a//b///c",
        "//a b//c d/e f///",
        "/qwerty/",
        "/qwe///rty/",

    )

    @Test
    fun correctly_operates_with_canonical_virtual_root() {
        test_paths_with_virtual_root("/")
    }

    @Test
    fun correctly_operates_with_empty_virtual_root() {
        test_paths_with_virtual_root("")
    }

    @Test
    fun correctly_operates_with_custom_virtual_root_without_tail_slash() {
        test_paths_with_virtual_root("/path/to/custom/root")
    }

    @Test
    fun correctly_operates_with_custom_virtual_root_with_tail_slash() {
        test_paths_with_virtual_root("/path/to/custom/root/")
    }


    @Test
    fun companion_object_contains_DS_constant() {
        Assert.assertEquals(
            "/",
            CloudWriter2.DS
        )
    }


    private fun test_paths_with_virtual_root(virtualRoot: String) {
        println()
        println("=============== START: test_paths_with_virtual_root (virtualRoot=\"$virtualRoot\") ================")

        val cloudWriter = LocalCloudWriter2(virtualRoot)

        prepareTestData(virtualRoot)
            .forEach { testedPath: String ->
                val resultPath = cloudWriter.absolutePathFor(testedPath)
                Assert.assertTrue(resultPath.startsWith(virtualRoot, false))
                Assert.assertFalse(resultPath.contains("//+"))
                println("$testedPath --> $resultPath")
            }

        println("=============== FINISH: test_paths_with_virtual_root (virtualRoot=\"$virtualRoot\") ================")
        println()
    }
}