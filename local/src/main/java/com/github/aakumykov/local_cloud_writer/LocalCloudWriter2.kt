package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.BasicCloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.copy_between_streams_with_counting.copyBetweenStreamsWithCounting
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocalCloudWriter2(
    override val virtualRootPath: String,
)
    : BasicCloudWriter2()
{
    override suspend fun createDir(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createAbsoluteDir(virtualRootPlus(dirPath))
        else createAbsoluteDir(dirPath)
    }

    override suspend fun createDirIfNotExist(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createAbsoluteDirIfNotExists(virtualRootPlus(dirPath))
        else createAbsoluteDirIfNotExists(dirPath)
    }

    private fun createAbsoluteDirIfNotExists(path: String): String {
        return if (fileExistsAbsolute(path)) path
        else createAbsoluteDir(path)
    }

    /**
     * @return Абсолютный путь к созданному каталогу.
     */
    override suspend fun createDeepDir(dirPath: String, isRelative: Boolean): String {

        val absolutePath = if (isRelative) virtualRootPlus(dirPath) else dirPath

        val relativePathToOperate = dirPath.replace(Regex("^${virtualRootPath}/+"),"")

        return iterateOverDirsInPathFromRoot(relativePathToOperate) { partialPath ->
            createDirIfNotExist(partialPath, true)
        }.let {
            absolutePath
        }
    }


    override suspend fun createDeepDirIfNotExists(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) createAbsoluteDeepDirIfNotExists(virtualRootPlus(dirPath))
        else createAbsoluteDeepDirIfNotExists(dirPath)
    }


    override suspend fun deleteFileOrEmptyDir(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) deleteEmptyDirAbsolute(virtualRootPlus(dirPath))
        else deleteEmptyDirAbsolute(dirPath)
    }

    private fun deleteEmptyDirAbsolute(absolutePath: String): String {
        return File(absolutePath).delete().let { isDeleted ->
            if (isDeleted) absolutePath
            else throw CloudWriterException("Dir '$absolutePath' was not deleted.")
        }
    }


    private suspend fun createAbsoluteDeepDirIfNotExists(path: String): String {
        return if (fileExistsAbsolute(path)) path
        else iterateOverDirsInPathFromRoot(path) { partialDeepPath ->
            createDirIfNotExist(partialDeepPath, true)
        }
    }



    private fun createAbsoluteDir(path: String): String {
        return with(File(path)) {
            if (!mkdir())
                throw CloudWriterException("Dir '$path' was not created")
            path
        }
    }

    override suspend fun fileExists(dirPath: String, isRelative: Boolean): Boolean {
        return if (isRelative) fileExistsAbsolute(virtualRootPlus(dirPath))
        else fileExistsAbsolute(dirPath)
    }

    private fun fileExistsAbsolute(path: String): Boolean {
        return File(path).exists()
    }


    @Throws(IOException::class, CloudWriterException::class)
    override suspend fun putStream(
        inputStream: InputStream,
        targetPath: String,
        isRelative: Boolean,
        overwriteIfExists: Boolean,
        readingCallback: ((Long) -> Unit)?,
        writingCallback: ((Long) -> Unit)?,
        finishCallback: ((Long,Long) -> Unit)?,
    ) {
        return suspendCancellableCoroutine { cc ->

            val path = if (isRelative) virtualRootPlus(targetPath) else targetPath

            val targetFile = File(path)

            if (targetFile.exists() && !overwriteIfExists)
                cc.resume(Unit)

            copyBetweenStreamsWithCounting(
                inputStream = inputStream,
                outputStream = targetFile.outputStream(),
                readingCallback = readingCallback,
                writingCallback = writingCallback,
                finishCallback = finishCallback,
            ).also {
                cc.resume(Unit)
            }
        }
    }


    override suspend fun renameFileOrEmptyDir(
        fromPath: String,
        toPath: String,
        isRelative: Boolean,
        overwriteIfExists: Boolean
    ): Boolean = suspendCoroutine { continuation ->

        val realFromPath = if (isRelative) virtualRootPlus(fromPath) else fromPath
        val realToPath = if (isRelative) virtualRootPlus(toPath) else toPath

        val targetFile = File(realToPath)

        continuation.resume(
            if (!overwriteIfExists && targetFile.exists()) false
            else File(realFromPath).renameTo(targetFile)
        )
    }
}