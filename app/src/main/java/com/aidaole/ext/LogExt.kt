package com.aidaole.ext

import android.util.Log

class LogExt

internal fun String.logi(tag: String) {
    Log.i(tag, this)
}