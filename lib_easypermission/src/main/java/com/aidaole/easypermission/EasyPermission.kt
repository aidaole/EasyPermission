package com.aidaole.easypermission

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.aidaole.easypermission.ext.logi
import com.aidaole.lib.easypermission.R
import java.util.Random
import java.util.UUID

/**
 * 请求权限主体
 */
object EasyPermission {
    private const val TAG = "EasyPermission"

    private val permissionRequests = mutableMapOf<Fragment, EasyPermissionItem>()

    /**
     * 检查是否所有权限
     *
     * @param activity
     * @param permissions
     * @return
     */
    fun checkPermissions(activity: FragmentActivity, permissions: Array<out String>): Boolean {
        val granted = getAllPermissionStates(activity, permissions)
        return isAllGranted(permissions, granted)
    }

    /**
     * 检查是否所有权限都是授予
     */
    fun isAllGranted(permissions: Array<out String>, granted: IntArray): Boolean {
        var allPermissionGranted = true
        granted.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                allPermissionGranted = false
            }
        }
        permissions.forEachIndexed { i, permission ->
            "checkPermissions: $permission -> ${granted[i]}".logi(TAG)
        }
        return allPermissionGranted
    }

    /**
     * 请求权限
     *
     * @param activity
     * @param text
     * @param permissions
     * @param onPermissionResult
     * @receiver
     */
    fun requestPermission(
        activity: FragmentActivity,
        text: String,
        permissions: Array<String>,
        onPermissionResult: (permissions: Array<out String>, granted: IntArray) -> Unit
    ) {
        val allPermissionGranted = checkPermissions(activity, permissions)
        if (allPermissionGranted) {
            "requestPermission-> 所有权限都有了".logi(TAG)
            onPermissionResult.invoke(
                permissions,
                IntArray(permissions.size).apply { fill(PackageManager.PERMISSION_GRANTED) })
            return
        }

        val permissionItem = makeRequestPermissionItem(activity, permissions, onPermissionResult)

        var shouldShowDialog = false
        permissions.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                shouldShowDialog = true
            }
        }
        if (shouldShowDialog) {
            showDialog(activity, permissions, ok = {
                openSystemSetting(activity)
            }, cancel = {
                permissionRequests.findByValue(permissionItem)?.let {
                    onResume(it)
                }
                onPermissionResult.invoke(
                    permissions,
                    IntArray(permissions.size).apply { fill(PackageManager.PERMISSION_DENIED) })
            })
            return
        }

        val tag = permissionItem.tag
        val permissionFragment = activity.supportFragmentManager.findFragmentByTag(tag)
        permissionRequests[permissionFragment]!!.requestSystem = true

        val descView = activity.layoutInflater.inflate(R.layout.permission_desc, null, false)
        descView.findViewById<TextView>(R.id.desc).text = text
        activity.findViewById<FrameLayout>(android.R.id.content).addView(
            descView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        val requestCode = Random(1000).nextInt(1000)
        ActivityCompat.requestPermissions(
            activity,
            permissions,
            requestCode
        )
    }

    /**
     * 请求sdcard权限，这个比较特殊，需要适配
     *
     * @param activity
     * @param text
     * @param requestPermissions
     * @param onPermissionResult
     * @receiver
     */
    fun requestStoragePermission(
        activity: FragmentActivity,
        text: String,
        requestPermissions: Array<String>,
        onPermissionResult: (permissions: Array<out String>, granted: IntArray) -> Unit
    ) {
        "requestStoragePermission-> ".logi(TAG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "requestStoragePermission-> api>30 请求所有文件权限".logi(TAG)
            if (!Environment.isExternalStorageManager()) {
                makeRequestPermissionItem(activity, requestPermissions, onPermissionResult)
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            } else {
                onPermissionResult.invoke(requestPermissions,
                    IntArray(requestPermissions.size).apply { fill(PackageManager.PERMISSION_GRANTED) })
            }
        } else {
            requestPermission(activity, text, requestPermissions) { permissions, granted ->
                onPermissionResult.invoke(permissions, granted)
            }
        }
    }


    private fun <K, V> Map<K, V>.findByValue(value: V): K? {
        return entries.find { it.value == value }?.key
    }

    private fun showDialog(
        activity: Activity, permissions: Array<String>, ok: () -> Unit, cancel: () -> Unit
    ) {
        AlertDialog.Builder(activity).setTitle("正在请求权限")
            .setMessage(permissions.joinToString(","))
            .setPositiveButton("OK") { dialog, which -> ok.invoke() }
            .setNegativeButton("Cancel", { dialog, which -> cancel.invoke() })
            .create()
            .show()
    }

    fun onResume(fragment: Fragment) {
        permissionRequests[fragment]?.let {
            removePermissionDescView(fragment)
            "onResume-> 删除fragmetn: ${fragment.tag}".logi(TAG)
            permissionRequests.remove(fragment)
            fragment.requireActivity().supportFragmentManager.beginTransaction().remove(fragment)
                .commit()
            it.onPermissionResult.invoke(
                it.permissions, getAllPermissionStates(fragment.requireActivity(), it.permissions)
            )
        } ?: run {
            "onResume-> 没有找到对应fragment: $fragment".logi(TAG)
        }
    }

    private fun removePermissionDescView(fragment: Fragment) {
        val contentView = fragment.requireActivity().findViewById<FrameLayout>(android.R.id.content)
        contentView?.removeView(contentView.findViewById(R.id.permission_layout))
    }

    private fun openSystemSetting(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    private fun getAllPermissionStates(
        activity: FragmentActivity,
        permissions: Array<out String>
    ): IntArray {
        val granted = IntArray(permissions.size)
        permissions.forEachIndexed { index, permission ->
            when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                    granted[index] =
                        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Environment.isExternalStorageManager()
                            } else {
                                ActivityCompat.checkSelfPermission(
                                    activity,
                                    permission
                                ) == PackageManager.PERMISSION_GRANTED
                            }
                        ) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
                }

                else -> {
                    granted[index] = ActivityCompat.checkSelfPermission(activity, permission)
                }
            }
        }
        return granted
    }

    private fun makeRequestPermissionItem(
        activity: FragmentActivity,
        permissions: Array<String>,
        onPermissionResult: (permissions: Array<out String>, granted: IntArray) -> Unit
    ): EasyPermissionItem {
        val tag = "uuid:${UUID.randomUUID()}"
        var permissionFragment = activity.supportFragmentManager.findFragmentByTag(tag)
        if (permissionFragment == null) {
            activity.supportFragmentManager.beginTransaction().run {
                val fragment = EasyPermissionFragment()
                permissionFragment = fragment
                add(fragment, tag)
                commitNow()
            }
        }
        val permissionItem = EasyPermissionItem(tag, permissions, onPermissionResult)
        permissionRequests[permissionFragment!!] = permissionItem
        return permissionItem
    }
}