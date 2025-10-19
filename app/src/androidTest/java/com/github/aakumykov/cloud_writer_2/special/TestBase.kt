package com.github.aakumykov.cloud_writer_2.special

import android.os.Environment
import java.io.File

abstract class TestBase : StorageAccessTestCase() {

    protected val storageRootDir: File
        get() = Environment.getExternalStorageDirectory()

    protected val downloadsDir: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    protected val photosDir: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

    protected fun createFileIn(
        parentDir: File, fileName: String, fileContents: ByteArray? = null
    ): File {
        return File(parentDir, fileName).apply {
            createNewFile()
            if (null != fileContents)
                writeBytes(fileContents)
        }
    }
}