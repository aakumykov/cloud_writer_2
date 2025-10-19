package com.github.aakumykov.cloud_writer_2.special

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.File

class CopyFromStorageToStorage : TestBase() {

    val cloudWriter2: CloudWriter2
        get() = LocalCloudWriter2(storageRootDir.absolutePath)


    @Test
    fun from_downloads_to_photo_dir_file_copy() {
        repeat(100) {

            val sFileName = randomName
            val tFileName = randomName
            val data = randomBytes

            val tFilePath = cloudWriter2.virtualRootPlus(tFileName)

            val sFile = createFileIn(downloadsDir, sFileName, data)
            val tFile = File(tFilePath)

            sFile.inputStream().use { inputStream ->
                runBlocking {
                    cloudWriter2.putStream(
                        inputStream = inputStream,
                        targetPath = tFilePath,
                        isRelative = false,
                        overwriteIfExists = true,
                    )
                }
            }

            // Проверяю, что исходный файл не изменился.
            Assert.assertTrue(sFile.exists())
            Assert.assertEquals(
                sFile.readBytes().joinToString(),
                data.joinToString()
            )

            // Проверяю, что содержимое конечного файла соответствует "исходным" данным.
            Assert.assertTrue(tFile.exists())
            Assert.assertEquals(
                tFile.readBytes().joinToString(),
                data.joinToString()
            )

            // Проверяю, что данные исходного и конечного файлов идентичны.
            Assert.assertEquals(
                sFile.readBytes().joinToString(),
                tFile.readBytes().joinToString()
            )
        }
    }


}