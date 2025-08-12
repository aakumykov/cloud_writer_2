package com.github.aakumykov.cloud_writer_2.utils

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.BaseOfTests
import org.junit.Assert
import org.junit.Test

class FunctionsTests : BaseOfTests() {

    companion object {
        const val NAME1 = "dir1"
        const val NAME2 = "dir2"
        const val NAME3 = "dir3"
    }

    @Test
    fun aggregates_names_to_path() {
        Assert.assertEquals(
            "${NAME1}${CloudWriter2.DS}${NAME2}${CloudWriter2.DS}${NAME3}",
            aggregateNamesToPath(NAME1, NAME2, NAME3)
        )
    }

    @Test
    fun short_id_is_not_empty() {
        Assert.assertTrue(
            shortId.isNotEmpty()
        )
    }

    @Test
    fun random_id_is_not_empty() {
        Assert.assertTrue(
            randomId.isNotEmpty()
        )
    }

    @Test
    fun random_name_is_not_empty() {
        Assert.assertTrue(
            randomName.isNotEmpty()
        )
    }
}