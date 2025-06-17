package com.github.aakumykov.cloud_writer_2.inheritance_probe.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LocalRelativeTest : LocalBase() {

    override val isRelative: Boolean = true

    override val dirPath: String
        get() = dirName

    override val deepDirPath: String
        get() = deepDirName
}
