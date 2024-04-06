package com.aidaole

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidaole.easypermission.EasyPermission
import com.aidaole.easypermission.R
import com.aidaole.easypermission.RequestDialogParams
import com.aidaole.easypermission.RequestPermissionParams
import com.aidaole.easypermission.databinding.ActivityMainBinding
import com.aidaole.ext.logi
import com.aidaole.ext.toast
import com.aidaole.files.FileScanner

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val layout by lazy { ActivityMainBinding.inflate(layoutInflater) }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.root)
        layout.storageBtn.setOnClickListener {
            EasyPermission.requestStoragePermission(
                RequestPermissionParams(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            ) { permissions, granted ->
                if (EasyPermission.isAllGranted(permissions, granted)) {
                    "文件权限-> 获取成功".toast(this)
                } else {
                    "文件权限-> 获取失败".toast(this)
                }
            }
        }
        layout.scanFileBtn.setOnClickListener {
            val hasPermission =
                EasyPermission.checkPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                )
            if (hasPermission) {
                scanFiles()
            } else {
                "没有storage权限".toast(this)
            }
        }
        layout.callBtn.setOnClickListener {
            val descView = layoutInflater.inflate(R.layout.permission_desc, layout.root, false)
            EasyPermission.requestPermission(
                RequestPermissionParams(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS),
                    descView
                )
            ) { permissions, granted ->
                if (EasyPermission.isAllGranted(permissions, granted)) {
                    "通话权限 获取成功".toast(this)
                } else {
                    "通话权限 获取失败".toast(this)
                }
            }
        }
        layout.locationBtn.setOnClickListener {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            EasyPermission.requestPermission(
                RequestPermissionParams(
                    this, arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    null,
                    RequestDialogParams(
                        "正在请求地理位置权限",
                        "这里是地物理位置权限使用说明",
                        "允许",
                        "拒绝"
                    )
                )
            ) { permissions, granted ->
                if (EasyPermission.isAllGranted(permissions, granted)) {
                    "locationBtn-> 获取定位权限成功".logi(TAG)
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            ?.let {
                                val latitude = it.latitude
                                val longitude = it.longitude
                                "locationBtn-> 地理位置: ${latitude}, $longitude".toast(this)
                            } ?: run {
                            locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                1000,
                                1F
                            ) {
                                val latitude = it.latitude
                                val longitude = it.longitude
                                "locationBtn-> 地理位置: ${latitude}, $longitude".toast(this)
                            }
                        }
                    }
                } else {
                    "locationBtn-> 没有权限".toast(this)
                }
            }
        }
    }

    private fun scanFiles() {
        val result = FileScanner().scanTxtFiles("/storage/emulated/0")
        if (result.isEmpty()) {
            "scanFiles-> 空空如也".logi(TAG)
        } else {
            result.forEach {
                "scanFiles-> ${it.name}".logi(TAG)
            }
        }
    }
}