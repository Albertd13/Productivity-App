package com.example.productivitygame.data

import android.content.Context
import com.example.productivitygame.foreground.TimerServiceManager
import com.example.productivitygame.notifications.NotificationExactScheduler

class AppContainer(private val context: Context) {
    val recurringCatAndTaskDao by lazy { TaskDatabase.getDatabase(context).recurringTaskDao() }
    val focusPlanDao by lazy { TaskDatabase.getDatabase(context).focusPlanDao() }
    val notificationScheduler = NotificationExactScheduler(context)
    val foreGroundServiceManager = TimerServiceManager(context)
}