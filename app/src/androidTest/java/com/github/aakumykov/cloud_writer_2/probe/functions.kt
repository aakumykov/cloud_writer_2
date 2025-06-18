package com.github.aakumykov.cloud_writer_2.probe

import java.util.UUID

val randomName: String
    get() = UUID.randomUUID().toString().split("-").first()