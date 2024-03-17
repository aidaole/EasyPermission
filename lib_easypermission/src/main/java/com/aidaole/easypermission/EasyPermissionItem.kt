package com.aidaole.easypermission

class EasyPermissionItem(
    val tag: String,
    var permissions: Array<out String>,
    val onPermissionResult: (permissions: Array<out String>, granted: IntArray) -> Unit,
    var requestSystem: Boolean = false
)