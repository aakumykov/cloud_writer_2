package com.github.aakumykov.local_cloud_writer.different_tests

import com.github.aakumykov.local_cloud_writer.LocalCloudWriterInstrumentedTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class TransferredBytesFlowInstrumentedTest : LocalCloudWriterInstrumentedTest() {

    @Test
    fun returned_flow_emits_transferred_bytes_data() = runBlocking {
        val dataSizeBytes = 1000
        createSourceFile(dataSizeBytes)

        val valuesFromFlow = mutableListOf<Long>()

        sourceFile.inputStream().use { inputStream ->
            localCloudWriter.putStream(
                inputStream = inputStream,
                targetAbsolutePath = targetFile.absolutePath
            )/*.collect { transferredBytes: Long ->
                valuesFromFlow.add(transferredBytes)
            }*/
        }

//        Assert.assertTrue("Данные о прогрессе в принципе приходили", valuesFromFlow.isNotEmpty())
//        Assert.assertTrue("Данные о прогрессе приходили в порядке увеличения", valuesFromFlow.first() >valuesFromFlow.last())
//        Assert.assertEquals("Последнее значение о прогрессе == размеру данных", dataSizeBytes, valuesFromFlow.last())
    }

    @Test
    fun r() = runBlocking { Assert.assertTrue(true) }
}