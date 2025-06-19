package com.github.aakumykov.cloud_writer_2.inheritance_probe.local

import android.os.Environment
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.inheritance_probe.CloudWriter2Tests
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import org.junit.Assert
import org.junit.Test
import java.io.File

abstract class LocalBase : CloudWriter2Tests() {

    override val virtualRootPath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath


    override val cloudWriter2: CloudWriter2
        get() = LocalCloudWriter2(virtualRootPath)


    @Test
    fun storage_writeability_check() = run {
        val dir = File(absoluteDirPath)
        step("Создаю каталог '$absoluteDirPath' не библиотечным методом") {
            Assert.assertTrue(dir.mkdir())
        }
        step("Проверяю, что он создался") {
            Assert.assertTrue(dir.exists())
        }
    }
}