package com.github.aakumykov.cloud_writer

import java.io.IOException
import java.io.InputStream

interface CloudWriter2 {

    val virtualRootPath: String

    /**
     * Соединяет виртауальный корень с произвольным количеством
     * дополнительных путей, в результате чего получается путь к более глубокому каталогу.
     *
     * Не добавляет к результату конечный слеш, так что, если:
     * - свойство [virtualRootPath] = "/root/path";
     * - аргументом - пустая строка (""),
     * результатом будет "/root/path", а не "/root/path/".
     *
     * Удаляет двойные и множественные слеши.
     *
     * FIXME: что делать с элементами ".."?
     *  Это угроза безопасности!
     *
     * Служебный метод, для внутреннего использования другими методами.
     */
    fun virtualRootPlus(vararg pathParts: String): String



    @Throws(IOException::class, CloudWriterException::class)
    suspend fun fileExists(dirPath: String, isRelative: Boolean): Boolean


    /**
     * Создаёт каталог по указанному пути.
     * @param dirPath Путь к создаваемому каталогу.
     * @param isRelative Признак того, что [dirPath] является относительным.
     * @return абсолютный путь к созданному каталогу.
     * @throws CloudWriterException если каталог не создан, в том числе по
     * причине того, что он уже существует. Для работы без ошибки в случае
     * наличия каталога, используйте метод [createDirIfNotExist].
     */
    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDir(dirPath: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDirIfNotExist(dirPath: String, isRelative: Boolean): String



    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDeepDir(dirPath: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDeepDirIfNotExists(dirPath: String, isRelative: Boolean): String



    /**
     * Удаляет пустой каталог.
     * В случае, если производится попытка удаления непустого каталога,
     * поведение зависит от реализации:
     * * локальная файловая система - будет выброшено исключение, каталог не будет удалён.
     * * облако - каталог будет отправлен на удаление в асинхронном режиме, без сигнала о завершении.
     * @return Абсолютный путь к удалённому каталогу.
     */
    suspend fun deleteFileOrEmptyDir(dirPath: String, isRelative: Boolean): String


    /**
     * @param writingCallback Колбек, возвращающий количество записанных байт.
     * @param finishCallback Колбек, возвращающий количество прочитанных и записанных байт.
     */
    @Throws(IOException::class, CloudWriterException::class)
    suspend fun putStream(
        inputStream: InputStream,
        targetPath: String,
        isRelative: Boolean,
        overwriteIfExists: Boolean = false,
        readingCallback: ((Long) -> Unit)? = null,
        writingCallback: ((Long) -> Unit)? = null,
        finishCallback: ((Long,Long) -> Unit)? = null,
    )



    /**
     * Переименовывает файл или пустой каталог.
     * Не работает с локальным хранилищем, если целевой
     * файл находится на физическом разделе, отличном
     * от исходного.
     */
    @Throws(IOException::class, CloudWriterException::class)
    suspend fun renameFileOrEmptyDir(
        fromPath: String,
        toPath: String,
        isRelative: Boolean,
        overwriteIfExists: Boolean = true
    ): Boolean



    companion object {
        /**
         * Directory separator.
         */
        const val DS = "/"
    }
}