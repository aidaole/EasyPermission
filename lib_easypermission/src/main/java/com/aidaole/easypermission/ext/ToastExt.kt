package com.aidaole.easypermission.ext

import android.content.Context
import android.widget.Toast

class ToastExt

internal fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}