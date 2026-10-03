package com.github.aakumykov.cloud_writer

import com.github.aakumykov.copy_between_streams_with_speed.SimpleStreamToStreamCopier
import java.io.InputStream
import java.io.OutputStream

abstract class BaseCloudWriter : CloudWriter {

    private val streamCopier by lazy { SimpleStreamToStreamCopier() }

    protected fun copyBetweenStreamsWithSpeed(
        inputStream: InputStream,
        outputStream: OutputStream,
        speedBytesPerSec: Int,
        progressRate: Int = 1,
        progressCallback: ((transferredBytes:Long, speedBytesPerSec:Long) -> Unit)? = null,
        finishCallback: ((transferredBytes:Long, timeElapsedMs:Long, speedBytesPerSec:Long) -> Unit)? = null,
    ) {
        streamCopier.copyFromStreamToStream(
            inputStream,
            outputStream,
            speedBytesPerSec,
            progressRate,
            finishCallback,
            progressCallback,
        )
    }
}