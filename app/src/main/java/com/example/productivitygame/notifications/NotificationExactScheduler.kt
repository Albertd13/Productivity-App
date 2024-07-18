package com.example.productivitygame.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.productivitygame.MainActivity
import com.example.productivitygame.ProductivityGameApplication
import com.example.productivitygame.data.Task
import com.example.productivitygame.ui.utils.getAlarmItem

class NotificationExactScheduler(
    private val context: Context
) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleNotification(taskAlarmItem: TaskAlarmItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms())
        {
            //TODO: ask for scheduling permission
            return
        }
        val app = context as ProductivityGameApplication
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    app.currentActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
            return
        }
        createNotificationChannel(notificationManager)
        val intent = Intent(context, ScheduledNotificationReceiver::class.java).apply {
            putExtra(titleExtra, taskAlarmItem.taskName)
            putExtra(messageExtra, taskAlarmItem.notificationDesc)
        }
        val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("NOTIFICATION_SCHEDULE", taskAlarmItem.taskId.toString())

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                taskAlarmItem.triggerInstant.toEpochMilliseconds(),
                showIntent
            ),
            PendingIntent.getBroadcast(
                context,
                taskAlarmItem.taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
    fun cancelNotifications(taskIdList: List<Int>) {
        taskIdList.forEach {
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    it,
                    Intent(context, ScheduledNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

    }
    //TODO: Might make this fun (and its constituents suspend functions down the line)
    fun updateNotifications(task: Task) {
        if (task.notificationsEnabled)
            scheduleNotification(task.getAlarmItem())
        else cancelNotifications(listOf(task.id))
    }
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            channelID,
            channelName,
            importance
        )
        channel.description = channelDesc
        notificationManager.createNotificationChannel(channel)

    }
}