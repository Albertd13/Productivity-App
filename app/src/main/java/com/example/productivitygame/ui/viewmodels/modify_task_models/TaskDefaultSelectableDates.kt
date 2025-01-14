package com.example.productivitygame.ui.viewmodels.modify_task_models

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import com.example.productivitygame.ui.utils.getCurrentDate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
class TaskSelectableDates(
    private val todayDateUtcMillis: Long,
    private val isTypeWeekly: Boolean,
    private val daysOfWeek: Set<DayOfWeek>
): SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        (utcTimeMillis >= todayDateUtcMillis) and
            if(isTypeWeekly) {
                (Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC)
                    .dayOfWeek in daysOfWeek)
            } else true

    override fun isSelectableYear(year: Int): Boolean =
        year >= getCurrentDate().year
}
