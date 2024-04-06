package com.aidaole.easypermission

/**
 * 用户拒绝过，系统弹对话框设置
 */
data class RequestDialogParams(
    val title: String,
    val desc: String,
    val okBtnText: String,
    val cancelBtnText: String,
)