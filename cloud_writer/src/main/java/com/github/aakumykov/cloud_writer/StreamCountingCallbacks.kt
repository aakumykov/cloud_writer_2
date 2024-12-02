package com.github.aakumykov.cloud_writer

interface StreamCountingCallbacks {

    fun interface ReadingCallback {
        fun onReadCountChanged(count: Long)
    }

    fun interface WritingCallback {
        fun onWriteCountChanged(count: Long)
    }
}
