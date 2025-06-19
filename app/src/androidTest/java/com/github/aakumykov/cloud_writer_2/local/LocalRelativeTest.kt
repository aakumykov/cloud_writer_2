package com.github.aakumykov.cloud_writer_2.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LocalRelativeTest : LocalBase() {

    override val isRelative: Boolean = true

    override val dirPath: String
        get() = dirName

    override val deepDirPath: String
        get() = deepDirName

    override val filePath: String
        get() = fileName

    /* override fun creates_deep_dir() = run {
        step("Создаю глубокий каталог '$deepDirPath'") {
            runBlocking {
                Assert.assertEquals(
                    deepDirRelativePath,
                    cloudWriter2.createDeepDir(deepDirPath, isRelative)
                )
            }
        }
    }*/
}
