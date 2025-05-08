package com.github.aakumykov.local_cloud_writer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.aakumykov.cloud_writer.CloudWriter
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class LocalCloudWriterInstrumentedTest {

    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().context
    }

    private val sourceFile: File
        get() = File(context.cacheDir, SOURCE_FILE_NAME)

    private val targetFile: File
        get() = File(context.cacheDir, TARGET_FILE_NAME)

    private val sourceDirParentDirPath: String
        get() = context.cacheDir.absolutePath

    private val sourceDirName: String
        get() = "dirInSource_1"


    @Before
    fun prepareSourceFile() {
        removeAllTestFiles()
        sourceFile.apply {
            createNewFile()
            writeBytes(randomBytes)
        }
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


    @Test
    fun when_copy_file_then_it_copied_with_its_content() {
        LocalCloudWriter().copyFile(
            sourceFile.absolutePath,
            targetFile.absolutePath,
            true
        ).also {
            assertTrue(targetFile.exists())
            assertEquals(
                sourceFile.readBytes().joinToString(""),
                targetFile.readBytes().joinToString(""),
            )
        }
    }


    @Test
    fun when_create_dir_then() {
        LocalCloudWriter().createDir(
            sourceDirParentDirPath,
            sourceDirName
        ).also { dirPath ->
            assertTrue(File(dirPath).exists())
            assertEquals(
                CloudWriter.composeFullPath(sourceDirParentDirPath, sourceDirName),
                dirPath
            )
        }
    }



    private fun removeAllTestFiles() {
        sourceFile.delete()
        sourceFile.delete()
    }

    private val randomBytes: ByteArray
        get() = Random.nextBytes(10)

    companion object {
        const val SOURCE_FILE_NAME = "source_file.txt"
        const val TARGET_FILE_NAME = "target_file.txt"
    }
}