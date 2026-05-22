package com.github.aakumykov.cloud_writer_2_demo.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.showToast(@StringRes strRes: Int) {
    showToast(getString(strRes))
}