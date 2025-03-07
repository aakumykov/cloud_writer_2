package com.github.aakumykov.local_cloud_writer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LocalCloudWriterInstrumentedTest {

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().context
    }

    private val sourceFile: File
        get() = File(context.cacheDir, SOURCE_FILE_NAME)

    private val targetFile: File
        get() = File(context.cacheDir, TARGET_FILE_NAME)


    @Before
    fun prepareSourceFile() {
        removeAllTestFiles()
        sourceFile.createNewFile()
    }

    @After
    fun removeTestFiles() {
        removeAllTestFiles()
    }

    @Test
    fun when_rename_file_then_it_renamed() {
        LocalCloudWriter().renameFileOrEmptyDir(
            sourceFile.absolutePath,
            targetFile.absolutePath
        )
        assertTrue(targetFile.exists())
    }


    private fun removeAllTestFiles() {
        sourceFile.delete()
        sourceFile.delete()
    }

    companion object {
        const val SOURCE_FILE_NAME = "source_file.txt"
        const val TARGET_FILE_NAME = "target_file.txt"
    }
}