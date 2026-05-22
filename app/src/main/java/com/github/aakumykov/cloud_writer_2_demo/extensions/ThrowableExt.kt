package com.github.aakumykov.cloud_writer_2_demo.extensions

val Throwable.errorMsg: String get() = message ?: javaClass.name

val Throwable.errorMsgExtended: String get() =
        if (null != message) "${message} (${javaClass.name})"
        else javaClass.name