package com.example.productivitygame.ui.viewmodels.modify_task_models

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.data.TaskDifficulty
import com.example.productivitygame.ui.utils.Result
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.utils.toEpochMillis
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

abstract class ModifyTaskViewModel: ViewModel() {

    abstract var taskUiState: TaskUiState
        protected set

    // DatePickerState stored in VM because selectableDates need to change upon changes
    // in TaskUiState, however mutating selectableDates value for rememberDatePickerState arg
    // does not trigger a recomposition, so datePickerState variable must be reassigned to
    // new DatePickerState object each time to reflect changes in selectableDates
    @OptIn(ExperimentalMaterial3Api::class)
    abstract var datePickerState: DatePickerState
        protected set
    fun updateUiState(taskDetails: TaskDetails) {
        val result = validateInput(taskDetails)
        taskUiState = TaskUiState(
            taskDetails = taskDetails,
            isEntryValid = result is Result.Success,
            errorMessage = result.message
        )
    }
    @OptIn(ExperimentalMaterial3Api::class)
    fun updateDatePickerState(newDatePickerState: DatePickerState) {
        datePickerState = newDatePickerState
    }

    fun getCurrentSelectableDates() = TaskSelectableDates(
        todayDateUtcMillis = getCurrentDate().toEpochMillis(TimeZone.UTC),
        isTypeWeekly = taskUiState.taskDetails.recurringType?.let { it::class } == RecurringType.Weekly::class,
        daysOfWeek = taskUiState.taskDetails.selectedDays
    )

    // validates input before allowing saving to database
    protected fun validateInput(taskDetails: TaskDetails = taskUiState.taskDetails): Result =
        with(taskDetails) {
            when {
                name.isBlank() -> Result.Fail("Name cannot be blank")
                (date == null) -> Result.Fail("Date cannot be blank")
                (recurringType?.let { it::class } == RecurringType.Weekly::class) and
                        (!selectedDays.contains(date.dayOfWeek))
                -> Result.Fail("dates selected do not fall in selected days")
                else -> Result.Success()
            }
        }
}

data class TaskUiState(
    val taskDetails: TaskDetails = TaskDetails(),
    val isEntryValid: Boolean = false,
    val errorMessage: String = ""
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
    val isDeadline: Boolean = false,
    val durationInMillis: Int = 0,
    val difficulty: TaskDifficulty? = null,
    // Only relevant for RecurringType.Weekly
    val selectedDays: Set<DayOfWeek> = setOf()
)