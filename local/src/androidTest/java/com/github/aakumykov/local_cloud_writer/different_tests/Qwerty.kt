package com.github.aakumykov.local_cloud_writer.different_tests

import android.util.Log
import com.github.aakumykov.local_cloud_writer.LocalCloudWriterInstrumentedTest
import com.github.aakumykov.local_cloud_writer.currentTime
import com.github.aakumykov.local_cloud_writer.different_tests.CopyBigFileInstrumentedTest.Companion.TAG
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class Qwerty : LocalCloudWriterInstrumentedTest() {

    @Test
    fun r() = runBlocking {
        val dataSizeBytes = DEFAULT_BUFFER_SIZE * 2
        createSourceFile(dataSizeBytes)

        sourceFile.inputStream().use { inputStream ->
            localCloudWriter.putStream(
                inputStream = inputStream,
                targetAbsolutePath = targetFile.absolutePath
            ).collect { transferredBytes: Long ->
                println(transferredBytes.toString())
            }
            println()
        }
    }

}