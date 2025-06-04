package com.github.aakumykov.extensions

val Throwable.errorMsg: String get() = message ?: javaClass.name

