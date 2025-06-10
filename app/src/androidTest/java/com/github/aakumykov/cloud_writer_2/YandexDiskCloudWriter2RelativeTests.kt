package com.github.aakumykov.cloud_writer_2

import com.github.aakumykov.cloud_writer.CloudWriterException
import com.github.aakumykov.yandex_disk_cloud_writer.YandexDiskCloudWriter2
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class YandexDiskCloudWriter2RelativeTests : TestCase() {

    private val yandexCloudWriter by lazy {
        YandexDiskCloudWriter2(
            authToken = yandexAuthToken,
            virtualRootPath = ""
        )
    }

    private val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)


    /**
     * API-токен:
     * 10) проверка наличия API-токена (в local.properties, через
     * строковый ресурс "yandex_disk_auth_token_for_tests", генерируемый в build.gradle(app)
     * [yandex_auth_token_for_tests_is_not_empty].
     * 20) ошибка при создании с неверным API-токеном [throws_exception_on_operate_without_api_token].
    */

    @Test
    fun yandex_auth_token_for_tests_is_not_empty() {
        device.targetContext.resources.apply {
            yandexAuthToken.apply {
                Assert.assertTrue(this.isNotEmpty())
            }
        }
    }


    @Test
    fun throws_exception_on_operate_without_api_token() = run {
        Assert.assertThrows(Exception::class.java) {
            runBlocking {
                YandexDiskCloudWriter2(authToken = "")
                    .createDir(randomName, true)
            }
        }
    }



    /**
    Создание каталога:
    10) успешное создание [creates_dir];
    20) ошибка при создании уже существующего [throws_exception_on_second_dir_creation_with_same_name];
     */

    @Test
    fun creates_dir() = run {
        val dirName = randomName
        runBlocking {
            Assert.assertEquals(
                dirName,
                create_dir_with_name(dirName)
            )
        }
    }


    @Test
    fun throws_exception_on_second_dir_creation_with_same_name() = run {

        val dirName = randomName

        runBlocking {

            create_dir_with_name(dirName)

            Assert.assertThrows(CloudWriterException::class.java) {
                runBlocking {
                    create_dir_with_name(dirName)
                }
            }
        }
    }


    /**
     * Проверка существования каталога:
     * 10) возврат true, если каталог есть;
     * 20) возврат false, если каталога нет;
     */

    @Test
    fun returns_true_or_false_if_dir_exists_or_not() {
        runBlocking {

            val dirName = randomName
            println("dirName: $dirName")

            Assert.assertFalse(
                yandexCloudWriter.fileExists(dirName, true)
            )

            create_dir_with_name(dirName)

            Assert.assertTrue(
                yandexCloudWriter.fileExists(dirName, true)
            )
        }
    }



    /**
     * @return Имя созданного каталога.
     */
    private suspend fun create_dir_with_name(name: String): String {
        return yandexCloudWriter
            .createDir(name, false)
            .also {
                Assert.assertEquals(name, it)
            }.let {
                name
            }
    }


    /*@Test
    fun do_not_creates_dir_if_cancelled() = runTest {

        // TODO: вынести в константу
        val cancellationTimeoutMs: Long = 100
        val dirName = randomName

        try {
            yandexCloudWriter
                .createDir(dirName, false)
                .also {
                    Assert.assertEquals(dirName, it)
                }
        } catch (e: Exception) {
            println(e.errorMsg)
        }

        launch (Dispatchers.IO) {
            delay(cancellationTimeoutMs)
            cancel(CancellationException("Прервано после $cancellationTimeoutMs мс"))
        }

        *//*CoroutineScope(Dispatchers.IO).launch {

            val dirName = randomName
            try {
                yandexCloudWriter
                    .createDir(dirName, false)
                    .also {
                        Assert.assertEquals(dirName, it)
                    }
            } catch (e: Exception) {
                println(e.errorMsg)
            }

        }.also {
            runBlocking {
                delay(cancellationTimeoutMs)
                it.cancel(CancellationException("Прервано после $cancellationTimeoutMs мс"))
            }
        }*//*
    }*/



}