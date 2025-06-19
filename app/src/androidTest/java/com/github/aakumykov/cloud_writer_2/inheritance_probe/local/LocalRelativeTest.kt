package com.github.aakumykov.cloud_writer_2.inheritance_probe.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LocalRelativeTest : LocalBase() {

    override val isRelative: Boolean = true

    override val dirPath: String
        get() = dirName

    override val deepDirPath: String
        get() = deepDirName


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
