package com.github.aakumykov.cloud_writer.extensions

fun String.stripMultiSlashes(): String = this.replace(Regex("[/]+"),"/")