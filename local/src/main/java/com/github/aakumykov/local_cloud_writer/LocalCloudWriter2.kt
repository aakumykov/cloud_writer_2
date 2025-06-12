package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.BasicCloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import java.io.File

class LocalCloudWriter2(
    override val virtualRootPath: String,
)
    : BasicCloudWriter2()
{
    override suspend fun createDir(path: String, isRelative: Boolean): String {
        return if (isRelative) createRelativeDir(path)
        else createAbsoluteDir(path)
    }

    override suspend fun createDirIfNotExist(path: String, isRelative: Boolean): String {
        return if (isRelative) createAbsoluteDirIfNotExists(virtualRootPlus(path))
        else createAbsoluteDirIfNotExists(path)
    }

    private fun createAbsoluteDirIfNotExists(path: String): String {
        return if (fileExistsAbsolute(path)) path
        else createAbsoluteDir(path)
    }

    /**
     * @return Путь к созданному каталогу: абсолютный или относительный,
     * в зависимости от режима запуска.
     */
    override suspend fun createDeepDir(path: String, isRelative: Boolean): String {

        val pathToOperate = path.replace(Regex("^${virtualRootPath}/+"),"")

        return iterateOverDirsInPathFromRoot(pathToOperate) { partialPath ->
            createDirIfNotExist(partialPath, true)
        }.let {
            path
        }
    }


    override suspend fun createDeepDirIfNotExists(path: String, isRelative: Boolean): String {
        return if (isRelative) createAbsoluteDeepDirIfNotExists(virtualRootPlus(path))
        else createAbsoluteDeepDirIfNotExists(path)
    }

    private suspend fun createAbsoluteDeepDirIfNotExists(path: String): String {
        return if (fileExistsAbsolute(path)) path
        else iterateOverDirsInPathFromRoot(path) { partialDeepPath ->
            createDirIfNotExist(partialDeepPath, true)
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
        return if (isRelative) fileExistsAbsolute(virtualRootPlus(path))
        else fileExistsAbsolute(path)
    }

    private fun fileExistsAbsolute(path: String): Boolean {
        return File(path).exists()
    }
}