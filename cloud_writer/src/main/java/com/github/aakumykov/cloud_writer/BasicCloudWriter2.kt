package com.github.aakumykov.cloud_writer

import com.github.aakumykov.cloud_writer.extensions.stripMultiSlashes

abstract class BasicCloudWriter2 : CloudWriter2 {

    abstract override val virtualRootPath: String

    override fun virtualRootPlus(vararg pathParts: String): String {
        return mutableListOf(virtualRootPath)
            .apply {
                if (!addAll(pathParts.toList()))
                    throw RuntimeException("Cannot add path parts to virtual root path.")
            }
            .joinToString(CloudWriter2.DS)
            .stripMultiSlashes()
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
    protected suspend fun iterateOverDirsInPathFromRoot(path: String, action: suspend (String) -> Unit): String {
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
}