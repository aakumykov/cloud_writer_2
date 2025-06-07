package com.github.aakumykov.local_cloud_writer

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer.CloudWriterException
import java.io.File

class LocalCloudWriter2(
    private val virtualRootPath: String,
)
    : CloudWriter2
{
    override fun createDir(path: String, isRelative: Boolean): String {
        return if (isRelative) createRelativeDir(path)
        else createAbsoluteDir(path)
    }

    override fun createDirIfNotExist(path: String, isRelative: Boolean): String {
        val fullPath = if (isRelative) virtualRootPlus(path) else path
        return if (!fileExistsAbsolute(fullPath)) createAbsoluteDir(fullPath)
        else fullPath
    }

    override fun createDeepDir(path: String, isRelative: Boolean): String {

        // Так как идёт пошаговое создание
        // каталогов из пути вглубь,
        // то нужно отрезать от него "системную"
        // незаписываемую часть.

        val pathToOperate = (if (isRelative) path
        else path.replace(virtualRootPath, ""))
            .replaceFirst(Regex("^/"),"")

        return iterateDeepOnPathWithAction(pathToOperate) { partialPath ->
            createRelativeDir(partialPath)
        }
    }


    /**
     * @return
     */
    private fun iterateDeepOnPathWithAction(path: String, action: (String) -> Unit): String {
        return path
            .split(CloudWriter2.DS)
            .reduce { acc, s ->
                action(acc)
                acc + CloudWriter2.DS + s
            }.also { tailDir: String ->
                action(tailDir)
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
        return iterateDeepOnPathWithAction(path) { partialDeepPath ->
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

    override fun fileExists(path: String, isRelative: Boolean): Boolean {
        return if (isRelative) fileExistsRelative(path)
        else fileExistsAbsolute(path)
    }

    private fun fileExistsRelative(path: String): Boolean {
        return fileExistsAbsolute(virtualRootPlus(path))
    }

    private fun fileExistsAbsolute(path: String): Boolean {
        return File(path).exists()
    }


    private fun virtualRootPlus(vararg pathParts: String): String {
        return mutableListOf(virtualRootPath).apply {
            if (!addAll(pathParts.toList()))
                throw RuntimeException("Cannot add path parts to virtual root path.")
        }.joinToString(CloudWriter2.DS)
    }
}