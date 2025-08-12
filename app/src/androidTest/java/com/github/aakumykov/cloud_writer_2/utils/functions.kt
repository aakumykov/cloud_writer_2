package com.github.aakumykov.cloud_writer_2.utils

import com.github.aakumykov.cloud_writer.CloudWriter2
import com.github.aakumykov.cloud_writer.extensions.stripMultiSlashes
import java.util.UUID

val randomName: String
    get() = shortId


val randomId: String
    get() = UUID.randomUUID().toString()


val shortId: String
    get() = randomId.split("-").first()


fun aggregateNamesToPath(vararg dirName: String): String {
    return dirName
        .filterNot { it.isEmpty() }
        .joinToString(CloudWriter2.DS)
        .stripMultiSlashes()
}