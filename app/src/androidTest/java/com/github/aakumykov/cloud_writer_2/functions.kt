package com.github.aakumykov.cloud_writer_2

import java.util.UUID

val randomName: String
    get() = randomId

val randomId: String
    get() = UUID.randomUUID().toString()
