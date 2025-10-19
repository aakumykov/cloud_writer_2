package com.github.aakumykov.cloud_writer_2.special

import java.util.UUID
import kotlin.random.Random

val randomName: String
    get() = UUID.randomUUID().toString()

val randomBytes: ByteArray
    get() = Random.nextBytes(10)