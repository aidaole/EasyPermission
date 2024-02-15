package com.aidaole.easypermission

import androidx.fragment.app.Fragment
import com.aidaole.ext.logi

class EasyPermissionFragment : Fragment() {
    companion object {
        private const val TAG = "PermissionFragment"
    }

    override fun onResume() {
        super.onResume()
        "onResume-> ${this.tag}".logi(TAG)
        EasyPermission.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        "onPause-> ${this.tag}".logi(TAG)
    }
}