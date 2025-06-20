package com.github.aakumykov.cloud_writer_2.local


class LocalAbsoluteBase : LocalBase(), Props {

    override val isRelative: Boolean = false

    override val dirPath: String
        get() = absoluteDirPath

    override val deepDirPath: String
        get() = deepDirAbsolutePath

    override val filePath: String
        get() = absoluteFilePath
}