package com.github.aakumykov.cloud_writer

fun interface StreamFinishCallback {
    fun onFinish(readBytesCount: Long, writtenBytesCount: Long)
}