package com.github.aakumykov.utils

import java.util.UUID
import kotlin.random.Random

val random: Random = Random

val randomId: String get() = UUID.randomUUID().toString()

val shortUUID: String get() = randomId.split("-").first()

val randomInt3: Int get() = random.nextInt(1,4)
val randomInt10: Int get() = random.nextInt(1,11)
val randomInt100: Int get() = random.nextInt(1,101)

