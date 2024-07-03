package com.example.productivitygame.data

import android.content.Context
import com.example.productivitygame.notifications.NotificationExactScheduler

class AppContainer(private val context: Context) {
    val recurringCatAndTaskDao: RecurringCatAndTaskDao by lazy {
        TaskDatabase.getDatabase(context).recurringTaskDao()
    }
    val notificationScheduler = NotificationExactScheduler(context)
}