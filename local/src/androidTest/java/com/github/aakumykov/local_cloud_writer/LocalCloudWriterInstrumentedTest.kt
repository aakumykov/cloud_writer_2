package com.github.aakumykov.local_cloud_writer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.aakumykov.cloud_writer.CloudWriter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
open class LocalCloudWriterInstrumentedTest {

    protected val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().context
    }

    protected val testDirName: String
        get() = "test_dir_1"

    protected val testDir: File
        get() = File(testDirParentPath, testDirName)

    protected val testDirParentPath: String
        get() = context.cacheDir.absolutePath


    protected val sourceFileParentPath: String = testDir.absolutePath
    protected val targetFileParentPath: String = testDir.absolutePath

    protected val sourceFileDir: File  = File(sourceFileParentPath)
    protected val targetFileDir: File  = File(targetFileParentPath)

    protected val sourceFile: File
        get() = File(sourceFileDir, SOURCE_FILE_NAME)

    protected val targetFile: File
        get() = File(targetFileDir, TARGET_FILE_NAME)


    protected val sourceFileContents: String
        get() = sourceFile.readBytes().joinToString("")

    protected val targetFileContents: String
        get() = targetFile.readBytes().joinToString("")


    @Before
    fun removeAllTestFiles() {
        sourceFile.delete()
        targetFile.delete()
        testDir.delete()

        Assert.assertFalse(sourceFile.exists())
        Assert.assertFalse(targetFile.exists())
        Assert.assertFalse(testDir.exists())
    }


    @Test
    fun empty_test(){}



    fun createSourceFile(size: Int = 10) {
        sourceFileDir.mkdirs()
        Assert.assertTrue(sourceFileDir.exists())

        sourceFile.apply {
            createNewFile().also { Assert.assertTrue(it) }
            writeBytes(randomBytes(size))
        }
        Assert.assertTrue(sourceFile.exists())
        Assert.assertTrue(sourceFileContents.isNotEmpty())
    }

    fun createTargetFile() {
        targetFileDir.mkdirs()
        Assert.assertTrue(targetFileDir.exists())

        targetFile.apply {
            createNewFile().also { Assert.assertTrue(it) }
            writeBytes(randomBytes10)
        }
        Assert.assertTrue(targetFile.exists())
        Assert.assertTrue(targetFileContents.isNotEmpty())
    }

    protected fun randomBytes(count: Int) = Random.nextBytes(count)

    protected val randomBytes10: ByteArray
        get() = randomBytes(10)


    protected val localCloudWriter: CloudWriter
        get() = LocalCloudWriter()


    companion object {
        const val SOURCE_FILE_NAME = "source_file.txt"
        const val TARGET_FILE_NAME = "target_file.txt"
    }
}

val currentTime get() = java.util.Date().time