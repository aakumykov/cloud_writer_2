package com.github.aakumykov.local_cloud_writer

import android.util.Log
import com.github.aakumykov.cloud_writer.CloudWriter
import com.github.aakumykov.cloud_writer.CloudWriter.OperationTimeoutException
import com.github.aakumykov.cloud_writer.CloudWriter.OperationUnsuccessfulException
import com.github.aakumykov.copy_between_streams_with_counting.copyBetweenStreamsWithCounting
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * @param virtualRootDir Параметр конструктора. Путь, относительно которого будут создаваться каталоги.
 *  Необходимость его возникла в связи с тем, что, например,
 *  в Android корневой каталог недоступен для записи. Для сохранения совместимости по умолчанию
 *  установлен в пустую строку ("").
 *  @param authToken Не используется.
 */
class LocalCloudWriter(
    private val virtualRootDir: String = "",
    private val authToken: String = ""
): CloudWriter
{
    /**
     * Создаёт каталог по пути [virtualRootDir] + [basePath] + [dirName]
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDir(basePath: String, dirName: String): String {
        Log.d(TAG, "createDir(basePath = $basePath, dirName = $dirName)")
        return createDir(virtualRootPlus(basePath, dirName))
    }


    /**
     * Создаёт каталог по пути [absoluteDirPath]
     * Если промежуточные каталоги отсутствуют, они будут созданы.
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDir(absoluteDirPath: String): String {
        createDirReal(absoluteDirPath)
        return absoluteDirPath
    }


    /**
     * Создаёт каталог по пути [virtualRootDir] + [basePath] + [dirName],
     * если таковой не существует.
     * @return Полный путь к каталогу.
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDirIfNotExists(basePath: String, dirName: String, force: Boolean): String {
        Log.d(TAG, "createDirIfNotExists(basePath = $basePath, dirName = $dirName, force = $force)")
        return createDirIfNotExists(virtualRootPlus(basePath, dirName))
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDirIfNotExists(absoluteDirPath: String, force: Boolean): String {
        Log.d(TAG, "createDirIfNotExists(absoluteDirPath = $absoluteDirPath, force = $force)")
        if (!force && !fileExists(absoluteDirPath))
                createDirReal(absoluteDirPath)
        return absoluteDirPath
    }


    /**
     * Создаёт "глубокий" каталог по пути [virtualRootDir] + [absolutePath]
     * @see CloudWriter.createDeepDirIfNotExists
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun createDeepDirIfNotExists(absolutePath: String, force: Boolean): String {

        Log.d(TAG, "createDeepDirIfNotExists(path = $absolutePath, force = $force)")

        //
        // Разбивать нужно путь, не включающий виртуальный!
        //

        return virtualRootPlus(absolutePath).let { path ->
            path
                .split(CloudWriter.DS)
                .reduce { acc, s ->
                    createDirIfNotExists(acc, force)
                    acc + CloudWriter.DS + s
                }.also { tailDir: String ->
                    createDirIfNotExists(tailDir, force)
                }
            path
        }
    }

    override fun createDeepDirIfNotExists(
        basePath: String,
        dirName: String,
        force: Boolean
    ): String {
        Log.d(TAG, "createDeepDirIfNotExists(basePath = $basePath, dirName = $dirName, force = $force)")
        return createDirIfNotExists(CloudWriter.composeFullPath(basePath, dirName), force)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun moveFileOrEmptyDir(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean
    ): Boolean {
        return renameFileOrEmptyDir(fromAbsolutePath, toAbsolutePath, overwriteIfExists)
    }


    /**
     * @see [createDir]
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
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
        targetAbsolutePath: String,
        overwriteIfExists: Boolean,
        writingCallback: ((Long) -> Unit)?,
        finishCallback: ((Long,Long) -> Unit)?,
    ) {
        val targetFile = File(targetAbsolutePath)

        if (targetFile.exists() && !overwriteIfExists)
            return

        copyBetweenStreamsWithCounting(
            inputStream = inputStream,
            outputStream = targetFile.outputStream(),
            writingCallback = writingCallback,
            finishCallback = finishCallback,
        )
    }


    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class
    )
    override fun copyFile(fromAbsolutePath: String, toAbsolutePath: String, overwriteIfExists: Boolean) {
        File(fromAbsolutePath).copyTo(File(toAbsolutePath), overwriteIfExists)
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun deleteDir(basePath: String, dirName: String) {
        val fsObject = File(basePath, dirName)
        fsObject.also {
            if (it.isDirectory) it.delete()
            else throw IllegalArgumentException("'${fsObject.absolutePath}' is not directory")
        }
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun fileExists(parentDirName: String, childName: String): Boolean {
        return fileExists(CloudWriter.composeFullPath(parentDirName, childName))
    }


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    override fun fileExists(absolutePath: String): Boolean {
        return File(absolutePath).exists()
    }


    @Deprecated("Избавиться")
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


    @Deprecated("Избавиться")
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


    private fun virtualRootPlus(vararg pathParts: String): String {
        return CloudWriter.composeFullPath(
            virtualRootDir,
            pathParts.joinToString(CloudWriter.DS)
        )
    }


    /**
     * Создаёт каталог (включая отсутствующие) по указанному в аргументе [absolutePath] пути.
     * Не добавляет виртуальный корень в качестве префикса.
     */
    private fun createDirReal(absolutePath: String) {
        with(File(absolutePath)) {
            if (!mkdirs())
                throw OperationUnsuccessfulException("Каталог '$absolutePath' не создан.")
        }
    }


    companion object {
        val TAG: String = LocalCloudWriter::class.java.simpleName
    }
}