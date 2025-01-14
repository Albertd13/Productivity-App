package com.example.productivitygame.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.productivitygame.MainActivity
import com.example.productivitygame.R

// Constants for notification
const val notificationID = 1
const val channelID = "CHANNEL_ONE"
const val channelName = "Task_Reminder_Notify"
const val channelDesc = "When notifications enabled on any task, notifies user"


const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class ScheduledNotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("NOTIF_ERROR", "Notifications Permissions not granted")
            return
        }
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelID)
            //TODO: Change icon to smth more meaningful
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.getStringExtra(titleExtra)) // Set title from intent
            .setContentText(intent.getStringExtra(messageExtra)) // Set content text from intent
            .setContentIntent(openAppPendingIntent)
            .build()
        // Get the NotificationManager service
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Show the notification using the manager
        manager.notify(notificationID, notification)
    }
}