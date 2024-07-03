package com.example.productivitygame.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.RecurringCatAndTaskDao
import com.example.productivitygame.notifications.NotificationExactScheduler
import com.example.productivitygame.ui.utils.getAlarmItem
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.utils.toTask
import com.example.productivitygame.ui.utils.toTaskDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

class ScheduleViewModel(
    private val recurringCatAndTaskDao: RecurringCatAndTaskDao,
    private val notificationExactScheduler: NotificationExactScheduler
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
        val task = taskDetails.toTask()
        recurringCatAndTaskDao.delete(task)
        if (task.notificationsEnabled)
            notificationExactScheduler.cancelNotifications(listOf(task.id))
    }
    suspend fun completeTask(taskDetails: TaskDetails) {
        val task = taskDetails.toTask()
        if (taskDetails.recurringType != null) {
            val newTask = task.copy(datetimeInstant =
                task.datetimeInstant.plus(
                    value = 1,
                    unit = taskDetails.recurringType.interval,
                    timeZone = TimeZone.currentSystemDefault()
                )
            )
            recurringCatAndTaskDao.update(newTask)
            if (task.notificationsEnabled)
                notificationExactScheduler.scheduleNotification(newTask.getAlarmItem())
        } else {
            deleteTask(taskDetails)
        }
    }

    //No change should be made to RecurringCat through this viewModel, so only task updated
    suspend fun toggleTaskNotification(taskDetails: TaskDetails) {
        val updatedTask = taskDetails.copy(notificationsEnabled = !taskDetails.notificationsEnabled).toTask()
        recurringCatAndTaskDao.update(updatedTask)
        notificationExactScheduler.updateNotifications(updatedTask)
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