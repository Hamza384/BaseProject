package com.example.charginganimation.hello.baseproject.myproject.util.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment


object PermissionManager {
    private val permissionCallbacks: HashMap<String, PermissionCallback> = HashMap()

    fun requestPermissions(activity: Activity, permissions: Array<String>, callback: PermissionCallback) {
        val permissionCallback = object : PermissionCallback {
            override fun onPermissionGranted() {
                callback.onPermissionGranted()
                permissions.forEach { permission -> permissionCallbacks.remove(permission) }
            }

            override fun onPermissionDenied() {
                callback.onPermissionDenied()
                permissions.forEach { permission -> permissionCallbacks.remove(permission) }
            }
        }

        permissions.forEach { permission ->
            permissionCallbacks[permission] = permissionCallback
        }

        fragment.requestPermissions(permissions, 0)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val callback = permissionCallbacks[permission]
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    callback?.onPermissionGranted()
                } else {
                    callback?.onPermissionDenied()
                }
            }
        }
    }

    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }
}


