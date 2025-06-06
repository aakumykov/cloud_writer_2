package com.github.aakumykov.extensions

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.fragment.app.FragmentActivity
import com.github.aakumykov.MainActivity.Companion.YANDEX_AUTH_TOKEN

val FragmentActivity.defaultSharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

val FragmentActivity.yandexAuthToken: String? get() {
    return defaultSharedPreferences.getString(YANDEX_AUTH_TOKEN, null)
}