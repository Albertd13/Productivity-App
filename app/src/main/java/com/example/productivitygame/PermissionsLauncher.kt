package com.example.productivitygame

import android.util.Log
import androidx.activity.result.ActivityResultLauncher

object PermissionsLauncher {
    lateinit var registerForActivityLauncher: ActivityResultLauncher<String>
    private var requestResult: Boolean? = null

    fun updateRequestResult(result: Boolean) {
        requestResult = result
    }
    fun requestPermission(permission: String): Boolean? {
        registerForActivityLauncher.launch(permission)
        Log.d("PERMISSIONS", "$requestResult")
        return requestResult
    }
}