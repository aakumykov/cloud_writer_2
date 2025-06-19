package com.github.aakumykov.cloud_writer_2.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LocalAbsoluteTest : LocalBase() {

    override val isRelative: Boolean = false

    override val dirPath: String
        get() = absoluteDirPath

    override val deepDirPath: String
        get() = deepDirAbsolutePath
}
