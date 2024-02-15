# EasyPermission

这是一个快速请求权限的代码库

我们知道Android中请求权限，需要使用 `ActivityCompat.requestPermissions(activity, permissions, requestCode)` 并且在 `onRequestPermissionsResult`
中根据 `requestCode` 判断是哪个权限请求来做响应，不仅写起来很麻烦，并且多个业务都需要合并在一起处理，到时代码逻辑不清晰。

此库提供 EasyPermission，直接请求权限，提供**回调方式**直接将权限请求结果返回，并且适配了**首次权限请求**，**拒绝过自行弹窗**，**增加顶部权限说明框**，**页面跳转和小窗请求权限**等
使用起来非常方便。举个例子：

这里我们请求通话和短信权限

```kotin
EasyPermission.requestPermission(
    this,
    REQUEST_CALL_CODE,
    "请求通话和短信权限",
    arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS)
) { permissions, granted ->
    if (isAllGranted(permissions, granted)) {
        "通话权限 获取成功".toast(this)
    } else {
        "通话权限 获取失败".toast(this)
    }
}
```
只需要在回调中，判断是否两个权限都授予即可，是不是非常方便。

android对文件权限有很大改动，在api 30以上需要所有文件权限，这里对外部存储权限也做了对应的适配

```kotlin
EasyPermission.requestStoragePermission(
    this, REQUEST_STORAGE_CODE, "请求storage权限",
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
) { permissions, granted ->
    if (isAllGranted(permissions, granted)) {
        "文件权限-> 获取成功".toast(this)
    } else {
        "文件权限-> 获取失败".toast(this)
    }
}
```

框架中会根据不同的android sdk使用不同的方法来判断是否有正确的权限
```kotlin
fun requestStoragePermission(
    activity: FragmentActivity,
    requestCode: Int,
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
        requestPermission(
            activity, requestCode, text, requestPermissions
        ) { permissions, granted ->
            onPermissionResult.invoke(permissions, granted)
        }
    }
}
```