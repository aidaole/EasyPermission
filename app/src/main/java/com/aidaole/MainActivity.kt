package com.aidaole

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidaole.easypermission.EasyPermission
import com.aidaole.easypermission.databinding.ActivityMainBinding
import com.aidaole.ext.logi
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
                this, 12, "请求storage权限"
            ) { permissions, granted ->
                if (isAllGranted(permissions, granted)) {
                    "storageBtn-> 获取成功".logi(TAG)
                } else {
                    "storageBtn-> 获取失败".logi(TAG)
                }
            }
        }
        layout.scanFileBtn.setOnClickListener {
            "scanFileBtn-> ".logi(TAG)
            scanFiles()
        }
        layout.callBtn.setOnClickListener {
            EasyPermission.requestPermission(
                this,
                10,
                "我要请求权限",
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS)
            ) { permissions, granted ->
                if (isAllGranted(permissions, granted)) {
                    "callBtn-> 获取成功".logi(TAG)
                } else {
                    "callBtn-> 获取失败".logi(TAG)
                }
            }
        }
        layout.locationBtn.setOnClickListener {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            EasyPermission.requestPermission(
                this, 101, "请求地理位置权限",
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) { permissions, granted ->
                if (isAllGranted(permissions, granted)) {
                    "locationBtn-> 获取定位权限成功".logi(TAG)
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                            val latitude = it.latitude
                            val longitude = it.longitude
                            "locationBtn-> 地理位置: ${latitude}, $longitude".logi(TAG)
                        } ?: run {
                            locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                1000,
                                1F
                            ) {
                                val latitude = it.latitude
                                val longitude = it.longitude
                                "locationBtn-> 地理位置: ${latitude}, $longitude".logi(TAG)
                            }
                        }
                    }
                } else {
                    "locationBtn-> 没有权限".logi(TAG)
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        "onPause-> ".logi(TAG)
    }

    private fun isAllGranted(permissions: Array<out String>, granted: IntArray): Boolean {
        var allGranted = true
        granted.forEachIndexed { i, _ ->
            "isAllGranted-> ${permissions[i]} -> ${granted[i] == PackageManager.PERMISSION_GRANTED}".logi(TAG)
            if (granted[i] == PackageManager.PERMISSION_DENIED) {
                allGranted = false
            }
        }
        return allGranted
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