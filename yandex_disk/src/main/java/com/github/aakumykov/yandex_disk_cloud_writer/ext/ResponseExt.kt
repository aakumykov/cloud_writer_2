package com.github.aakumykov.yandex_disk_cloud_writer.ext

import com.github.aakumykov.cloud_writer.CloudWriterException
import okhttp3.Response

val Response.toCloudWriterException: CloudWriterException
    get() {
    return CloudWriterException("${code}: $message")
}