package com.github.aakumykov.local_cloud_writer

import org.junit.Assert
import org.junit.Test

class LocalCloudWriterIOStreamsInstrumentedTest : LocalCloudWriterInstrumentedTest() {

    @Test
    fun input_stream_allows_read_bytes_from_file() {
        createSourceFile()

        val inputStream = localCloudWriter.getInputStream(sourceFileParentPath, SOURCE_FILE_NAME)

        Assert.assertEquals(
            sourceFileContents,
            inputStream.readBytes().joinToString("")
        )
    }

    @Test
    fun output_stream_allows_write_bytes_to_file() {
        createSourceFile()
        createTargetFile()

        val data = randomBytes10

        val outputStream = localCloudWriter.getOutputStream(targetFileParentPath, TARGET_FILE_NAME)
        outputStream.write(data)

        Assert.assertEquals(
            data.joinToString(""),
            targetFileContents
        )
    }
}