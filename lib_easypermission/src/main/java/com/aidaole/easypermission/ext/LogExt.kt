package com.aidaole.easypermission.ext

import android.util.Log

class LogExt

internal fun String.logi(tag: String) {
    Log.i(tag, this)
}