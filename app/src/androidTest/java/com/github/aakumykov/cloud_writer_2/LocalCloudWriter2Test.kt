package com.github.aakumykov.cloud_writer_2

import android.os.Environment
import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.local_cloud_writer.LocalCloudWriter2
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

/*
LocalCloudWriter2 имеет два варианта запуска методов:
1) с относительным путём;
2) с абсолютным путём.
"Относительные" методы используют внутри себя "абсолютные", поэтому вторые
тестирую полностью, а первые только в том, в чём они отличаются.

createDir()
createDirIfNotExists()
createDeepDir()
createDeepDirIfNotExists()
 */
class LocalCloudWriter2Test : StorageAccessTestCase() {

    private val dirName: String = randomName
    private val basePath = downloadsPath
    private val dir = File(basePath,dirName)
    private val dirAbsolutePath = File(basePath,dirName).absolutePath

    private val localCloudWriter2 by lazy { LocalCloudWriter2(basePath) }

    private val downloadsPath: String
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath


    @Before
    fun check_dir_not_exists() {
        Assert.assertFalse(dir.exists())
    }

    @After
    fun deleting_dir() {
        dir.deleteRecursively()
    }


    @Test
    fun storage_writeability_check() = run {
        step("Создаю каталог '$dirAbsolutePath' не библиотечным методом") {
            Assert.assertTrue(dir.mkdir())
        }
        step("Проверяю, что он создался") {
            Assert.assertTrue(dir.exists())
        }
    }


    //
    // Проверки, методов создания по абсолютному пути.
    //

    @Test
    fun creates_dir() = run {
        step("Создаю каталог") {
            localCloudWriter2.createDir(dirAbsolutePath, false)
        }
        step("Проверяю, что он существует") {
            Assert.assertTrue(dir.exists())
        }
    }


    @Test
    fun returns_path_to_created_dir() = run {
        step("Возвращает путь к созданному каталогу") {
            Assert.assertEquals(
                dirAbsolutePath,
                localCloudWriter2.createDir(dirAbsolutePath, isRelative = false)
            )
        }
    }

    @Test
    fun throws_exception_if_dir_exists() = run {
        creates_dir()
        step("Пробую создать каталог '$dirAbsolutePath' ещё раз"){
            Assert.assertThrows(CloudWriterException::class.java) {
                localCloudWriter2.createDir(dirAbsolutePath, false)
            }
        }
    }

    @Test
    fun throws_exception_on_not_write_access() = run {

        val dir = File("/","123")
        val path = dir.absolutePath

        step("Проверяю, что каталога '$path' ВНЕЗАПНО нет") {
            Assert.assertFalse(dir.exists())
        }
        step("Проверяю, что бросается исключение при попытке его создать") {
            Assert.assertThrows(CloudWriterException::class.java) {
                localCloudWriter2.createDir(path, false)
            }
        }
    }


    @Test
    fun creates_dir_if_not_exists() = run {
        creates_dir()
        step("Не бросает исключения при повторном создании каталога условным методом") {
            Assert.assertEquals(
                dirAbsolutePath,
                localCloudWriter2.createDirIfNotExist(dirAbsolutePath, false)
            )
        }
    }


    @Test
    fun throws_exception_on_creating_deep_dir_with_not_deep_method() = run {

        val deepDirName = File(randomName, randomName).absolutePath
        val deepDir = File(basePath,deepDirName)
        val deepDirPath = deepDir.absolutePath

        step("Проверяю, что каталога '$deepDirPath' ещё нет") {
            Assert.assertFalse(deepDir.exists())
        }
        step("Проверяю, что бросает исключение при попытке создать"){
            Assert.assertThrows(CloudWriterException::class.java) {
                localCloudWriter2.createDir(deepDirPath, false)
            }
        }
    }


    @Test
    fun can_create_multi_level_dir() = run {

        val deepDir1 = File(dir, randomName)
        val deepDir2 = File(deepDir1, randomName)
        val deepDir3 = File(deepDir2, randomName)

        val deepDir = deepDir3
        val deepDirPath = deepDir.absolutePath

        step("Проверяю, что глубокого каталога ещё нет") {
            Assert.assertFalse(deepDir.exists())
        }
        step("Создаю глубокий каталог '$deepDirPath'") {
            localCloudWriter2.createDeepDir(deepDirPath, false)
        }
        step("Проверяю, что глубокий каталог создан") {
            Assert.assertTrue(deepDir.exists())
        }
    }
}