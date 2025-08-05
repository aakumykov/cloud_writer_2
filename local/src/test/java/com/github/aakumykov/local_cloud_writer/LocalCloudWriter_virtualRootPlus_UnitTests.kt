package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import org.junit.Assert
import org.junit.Test

class LocalCloudWriter_virtualRootPlus_UnitTests {

    private val localCloudWriter2: CloudWriter2 = LocalCloudWriter2("/")

    @Test
    fun joins_multiple_names_to_path() {

        val dirNamePrefix = "dir"

        repeat(10) { i ->

            val expectedPathBuilder = StringBuilder()
                .append(localCloudWriter2.virtualRootPath)

            val dirNameList: List<String> = buildList { repeat(i+1) { j ->

                val dirName = "$dirNamePrefix$j"

                this.add(dirName)

                expectedPathBuilder
                    .append(CloudWriter2.DS)
                    .append(dirName)
            } }

            val resultPath = localCloudWriter2.virtualRootPlus(*dirNameList.toTypedArray())

            Assert.assertEquals(
                expectedPathBuilder,
                resultPath
            )
        }
    }
}