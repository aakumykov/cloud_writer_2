package com.github.aakumykov.cloud_writer_2.common

import java.util.UUID

val randomName: String
    get() = shortId


val randomId: String
    get() = UUID.randomUUID().toString()


val shortId: String
    get() = randomId.split("-").first()

