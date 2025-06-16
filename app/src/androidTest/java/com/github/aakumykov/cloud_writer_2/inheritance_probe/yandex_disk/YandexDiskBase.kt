package com.github.aakumykov.cloud_writer_2.inheritance_probe.yandex_disk

import android.util.Log
import com.github.aakumykov.cloud_writer_2.R
import com.github.aakumykov.cloud_writer_2.inheritance_probe.CloudWriter2Base
import kotlinx.coroutines.runBlocking

abstract class YandexDiskBase : CloudWriter2Base()  {

    protected val yandexAuthToken: String
        get() = device.targetContext.getString(R.string.yandex_disk_auth_token_for_tests)

    override fun checkFileExistsNative(path: String, isRelative: Boolean): Boolean {
        Log.w(TAG, "Метод не надёжен, так как использует функцию из библиотеки, которую призван проверять.")
        return runBlocking {
            cloudWriter2.fileExists(path, isRelative)
        }
    }

    companion object {
        val TAG: String = YandexDiskBase::class.java.simpleName
    }
}