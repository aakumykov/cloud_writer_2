package com.github.aakumykov.cloud_writer_2

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StepsProbe : TestCase() {

    //
    // Последующие шаги не выполняются, если возникла ошибка,
    // поэтому нет нужды делать их вложенными.
    //

    @Test
    fun qwerty() = run {
        step("1") {
            Assert.assertTrue(false)
        }
        step("2") {
            Assert.assertFalse(true)
        }
    }
}