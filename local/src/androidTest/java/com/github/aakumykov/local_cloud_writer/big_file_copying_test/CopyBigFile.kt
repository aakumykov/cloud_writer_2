package com.github.aakumykov.local_cloud_writer.big_file_copying_test

import android.os.Environment
import android.util.Log
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter
import com.github.aakumykov.local_cloud_writer.currentTime
import com.github.aakumykov.local_cloud_writer.kaspresso.StorageAccessTestCase
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.io.File

class CopyBigFile : StorageAccessTestCase() {

    private val localCloudWriter get() = LocalCloudWriter()

    @Test
    fun copying_big_file_as_stream_duration() {

        val sourceFileName = "debian.iso"
        val targetFileName = "debian2.iso"

        val downloadsDir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
        val musicDir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)

        val sourceFile = File(downloadsDir, sourceFileName)
        val targetFile = File(musicDir, targetFileName)

        Assert.assertTrue(sourceFile.exists())


        sourceFile.inputStream().use { inputStream ->
            val startTime = currentTime
            localCloudWriter.putStream(
                inputStream,
                targetFile.absolutePath,
                overwriteIfExists = true,
                finishCallback = { _,_,_ ->
                    val duration = currentTime - startTime
                    Log.d(TAG,"продолжительность копирования $duration")
                }
            )
        }

        TestCase.assertTrue(sourceFile.exists())
        TestCase.assertTrue(targetFile.exists())
    }

    companion object {
        val TAG: String = CopyBigFile::class.java.simpleName
    }
}