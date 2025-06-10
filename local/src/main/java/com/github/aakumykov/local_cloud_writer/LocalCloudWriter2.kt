package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import java.io.File

class LocalCloudWriter2(
    override val virtualRootPath: String,
)
    : CloudWriter2
{
    override suspend fun createDir(path: String, isRelative: Boolean): String {
        return if (isRelative) createRelativeDir(path)
        else createAbsoluteDir(path)
    }

    override fun createDirIfNotExist(path: String, isRelative: Boolean): String {
        val fullPath = if (isRelative) virtualRootPlus(path) else path
        return if (!fileExistsAbsolute(fullPath)) createAbsoluteDir(fullPath)
        else fullPath
    }

    /**
     * @return Путь к созданному каталогу: абсолютный или относительный,
     * в зависимости от режима запуска.
     */
    override fun createDeepDir(path: String, isRelative: Boolean): String {

        // Так как идёт пошаговое создание
        // каталогов из пути вглубь,
        // то нужно отрезать от него "системную"
        // незаписываемую часть.

        val pathToOperate = path.replace(Regex("^${virtualRootPath}/+"),"")

        return iterateOverDirsInPathFromRoot(pathToOperate) { partialPath ->
            createRelativeDir(partialPath)
        }.let {
            path
        }
    }


    /**
     * Проходит путь [path] от корня в грубину, вызывая действие
     * [action] на каждой итерации.
     * @return Первоначальное значение [path].
     *
     * Пример:
     *
     * Если [path] = /dir1/dir2/dir3, то блок [action] будет вызван три раза с параметрами:
     * 1) "dir1"
     * 2) "dir1/dir2"
     * 3) "dir1/dir2/dir3"
     */
    private fun iterateOverDirsInPathFromRoot(path: String, action: (String) -> Unit): String {
        return path
            .split(CloudWriter2.DS)
            .filterNot { "" == it }
            .reduce { acc, s ->
                action(acc)
                acc + CloudWriter2.DS + s
            }.let { tailDir: String ->
                action(tailDir)
                path
            }
    }

    override fun createDeepDirIfNotExists(path: String, isRelative: Boolean): String {
        return if (isRelative) createRelativeDeepDirIfNotExists(path)
        else createAbsoluteDeepDirIfNotExists(path)
    }

    private fun createRelativeDeepDirIfNotExists(path: String): String {
        return createAbsoluteDeepDirIfNotExists(virtualRootPlus(path))
    }

    private fun createAbsoluteDeepDirIfNotExists(path: String): String {
        return iterateOverDirsInPathFromRoot(path) { partialDeepPath ->
            if (!fileExistsAbsolute(partialDeepPath))
                createAbsoluteDir(partialDeepPath)
        }
    }


    private fun createRelativeDir(path: String): String {
        return createAbsoluteDir(virtualRootPlus(path))
    }


    private fun createAbsoluteDir(path: String): String {
        return with(File(path)) {
            if (!mkdir())
                throw CloudWriterException("Dir '$path' was not created")
            path
        }
    }

    override suspend fun fileExists(path: String, isRelative: Boolean): Boolean {
        return if (isRelative) fileExistsRelative(path)
        else fileExistsAbsolute(path)
    }

    private fun fileExistsRelative(path: String): Boolean {
        return fileExistsAbsolute(virtualRootPlus(path))
    }

    private fun fileExistsAbsolute(path: String): Boolean {
        return File(path).exists()
    }
}