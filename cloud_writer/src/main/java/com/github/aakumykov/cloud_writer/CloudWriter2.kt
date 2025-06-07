package com.github.aakumykov.cloud_writer

import java.io.IOException

interface CloudWriter2 {
    @Throws(IOException::class, CloudWriterException::class)
    fun createDir(path: String, isRelative: Boolean): String

    @Throws(IOException::class, CloudWriterException::class)
    fun createDirIfNotExist(path: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    fun createDeepDir(path: String, isRelative: Boolean): String

    @Throws(IOException::class, CloudWriterException::class)
    fun createDeepDirIfNotExists(path: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    fun fileExists(path: String, isRelative: Boolean): Boolean


    companion object {
        /**
         * Directory separator.
         */
        const val DS = "/"
    }
}