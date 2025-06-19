package com.github.aakumykov.cloud_writer_2.yandex_disk

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YandexDiskRelativeTest : YandexDiskBase() {

    override val isRelative: Boolean = true

    override val dirPath: String
        get() = dirName

    override val deepDirPath: String
        get() = deepDirName

    override val filePath: String
        get() = fileName
}