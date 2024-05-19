package com.example.productivitygame.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.data.Task
import com.example.productivitygame.data.TaskDao
import com.example.productivitygame.data.TaskDifficulty
import com.example.productivitygame.data.TaskReward
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class AddTaskViewModel(private val taskDao: TaskDao): ViewModel() {
    var taskUiState by mutableStateOf(TaskUiState())
        private set

    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState =
            TaskUiState(taskDetails = taskDetails, isEntryValid = false)
    }

    // validates input before allowing saving to database
    private fun validateInput(taskDetails: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(taskDetails) {
            name.isNotBlank() and (date != null)
        }
    }

    suspend fun saveItem() {
        if (validateInput()) {
            taskDao.insert(taskUiState.taskDetails.toTask())
        }
    }
}

data class TaskUiState(
    val taskDetails: TaskDetails = TaskDetails(),
    val isEntryValid: Boolean = false
)

data class TaskDetails(
    val id: Int = 0,
    val name: String = "",
    val notes: String = "",
    val recurringType: RecurringType? = null,
    val productive: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val date: LocalDate? = getCurrentDate(),
    val time: LocalTime? = null,
    val durationInMillis: Int = 0,
    val difficulty: TaskDifficulty? = null
)

fun TaskDetails.toTask(): Task = Task(
    id = id,
    name = name,
    taskNotes = notes,
    recurringType = recurringType,
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

fun Task.toDetails(): TaskDetails {
    val localDatetime = datetimeInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return TaskDetails(
        id = id,
        name = name,
        notes = taskNotes,
        recurringType = recurringType,
        productive = productive,
        notificationsEnabled = notificationsEnabled,
        // fix the null check in future
        date = localDatetime.date,
        //TODO: convert datetime to date and time
        time = if (hasTime) localDatetime.time else null,
        durationInMillis = durationInMillis,
    )
}