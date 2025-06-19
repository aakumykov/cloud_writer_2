package com.github.aakumykov.cloud_writer_2.inheritance_probe.yandex_disk

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YandexDiskAbsoluteTest : YandexDiskBase() {

    override val isRelative: Boolean = false

    override val dirPath: String
        get() = absoluteDirPath

    override val deepDirPath: String
        get() = deepDirAbsolutePath
}