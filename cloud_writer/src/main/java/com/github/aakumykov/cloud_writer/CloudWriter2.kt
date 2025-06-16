package com.github.aakumykov.cloud_writer

import java.io.IOException

interface CloudWriter2 {

    @Throws(IOException::class, CloudWriterException::class)
    suspend fun fileExists(path: String, isRelative: Boolean): Boolean


    /**
     * Создаёт каталог по указанному пути.
     * @param path Путь к создаваемому каталогу.
     * @param isRelative Признак того, что [path] является относительным.
     * @return абсолютный путь к созданному каталогу.
     * @throws CloudWriterException если каталог не создан, в том числе по
     * причине того, что он уже существует. Для работы без ошибки в случае
     * наличия каталога, используйте метод [createDirIfNotExist].
     */
    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDir(path: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDirIfNotExist(path: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDeepDir(path: String, isRelative: Boolean): String


    @Throws(IOException::class, CloudWriterException::class)
    suspend fun createDeepDirIfNotExists(path: String, isRelative: Boolean): String


    @Deprecated("Переименовать в absolutePathFor()")
    fun virtualRootPlus(vararg pathParts: String): String


    companion object {
        /**
         * Directory separator.
         */
        const val DS = "/"
    }
}