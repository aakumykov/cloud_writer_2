package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.BaseOfTests
import com.github.aakumykov.cloud_writer_2.utils.aggregateNamesToPath
import com.github.aakumykov.cloud_writer_2.utils.randomName
import com.github.aakumykov.utils.random
import java.io.File

abstract class CloudWriter2TestsCommon : BaseOfTests() {

    protected abstract val cloudWriter2: CloudWriter2
    protected abstract val isRelative: Boolean

    protected abstract val virtualRootPath: String

    protected val dirName: String = randomName
    protected val fileName: String = "$randomName.bin"
    protected val deepDirName: String = aggregateNamesToPath(randomName, randomName, randomName)

    protected abstract val dirPath: String
    protected abstract val deepDirPath: String

    protected abstract val filePath: String

    private val dataBytes: ByteArray by lazy { random.nextBytes(100) }

    protected val fileWithData: File by lazy {
        File.createTempFile("file_","_upload.txt").apply {
            writeBytes(dataBytes)
        } }

    protected val absoluteFilePath: String get() = cloudWriter2.virtualRootPlus(fileName)

    protected val absoluteDirPath: String get() = cloudWriter2.virtualRootPlus(dirName)
    protected val deepDirAbsolutePath get() = aggregateNamesToPath(virtualRootPath, deepDirName)

    protected val dirRelativePath: String = absolutePathMinusVirtualRoot(absoluteDirPath)
    protected val deepDirRelativePath = absolutePathMinusVirtualRoot(deepDirAbsolutePath)


    private fun absolutePathMinusVirtualRoot(absolutePath: String): String
            = absolutePath.replace(Regex("^$virtualRootPath"),"")


    protected open suspend fun createDir(
        dirPath: String,
        isRelative: Boolean,
        onDirCreationAction: (suspend (createdDirPath:String) -> Unit)? = null
    ) {
        cloudWriter2.createDir(dirPath, isRelative).also { createdDirPath ->
            onDirCreationAction?.invoke(createdDirPath)
        }
    }
}