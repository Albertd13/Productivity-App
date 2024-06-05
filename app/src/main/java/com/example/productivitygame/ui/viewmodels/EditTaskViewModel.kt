package com.example.productivitygame.ui.viewmodels

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.RecurringCatAndTaskDao
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.ui.TaskSelectableDates
import com.example.productivitygame.ui.screens.EditTaskDestination
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.utils.toEpochMillis
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import java.util.Locale

class EditTaskViewModel(
    private val recurringCatAndTaskDao: RecurringCatAndTaskDao,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val taskId: Int = checkNotNull(savedStateHandle[EditTaskDestination.taskIdArg])
    var taskUiState by mutableStateOf(TaskUiState())
        private set
    init {
        viewModelScope.launch {
            taskUiState = recurringCatAndTaskDao.getTaskWithId(taskId)
                .filterNotNull()
                .first()
                .toTaskUiState(true)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    var datePickerState by mutableStateOf(
        DatePickerState(
        yearRange = 2024..2025,
        locale = Locale.getDefault(),
        initialSelectedDateMillis = getCurrentDate().toEpochMillis(TimeZone.UTC),
        selectableDates = getCurrentSelectableDates()
    )
    )
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