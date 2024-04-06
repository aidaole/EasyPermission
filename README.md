# EasyPermission

[![](https://jitpack.io/v/aidaole/EasyPermission.svg)](https://jitpack.io/#aidaole/EasyPermission)

这是一个快速请求权限的代码库

我们知道Android中请求权限，需要使用 `ActivityCompat.requestPermissions(activity, permissions, requestCode)` 并且在 `onRequestPermissionsResult`
中根据 `requestCode` 判断是哪个权限请求来做响应，不仅写起来很麻烦，并且多个业务都需要合并在一起处理，到时代码逻辑不清晰。

此库提供 EasyPermission，直接请求权限，提供:
**回调方式**直接将权限请求结果返回，并且适配了
**andorid30以上文件权限请求接口**
**首次权限请求**
**拒绝过自行弹窗**
**增加顶部权限说明框**
**页面跳转和小窗请求权限**

使用起来非常方便。举个例子：

## 请求文件权限


## 请求带顶部弹窗的权限

```kotin
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
```
只需要在回调中，判断是否两个权限都授予即可，是不是非常方便。


## 请求文件权限

android对文件权限有很大改动，在api 30以上需要所有文件权限，这里对外部存储权限也做了对应的适配

```kotlin
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
```

## 请求被拒绝自定义弹窗

```kotlin
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
```