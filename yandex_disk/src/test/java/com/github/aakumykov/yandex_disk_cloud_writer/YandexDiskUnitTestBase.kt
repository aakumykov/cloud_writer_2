package com.github.aakumykov.yandex_disk_cloud_writer

import java.io.File
import java.util.Properties

open class YandexDiskUnitTestBase(private val localPropertiesAuthIdKey: String) {

    private val localPropertiesFilePath = "../local.properties"

    protected val yandexAuthId: String by lazy {
        val localPropertiesFile = File(localPropertiesFilePath)
        Properties().apply {
            load(localPropertiesFile.inputStream())
        }.getProperty(localPropertiesAuthIdKey)
    }
}