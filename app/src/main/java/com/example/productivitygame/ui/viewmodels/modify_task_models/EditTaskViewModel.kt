package com.example.productivitygame.ui.viewmodels.modify_task_models

import android.util.Log
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.dao.RecurringCatAndTaskDao
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.notifications.NotificationExactScheduler
import com.example.productivitygame.ui.screens.EditTaskDestination
import com.example.productivitygame.ui.utils.Result
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.utils.getRecurringCat
import com.example.productivitygame.ui.utils.toEpochMillis
import com.example.productivitygame.ui.utils.toTask
import com.example.productivitygame.ui.utils.toTaskUiState
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import java.util.Locale

class EditTaskViewModel(
    private val recurringCatAndTaskDao: RecurringCatAndTaskDao,
    private val notificationExactScheduler: NotificationExactScheduler,
    savedStateHandle: SavedStateHandle
): ModifyTaskViewModel() {
    private val taskId: Int = checkNotNull(savedStateHandle[EditTaskDestination.taskIdArg])
    override var taskUiState by mutableStateOf(TaskUiState())
    private var originalTaskDetails: TaskDetails = TaskDetails()
    init {
        viewModelScope.launch {
            taskUiState = recurringCatAndTaskDao.getTaskWithId(taskId).toTaskUiState(true)
        }
        originalTaskDetails = taskUiState.taskDetails.copy()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override var datePickerState by mutableStateOf(DatePickerState(
        yearRange = 2024..2025,
        locale = Locale.getDefault(),
        initialSelectedDateMillis = getCurrentDate().toEpochMillis(TimeZone.UTC),
        selectableDates = getCurrentSelectableDates()
    ))

    //for Weekly Recurring types, save a separate task for each selected day, with date modified to fit day
    private suspend fun saveNewTasks() {
        if (validateInput() is Result.Success) {
            with(taskUiState.taskDetails){
                val taskList =
                    if (recurringType?.let { it::class } == RecurringType.Weekly::class) {
                        val startDayIsoNr = (date!!.dayOfWeek).isoDayNumber
                        buildList {
                            selectedDays.forEach {
                                val daysToAdd = (it.isoDayNumber - startDayIsoNr) % 7
                                val newDate = date.plus(daysToAdd, DateTimeUnit.DAY)
                                add(taskUiState.taskDetails
                                        .copy(date = newDate, taskId = 0)
                                        .toTask()
                                )
                            }
                        }
                    } else listOf(toTask())
                taskList.forEach {
                    notificationExactScheduler.updateNotifications(it)
                }
                recurringCatAndTaskDao.insertRecurringTasks(
                    recurringCategory = getRecurringCat(),
                    tasksToInsert = taskList
                )
            }
        } else {
            Log.d("INVALID", "${taskUiState.taskDetails}")
        }
    }

    suspend fun updateItem() {
        //if no change in days of week, task can be directly updated
        if (taskUiState.taskDetails.selectedDays == originalTaskDetails.selectedDays) {
            val task = taskUiState.taskDetails.toTask()
            notificationExactScheduler.updateNotifications(task)
            recurringCatAndTaskDao.update(task)
        } else {
            val deletedTaskIds = recurringCatAndTaskDao.deleteTasksByCatIdAndName(
                recurringCatId = taskUiState.taskDetails.recurringCatId,
                taskName = originalTaskDetails.name
            )
            notificationExactScheduler.cancelNotifications(deletedTaskIds)
            saveNewTasks()
        }
    }
}

