package com.example.productivitygame.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
class TaskDefaultSelectableDates(private val todayDateUtcMillis: Long): SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis >= todayDateUtcMillis


    override fun isSelectableYear(year: Int): Boolean =
        year >= getCurrentDate().year
}

@OptIn(ExperimentalMaterial3Api::class)
class TaskWeeklySelectableDates(
    private val todayDateUtcMillis: Long,
    private val daysOfWeek: Set<DayOfWeek>
): SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        (utcTimeMillis >= todayDateUtcMillis) and
        (Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).dayOfWeek in daysOfWeek)


    override fun isSelectableYear(year: Int): Boolean =
        year >= getCurrentDate().year
}
