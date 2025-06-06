package com.github.aakumykov.cloud_writer

import com.github.aakumykov.cloud_writer.extensions.stripMultiSlashes
import java.io.File
import java.io.IOException
import java.io.InputStream

// TODO: suspend-методы
// TODO: возвращать Result вместо выборса исключений
interface CloudWriter {

    /**
     * Пробует создать каталог по указанному пути.
     * Дочерний каталог может быть многоуровневым.
     * @param basePath "Родительский" каталог.
     * @param dirName Имя создаваемого каталога, может быть многоуровневым. // FIXME: нужна ли эта многоуровневость?
     * @return Абсолютный путь к созданному каталогу.
     * @throws [IOException], [OperationUnsuccessfulException]
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun createDir(basePath: String, dirName: String): String


    /**
     * Пробует создать каталог по указанному пути.
     * @return Полный путь к созданному каталогу.
     * @throws [IOException], [OperationUnsuccessfulException]
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class,)
    fun createDir(absoluteDirPath: String): String


    /**
     * Создаёт каталог, по отдельности создавая каждый каталог в глубину
     * пути, если такого ещё не существует.
     * @param force Не проверять наличие каталога, пробовать создавать сразу.
     * @return Полный путь к созданному каталогу.
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class,)
    fun createDeepDirIfNotExists(absolutePath: String, force: Boolean = false): String


    /**
     * @see [createDirIfNotExists]
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class,)
    fun createDeepDirIfNotExists(basePath: String, dirName: String, force: Boolean = false): String


    /**
     * Создаёт каталог с именем [dirName] в указанном [basePath] каталоге,
     * если такового не существует (что актуально для облачных хранилищ: их API
     * могут выбрасывать ошибку при попытке повторного создания каталога).
     *
     * @param force Пытаться создать каталог, даже если он уже существует.
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun createDirIfNotExists(basePath: String, dirName: String, force: Boolean = false): String


    /**
     * Пробует создать каталог по указанному пути, если такого ещё не существует.
     * (В случае облачного API это приводит к задержке на проверку.)
     * @throws [IOException], [OperationUnsuccessfulException]
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun createDirIfNotExists(absoluteDirPath: String, force: Boolean = false): String



            /**
     * Создаёт каталог в указанном каталоге. Дочерний каталог может быть многоуровневым.
     * @see [createDir]
     * @return Полный путь к созданному каталогу, обёрнутый в [kotlin.Result]
     */
    fun createDirResult(basePath: String, dirName: String): Result<String>


    /**
     * Отправляет файл по указанному пути.
     * Реализации обязаны использовать параметр targetPath "как есть", не внося в него корректировки!
     *
     * Метод не умеет создавать родительские каталоги, поэтому каталог назначения
     * должен существовать заранее. Может быть создан метдами [createDir], [createDirResult],
     * которые способны создавать "глубокие" каталоги.
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun putFile(sourceFile: File, targetAbsolutePath: String, overwriteIfExists: Boolean = false)


    /**
     * Записывает поток в файл по указанному пути, читая данные из InputStream.
     * @param writingCallback
     * @param finishCallback
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun putStream(
        inputStream: InputStream,
        targetPath: String,
        overwriteIfExists: Boolean = false,
        writingCallback: ((Long) -> Unit)? = null,
        finishCallback: ((Long,Long) -> Unit)? = null,
    )


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun fileExists(parentDirName: String, childName: String): Boolean


    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun fileExists(absolutePath: String): Boolean


    // TODO: локальное удаление в корзину
    /**
     * Удаляет файл/папку.
     */
    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    @Deprecated("Избавиться")
    fun deleteFile(basePath: String, fileName: String)


    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    fun deleteDir(basePath: String, dirName: String)


    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    // TODO: удаление в копзину/полное
    @Deprecated("Избавиться")
    fun deleteDirRecursively(basePath: String, dirName: String)


    /**
     * Переименовывает файл или пустой каталог.
     * Не работает с локальным хранилищем, если целевой
     * файл находится на физическом разделе, отличном
     * от исходного.
     */
    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
    fun renameFileOrEmptyDir(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean = true
    ): Boolean


    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class
    )
    fun copyFile(fromAbsolutePath: String, toAbsolutePath: String, overwriteIfExists: Boolean)


    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class
    )
    fun moveFileOrEmptyDir(
        fromAbsolutePath: String,
        toAbsolutePath: String,
        overwriteIfExists: Boolean
    ): Boolean


    // TODO: выделить в отдельный файл...
    sealed class CloudWriterException(message: String) : Exception(message)

    class OperationUnsuccessfulException(errorMsg: String) : CloudWriterException(errorMsg) {
        constructor(responseCode:Int, responseMessage: String) : this("$responseCode: $responseMessage")
    }

    class OperationTimeoutException(errorMsg: String) : CloudWriterException(errorMsg)


    companion object {
        const val DS = "/"

        fun composeFullPath(vararg pathParts: String): String {
            return pathParts.joinToString(DS).stripMultiSlashes()
        }
    }
}