package com.example.productivitygame.notifications

import kotlinx.datetime.Instant

data class TaskAlarmItem (
    // uses taskId so that the alarmManager can cancel it if needed later on
    val taskId: Int,
    val triggerInstant: Instant,
    val taskName: String,
    val notificationDesc: String
)