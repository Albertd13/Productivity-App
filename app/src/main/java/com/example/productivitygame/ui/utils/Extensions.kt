package com.example.productivitygame.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.data.RecurringCategory
import com.example.productivitygame.data.Task
import com.example.productivitygame.data.TaskAndRecurringCat
import com.example.productivitygame.data.TaskReward
import com.example.productivitygame.notifications.TaskAlarmItem
import com.example.productivitygame.ui.screens.CountdownItem
import com.example.productivitygame.ui.screens.LongBreak
import com.example.productivitygame.ui.screens.ShortBreak
import com.example.productivitygame.ui.screens.WorkSegment
import com.example.productivitygame.ui.viewmodels.modify_task_models.TaskDetails
import com.example.productivitygame.ui.viewmodels.modify_task_models.TaskUiState
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Duration

fun Task.getAlarmItem(): TaskAlarmItem =
    TaskAlarmItem(
        taskId = id,
        triggerInstant = datetimeInstant,
        taskName = name,
        notificationDesc = notes
    )

fun TaskDetails.toTask(): Task = Task(
    id = taskId,
    name = name,
    notes = notes,
    recurringCatId = recurringCatId,
    productive = productive,
    notificationsEnabled = notificationsEnabled,
    // Null check is done in input validation
    datetimeInstant =
    if (time == null)
        date!!.atStartOfDayIn(TimeZone.currentSystemDefault())
    else
        LocalDateTime(date = date!!, time = time).toInstant(TimeZone.currentSystemDefault()),
    hasTime = time != null,
    durationInMillis = durationInMillis,
    difficulty = null,
    //TODO: calculate rewards based on info above
    reward = TaskReward(),
    isDeadline = isDeadline
)

fun TaskDetails.getRecurringCat(): RecurringCategory =
    RecurringCategory(
        id = recurringCatId,
        type = recurringType,
        interval = recurringType?.interval ?: DateTimeUnit.DAY,
        daysOfWeek = selectedDays.ifEmpty { null }
    )

fun TaskAndRecurringCat.toTaskDetails(): TaskDetails {
    val localDatetime = task.datetimeInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return TaskDetails(
        taskId = task.id,
        recurringCatId = recurringCategory.id,
        name = task.name,
        notes = task.notes,
        isDeadline = task.isDeadline,
        recurringType = recurringCategory.type,
        productive = task.productive,
        notificationsEnabled = task.notificationsEnabled,
        date = localDatetime.date,
        time = if (task.hasTime) localDatetime.time else null,
        durationInMillis = task.durationInMillis,
        selectedDays = recurringCategory.daysOfWeek ?: setOf()
    )
}

/**
 * Darkens any given color by reducing all rgb values by same factor.
 *
 * factor - smaller number results in darker Color
 */
fun Color.darken(factor: Float) =
    Color(
        (red * factor * 255).toInt(),
        (green * factor * 255).toInt(),
        (blue * factor * 255).toInt()
    )

fun TaskAndRecurringCat.toTaskUiState(isEntryValid: Boolean = false): TaskUiState =
    TaskUiState(
        taskDetails = this.toTaskDetails(),
        isEntryValid = isEntryValid
    )

fun FocusPlanDetails.generateFocusSequence (totalWorkTime: Duration): List<CountdownItem> {
    var remainingTime = totalWorkTime
    val originalCycles = cycles ?: -1
    var remainingCycles = originalCycles
    return buildList {
        while (remainingTime > workDuration) {
            add(WorkSegment(workDuration))
            remainingTime -= workDuration
            if (remainingCycles != -1) {
                remainingCycles--
                if (remainingCycles == 0) {
                    // If cycles is not null, long break should not be null either
                    add(LongBreak(longBreakDuration!!))
                    remainingCycles = originalCycles
                } else {
                    add(ShortBreak(shortBreakDuration))
                }
            }
        }
        add(WorkSegment(remainingTime))
    }
}

fun Duration.format(format: String): String {
    return String.format(Locale.getDefault(), format, inWholeHours, inWholeMinutes % 60, inWholeSeconds % 60)
}

// epoch millis to UTC date
fun <EpochMillis: Long>EpochMillis.toUtcDate(): LocalDate =
    LocalDate.fromEpochDays((this / 86400000).toInt())

fun LocalDate.toEpochMillis(zone: TimeZone = TimeZone.currentSystemDefault()) =
    this.atStartOfDayIn(zone).toEpochMilliseconds()