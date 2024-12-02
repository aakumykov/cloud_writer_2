package com.github.aakumykov.cloud_writer

fun interface StreamWritingCallback {
    fun onWriteCountChanged(count: Long)
}
