package com.example.productivitygame.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.RecurringCatAndTaskDao
import com.example.productivitygame.data.TaskAndRecurringCat
import com.example.productivitygame.ui.utils.getCurrentDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class ScheduleViewModel(
    private val recurringCatAndTaskDao: RecurringCatAndTaskDao
): ViewModel() {
    var scheduleUiState by mutableStateOf(ScheduleUiState())
        private set

    fun updateScheduleUiState(newScheduleUiState: ScheduleUiState) {
        scheduleUiState = newScheduleUiState
    }
    fun getTasksOnDate(
        startDate: LocalDate,
        endDate: LocalDate,
        hasTime: Boolean
    ): StateFlow<ScheduleTaskState> =
        recurringCatAndTaskDao
            .getTasksFromInstantRange(
                startDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
                endDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
                hasTime = hasTime
            )
            .map { dataList ->
                ScheduleTaskState(dataList.map { it.toTaskDetails() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ScheduleTaskState()
            )

    //Note: this will not delete the RecurringCat
    suspend fun deleteTask(taskDetails: TaskDetails) {
        recurringCatAndTaskDao.delete(taskDetails.toTask())
    }
    suspend fun completeTask(taskDetails: TaskDetails) {
        val task = taskDetails.toTask()
        if (taskDetails.recurringType != null){
            recurringCatAndTaskDao.update(
                task.copy(datetimeInstant =
                    task.datetimeInstant.plus(
                        value = 1,
                        unit = taskDetails.recurringType.interval,
                        timeZone = TimeZone.currentSystemDefault()
                    )
                )
            )
        } else {
            recurringCatAndTaskDao.delete(task)
        }
    }
    //No change should be made to RecurringCat through this viewModel, so only task updated
    suspend fun updateTask(taskDetails: TaskDetails) {
        recurringCatAndTaskDao.update(taskDetails.toTask())
    }
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ScheduleUiState(
    val dateSelected: LocalDate = getCurrentDate(),
    val selectedTaskToDelete: TaskDetails = TaskDetails()
)

data class ScheduleTaskState(
    val taskList: List<TaskDetails> = listOf()
)

fun TaskAndRecurringCat.toTaskDetails(): TaskDetails {
    val localDatetime = task.datetimeInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return TaskDetails(
        taskId = task.id,
        recurringCatId = recurringCategory.id,
        name = task.name,
        notes = task.notes,
        recurringType = recurringCategory.type,
        productive = task.productive,
        notificationsEnabled = task.notificationsEnabled,
        date = localDatetime.date,
        time = if (task.hasTime) localDatetime.time else null,
        durationInMillis = task.durationInMillis,
        selectedDays = recurringCategory.daysOfWeek ?: setOf()
    )
}

fun TaskAndRecurringCat.toTaskUiState(isEntryValid: Boolean = false): TaskUiState =
    TaskUiState(
        taskDetails = this.toTaskDetails(),
        isEntryValid = isEntryValid
    )

