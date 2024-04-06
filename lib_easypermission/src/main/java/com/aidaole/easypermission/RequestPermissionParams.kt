package com.aidaole.easypermission

import android.view.View
import androidx.fragment.app.FragmentActivity

data class RequestPermissionParams(
    val activity: FragmentActivity,
    val descView: View? = null,
    val requestPermissions: Array<String>
)