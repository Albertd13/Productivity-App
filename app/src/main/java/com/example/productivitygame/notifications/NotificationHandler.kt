package com.example.productivitygame.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.productivitygame.PermissionsLauncher

abstract class NotificationHandler(context: Context) {
    val notificationManager =
        ContextCompat.getSystemService(context, NotificationManager::class.java)

    // Requests for notification permission if not enabled
    // returns notification permission status (after requesting if build version > 33)
    fun getNotificationPermission(): Boolean =
        if (notificationManager?.areNotificationsEnabled() != true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionsLauncher.requestPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) ?: false
            } else false
        } else true

    fun createNotificationChannel(
        channelId: String,
        channelName: String,
        channelDesc: String,
        importance: Int,
        vibrationEnabled: Boolean = false
    ) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        ).apply {
            description = channelDesc
            enableVibration(vibrationEnabled)
        }
        notificationManager?.createNotificationChannel(channel)
    }
}