package com.github.aakumykov.cloud_writer_2.local

interface Props {
    val isRelative: Boolean
    val dirPath: String
    val deepDirPath: String
    val filePath: String
}