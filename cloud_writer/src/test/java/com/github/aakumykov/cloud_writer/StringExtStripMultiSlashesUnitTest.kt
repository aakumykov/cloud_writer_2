package com.github.aakumykov.cloud_writer

import com.github.aakumykov.cloud_writer.extensions.stripMultiSlashes
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class StringExtStripMultiSlashesUnitTest {

    private val random: Random get() = Random

    private val multipleSlashes: String
        get() = "/".repeat(random.nextInt(3,10))


    private val zeroLengthString = ""
    private val emptyString = "     "
    private val simpleString = "qwerty"

    private val startingSingleSlashString = "qwerty"
    private val middleSingleSlashString = "qwe/rty"
    private val endingSingleSlashString = "qwerty/"

    private val startingDoubleSlashString = "//qwerty"
    private val middleDoubleSlashString = "qwe//rty"
    private val endingDoubleSlashString = "qwerty//"

    private val startingMultipleSlashString = "${multipleSlashes}qwerty"
    private val middleMultipleSlashString = "qwerty${multipleSlashes}qwerty"
    private val endingMultipleSlashString = "qwerty${multipleSlashes}"

    // TODO: строка ещё и с пробелами

    @Test
    fun when_string_is_zero_length_when_result_equals() {
        assertEquals(zeroLengthString.stripMultiSlashes(),zeroLengthString)
    }

    @Test
    fun when_string_is_empty_then_reqult_equals() {
        assertEquals(emptyString.stripMultiSlashes(), emptyString)
    }

//    @Test
//    fun when_one_starting_slash
}