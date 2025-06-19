package com.github.aakumykov.cloud_writer

import java.io.IOException

interface CloudWriter2 {

    /**
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
    suspend fun deleteEmptyDir(dirPath: String, isRelative: Boolean): String


    companion object {
        /**
         * Directory separator.
         */
        const val DS = "/"
    }
}