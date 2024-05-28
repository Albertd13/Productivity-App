package com.example.productivitygame.ui

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.productivitygame.data.RecurringCategory
import com.example.productivitygame.data.RecurringCatAndTaskDao
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.data.Task
import com.example.productivitygame.data.TaskDifficulty
import com.example.productivitygame.data.TaskReward
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.util.Locale

class AddTaskViewModel(
    private val recurringCatAndTaskDao: RecurringCatAndTaskDao
): ViewModel() {
    var taskUiState by mutableStateOf(TaskUiState())
        private set

    @OptIn(ExperimentalMaterial3Api::class)
    var datePickerState by mutableStateOf(DatePickerState(
        yearRange = 2024..2025,
        locale = Locale.getDefault(),
        initialSelectedDateMillis = getCurrentDate().toEpochMillis(TimeZone.UTC),
        selectableDates = getCurrentSelectableDates()
    ))
        private set
    fun getCurrentSelectableDates() = TaskSelectableDates(
        todayDateUtcMillis = getCurrentDate().toEpochMillis(TimeZone.UTC),
        isTypeWeekly = taskUiState.taskDetails.recurringType?.let { it::class } == RecurringType.Weekly::class,
        daysOfWeek = taskUiState.taskDetails.selectedDays
    )

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState =
            TaskUiState(taskDetails = taskDetails, isEntryValid = false)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateDatePickerState(newDatePickerState: DatePickerState) {
        datePickerState = newDatePickerState
    }

    // validates input before allowing saving to database
    private fun validateInput(taskDetails: TaskDetails = taskUiState.taskDetails): Boolean =
        with(taskDetails) {
            name.isNotBlank() and
            (date != null) and
            (
                (recurringType?.let { it::class } != RecurringType.Weekly::class) or
                (selectedDays.contains(date!!.dayOfWeek))
            )
        }

    //for Weekly Recurring types, save an activity for each selected day, with date modified to fit day
    suspend fun saveItem() {
        if (validateInput()) {
            with(taskUiState.taskDetails){
                val taskList = if (recurringType?.let { it::class } == RecurringType.Weekly::class){
                        val startDayIsoNr = (date!!.dayOfWeek).isoDayNumber
                        buildList {
                            selectedDays.forEach {
                                val daysToAdd = (it.isoDayNumber - startDayIsoNr) % 7
                                val newDate = date.plus(daysToAdd, DateTimeUnit.DAY)
                                add(taskUiState.taskDetails.copy(date = newDate).toTask())
                            }
                        }

                    } else listOf(toTask())

                recurringCatAndTaskDao.insertRecurringTasks(
                    recurringCategory = getRecurringCat(),
                    insertedTasks = taskList
                )
            }
        }
    }
}

data class TaskUiState(
    val taskDetails: TaskDetails = TaskDetails(),
    val isEntryValid: Boolean = false,
)

data class TaskDetails(
    val taskId: Int = 0,
    val recurringCatId: Int = 0,
    val name: String = "",
    val notes: String = "",
    val recurringType: RecurringType? = null,
    val productive: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val date: LocalDate? = getCurrentDate(),
    val time: LocalTime? = null,
    val durationInMillis: Int = 0,
    val difficulty: TaskDifficulty? = null,
    // Only relevant for RecurringType.Weekly
    val selectedDays: Set<DayOfWeek> = setOf()
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
    reward = TaskReward()
)

fun TaskDetails.getRecurringCat(): RecurringCategory =
     RecurringCategory(
        id = recurringCatId,
        type = recurringType,
        interval = recurringType?.interval ?: DateTimeUnit.DAY,
        daysOfWeek = selectedDays.ifEmpty { null }
     )

fun Task.toDetails(): TaskDetails {
    val localDatetime = datetimeInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    val taskDetails = TaskDetails(
        taskId = id,
        name = name,
        notes = notes,
        productive = productive,
        notificationsEnabled = notificationsEnabled,
        // fix the null check in future
        date = localDatetime.date,
        time = if (hasTime) localDatetime.time else null,
        durationInMillis = durationInMillis,
    )

    if (taskDetails.recurringType != null)
        taskDetails.recurringType.interval =  DateTimeUnit.DAY
    return taskDetails
}