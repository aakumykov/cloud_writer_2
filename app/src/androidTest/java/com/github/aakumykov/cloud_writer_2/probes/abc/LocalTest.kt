package com.github.aakumykov.cloud_writer_2.probes.abc

import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer_2.common.StorageAccessTestCase
import com.github.aakumykov.cloud_writer_2.common.randomName
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

open class BaseTest(
    protected val cloudWriter2: CloudWriter2
) : StorageAccessTestCase() {

}


val dirName: String = randomName


val localVirtualRootPath: String
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

val localDirPath: String
    get() = File(localVirtualRootPath, dirName).absolutePath


val cloudlVirtualRootPath: String = "/"

val cloudDirPath: String
    get() = File(cloudlVirtualRootPath, dirName).absolutePath



@RunWith(AndroidJUnit4::class)
class LocalTest : BaseTest(LocalCloudWriter2(localVirtualRootPath)) {

    @Test
    fun creates_local_dir() = runTest {
        Assert.assertEquals(
            localDirPath,
            cloudWriter2.createDir(dirName, true)
        )
    }
}

@RunWith(AndroidJUnit4::class)
class YandexTest : BaseTest(YandexDiskCloudWriter2(
    authToken = "y0__xDZlpzOBxjblgMg4KXJtxMwwO29hAgKumNHLZ0BJzJ2rsqZRHYUSSIlEw",
    virtualRootPath = cloudlVirtualRootPath
)) {

    @Test
    fun creates_yandex_dir() = runTest {
        Assert.assertEquals(
            cloudDirPath,
            cloudWriter2.createDir(dirName, true)
        )
    }
}