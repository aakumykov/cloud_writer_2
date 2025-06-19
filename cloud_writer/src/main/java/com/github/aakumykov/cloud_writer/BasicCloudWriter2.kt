package com.github.aakumykov.cloud_writer

abstract class BasicCloudWriter2 : CloudWriter2 {

    protected abstract val virtualRootPath: String

    override fun virtualRootPlus(vararg pathParts: String): String {
        return mutableListOf(virtualRootPath)
            .apply {
                if (!addAll(pathParts.toList()))
                    throw RuntimeException("Cannot add path parts to virtual root path.")
            }
            .joinToString(CloudWriter2.DS)
            .replace(Regex("/+"),"/")
    }


    override suspend fun deleteDeepEmptyDir(dirPath: String, isRelative: Boolean): String {
        return if (isRelative) deleteDeepEmptyDirReal(virtualRootPlus(dirPath), true)
        else deleteDeepEmptyDirReal(dirPath, false)
    }


    private suspend fun deleteDeepEmptyDirReal(dirPath: String, isRelative: Boolean): String {
        return iterateOverDirsInPathFromRoot(dirPath, true) { pathPart: String ->
            deleteEmptyDir(pathPart, isRelative)
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
    protected suspend fun iterateOverDirsInPathFromRoot(
        path: String,
        reverseDirection: Boolean = false,
        action: suspend (String) -> Unit
    ): String {
        return path
            .split(CloudWriter2.DS)
            .filterNot { "" == it }
            .apply {
                if (reverseDirection)
                    reversed()
            }
            .reduce { acc, s ->
                action(acc)
                acc + CloudWriter2.DS + s
            }.let { tailDir: String ->
                action(tailDir)
                path
            }
    }
}