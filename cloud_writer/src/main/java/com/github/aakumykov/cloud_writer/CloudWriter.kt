package com.github.aakumykov.cloud_writer

import com.github.aakumykov.cloud_writer.extensions.stripMultiSlashes
import java.io.File
import java.io.IOException
import java.io.InputStream

// TODO: suspend-методы
// TODO: возвращать Result вместо выборса исключений
interface CloudWriter {

    /**
     * Создаёт каталог в указанном каталоге. Дочерний каталог может быть многоуровневым.
     * @param basePath "Родительский" каталог.
     * @param dirName Имя создаваемого каталога, может быть многоуровневым.
     */
    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
    )
    fun createDir(basePath: String, dirName: String)

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
    fun putFile(file: File, targetPath: String, overwriteIfExists: Boolean = false)


    /**
     * Записывает поток в файл по указанному пути, читая данные из InputStream.
     * @param writingCallback Коллбек, вызывающийся при записи потока, через
     * каждые [DEFAULT_BUFFER_SIZE].
     * @param finishCallback Сигнал завершения записи. Возвращает количество записанных байт.
     */
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun putStream(
        inputStream: InputStream,
        targetPath: String,
        overwriteIfExists: Boolean = false,
        writingCallback: StreamWritingCallback? = null,
        finishCallback: StreamFinishCallback? = null,
    )


    // TODO: не нужна
    // FIXME: добавить аннотацию в реализации
    @Throws(IOException::class, OperationUnsuccessfulException::class)
    fun fileExists(parentDirName: String, childName: String): Boolean


    // TODO: локальное удаление в корзину
    /**
     * Удаляет файл/папку.
     */
    @Throws(
        IOException::class,
        OperationUnsuccessfulException::class,
        OperationTimeoutException::class
    )
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
    fun deleteDirRecursively(basePath: String, dirName: String)


    // TODO: выделить в отдельный файл...
    sealed class CloudWriterException(message: String) : Exception(message)

    class OperationUnsuccessfulException(errorMsg: String) : CloudWriterException(errorMsg) {
        constructor(responseCode:Int, responseMessage: String) : this("$responseCode: $responseMessage")
    }

    class OperationTimeoutException(errorMsg: String) : CloudWriterException(errorMsg)


    companion object {
        const val DS = "/"

        fun composeFullPath(basePath: String, fileName: String): String {
            return "${basePath}${DS}${fileName}".stripMultiSlashes()
        }
    }
}