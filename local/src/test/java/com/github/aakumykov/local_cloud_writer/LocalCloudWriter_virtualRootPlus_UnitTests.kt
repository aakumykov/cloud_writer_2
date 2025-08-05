package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import org.junit.Assert
import org.junit.Test

class LocalCloudWriter_virtualRootPlus_UnitTests {

    private val localCloudWriter2: CloudWriter2 = LocalCloudWriter2("/")

    // `соединяет несколько путей в один`
    @Test
    fun joins_multiple_names_to_path() {
        repeat(10) { i ->
            val dirNameList: List<String> = buildList { add("dir$i") }
            val resPath = localCloudWriter2.virtualRootPlus(*dirNameList.toTypedArray())
            Assert.assertEquals(
                ,
            )
        }
    }
}