package com.example.productivitygame.ui.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn

// Gives the epoch Millis to LocalDate in UTC
fun getCurrentDate(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.todayIn(zone)

// epoch millis to UTC date
fun <EpochMillis: Long>EpochMillis.toUtcDate(): LocalDate = LocalDate.fromEpochDays((this / 86400000).toInt())
fun LocalDate.toEpochMillis(zone: TimeZone = TimeZone.currentSystemDefault()) =
    this.atStartOfDayIn(zone).toEpochMilliseconds()
