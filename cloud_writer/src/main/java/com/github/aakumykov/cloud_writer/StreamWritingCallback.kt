package com.github.aakumykov.cloud_writer

interface StreamWritingCallback {
    fun onWriteCountChanged(count: Long)
}
