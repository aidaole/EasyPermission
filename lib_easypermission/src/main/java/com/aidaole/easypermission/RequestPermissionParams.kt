package com.aidaole.easypermission

import android.view.View
import androidx.fragment.app.FragmentActivity

/**
 * 请求你权限参数
 */
data class RequestPermissionParams(
    val activity: FragmentActivity,
    val requestPermissions: Array<String>,
    val descView: View? = null,
    val dialogParams: RequestDialogParams? = null
)