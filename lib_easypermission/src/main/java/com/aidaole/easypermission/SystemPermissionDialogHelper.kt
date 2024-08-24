package com.aidaole.easypermission

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.edit

/**
 * 这个类主要是为了提前获取系统是否会弹出系统权限弹窗,
 * 在不同的机型上可能表现不同, 比如:
 * 1. 华为只要拒绝过一次就不会再弹窗, 不能点back(同android原生)
 * 2. oppo,vivo 在权限弹窗之后可两次拒绝, 但是可以点back关闭
 */
object SystemPermissionDialogHelper {
    private const val TAG = "PermissionTopDialogHelper"
    const val SYS_DIALOG_WILL_SHOW = "SYS_DIALOG_WILL_SHOW"
    const val REJECT_FIRST = "REJECT_FIRST"

    fun getSystemDialogWillShow(activity: Activity, permission: String): Boolean {
        val permissionSystemDialogWillShow = "${SYS_DIALOG_WILL_SHOW}_${permission}"
        return getSpFile(activity).getBoolean(permissionSystemDialogWillShow, true)
    }

    fun writeSystemDialogWillShowNextTime(
        activity: Activity,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(
            TAG,
            "checkPermissionWillShowSystemDialogLastTime() called with: permissions = $permissions, grantResults = $grantResults"
        )
        Log.d(
            TAG,
            "checkPermissionWillShowSystemDialogLastTime: MANUFACTURER: ${Build.MANUFACTURER}, BRAND:${Build.BRAND}"
        )
        permissions.forEachIndexed { index, s ->
            val permission = permissions[index]
            val granted = grantResults[index]
            checkOnePermissionSystemDialogLastTime(activity, permission, granted)
        }
    }

    fun clearPermissionHistory(activity: Activity, permission: String) {
        val permissionSystemDialogWillShow = "${SYS_DIALOG_WILL_SHOW}_${permission}"
        val permissionFirstReject = "${REJECT_FIRST}_${permission}"
        getSpFile(activity).edit {
            remove(permissionSystemDialogWillShow)
            remove(permissionFirstReject)
            commit()
        }
    }

    private fun getSpFile(context: Context): SharedPreferences {
        return context.getSharedPreferences("PermissionDialogHelper", Context.MODE_PRIVATE)
    }

    private fun putSp(activity: Activity, key: String, value: Boolean) {
        getSpFile(activity).edit {
            putBoolean(key, value)
            commit()
        }
    }

    private fun putPermissionSystemDialogWillShow(activity: Activity, permission: String, willShow: Boolean) {
        val permissionSystemDialogWillShow = "${SYS_DIALOG_WILL_SHOW}_${permission}"
        putSp(activity, permissionSystemDialogWillShow, willShow)
        Log.d(TAG, "putPermissionSystemDialogWillShow: 写入${permissionSystemDialogWillShow} = $willShow")
    }

    private fun getFirstReject(activity: Activity, permission: String): Boolean {
        val permissionFirstReject = "${REJECT_FIRST}_${permission}"
        return getSpFile(activity).getBoolean(permissionFirstReject, false)
    }

    private fun putFirstReject(activity: Activity, permission: String, isRejected: Boolean) {
        val permissionFirstReject = "${REJECT_FIRST}_${permission}"
        putSp(activity, permissionFirstReject, isRejected)
        Log.d(TAG, "checkOnePermissionWillShowSystemDialogNextTime: 写入${permissionFirstReject} = $isRejected")
    }

    private fun checkOnePermissionSystemDialogLastTime(
        activity: Activity, permission: String, granted: Int
    ) {
        if (granted == PackageManager.PERMISSION_GRANTED) {
            clearPermissionHistory(activity, permission)
            return
        }
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                activity, permission
            ) && getFirstReject(activity, permission)
        ) {
            Log.d(TAG, "checkOnePermissionSystemDialogLastTime: 下一次系统不会弹")
            putPermissionSystemDialogWillShow(activity, permission, false)
        } else {
            if (isHuaweiDevice()) {
                Log.d(TAG, "checkOnePermissionSystemDialogLastTime: 华为直接处理, 下一次系统不会弹")
                putPermissionSystemDialogWillShow(activity, permission, false)
            } else {
                putPermissionSystemDialogWillShow(activity, permission, true)
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    putFirstReject(activity, permission, true)
                }
            }
        }
    }

    private fun isHuaweiDevice(): Boolean {
        return Build.MANUFACTURER.equals("Huawei", ignoreCase = true) || Build.BRAND.equals("Huawei", ignoreCase = true)
    }
}