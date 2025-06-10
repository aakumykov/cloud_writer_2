package com.github.aakumykov.cloud_writer_2

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.github.aakumykov.cloud_writer_2", appContext.packageName)
    }

    suspend fun fetchData(): String {
        delay(1000L)
        return "Hello world"
    }

    @Test
    fun dataShouldBeHelloWorld() = runTest {
        val data = fetchData()
        Assert.assertEquals("Hello world", data)
    }
}