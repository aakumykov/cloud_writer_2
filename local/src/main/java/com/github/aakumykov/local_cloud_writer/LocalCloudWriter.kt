package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter
import com.github.aakumykov.cloud_writer.CloudWriter.OperationTimeoutException
import com.github.aakumykov.cloud_writer.CloudWriter.OperationUnsuccessfulException
import com.github.aakumykov.cloud_writer.StreamFinishCallback
import com.github.aakumykov.cloud_writer.StreamWritingCallback
import com.github.aakumykov.copy_between_streams_with_counting.copyBetweenStreamsWithCounting
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class LocalCloudWriter constructor(
    private val authToken: String = ""
): CloudWriter
{
    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
    )
    override fun createDir(basePath: String, dirName: String) {

        val fullDirName = CloudWriter.composeFullPath(basePath, dirName)

        with(File(fullDirName)) {
            if (!exists())
                if (!mkdirs())
                    throw OperationUnsuccessfulException(0, dirNotCreatedMessage(absolutePath))
        }
    }

    override fun createDirResult(basePath: String, dirName: String): Result<String> {
        return try {
            createDir(basePath, dirName)
            Result.success(File(basePath, dirName).absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun putFile(sourceFile: File, targetAbsolutePath: String, overwriteIfExists: Boolean) {

        val targetFile = File(targetAbsolutePath)

        val isMoved = sourceFile.renameTo(targetFile)

        if (!isMoved)
            throw IOException("File cannot be not moved from '${sourceFile.absolutePath}' to '${targetAbsolutePath}'")
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun putStream(
        inputStream: InputStream,
        targetPath: String,
        overwriteIfExists: Boolean,
        writingCallback: StreamWritingCallback?,
        finishCallback: StreamFinishCallback?,
    ) {
        val targetFile = File(targetPath)

        if (targetFile.exists() && !overwriteIfExists)
            return

        copyBetweenStreamsWithCounting(
            inputStream = inputStream,
            outputStream = targetFile.outputStream(),
            writingCallback = {},
            finishCallback = { writtenBytesCount, readBytesCount ->
                finishCallback?.onFinish(writtenBytesCount, readBytesCount)
            }
        )
    }

    override fun deleteDir(basePath: String, dirName: String) {
        val fsObject = File(basePath, dirName)
        fsObject.also {
            if (it.isDirectory) it.delete()
            else throw IllegalArgumentException("'${fsObject.absolutePath}' is not directory")
        }
    }


    override fun fileExists(parentDirName: String, childName: String): Boolean {
        return File(parentDirName, childName).exists()
    }

    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    override fun deleteFile(basePath: String, fileName: String) {

        val path = CloudWriter.composeFullPath(basePath, fileName)

        with(File(path)) {
            if (!exists())
                throw FileNotFoundException(path) // FIXME: осмысленное сообщение

            if (!delete())
                throw UnsupportedOperationException("File '$path' was not deleted.") // FIXME: OperationUnsuccessfulException
        }
    }

    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    override fun deleteDirRecursively(basePath: String, dirName: String) {

        val path = CloudWriter.composeFullPath(basePath, dirName)

        with(File(path)) {
            if (!exists())
                throw FileNotFoundException("Dir not found: $path")

            if (!deleteRecursively())
                throw OperationUnsuccessfulException("Dir was not deleted recursively: $path")
        }
    }

    override fun renameFileOrEmptyDir(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean
    ): Boolean {
        val targetFile = File(toAbsolutePath)
        if (!overwriteIfExists && targetFile.exists())
            return false
        return File(fromAbsolutePath).renameTo(targetFile)
    }


    private fun dirNotCreatedMessage(dirName: String): String
            = "Directory '${dirName}' not created."
}