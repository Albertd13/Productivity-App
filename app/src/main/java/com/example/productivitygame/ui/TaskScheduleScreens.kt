package com.example.productivitygame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.ui.theme.ProductivityGameTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.todayIn


@Composable
fun AddTaskScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    productive: Boolean = true,
    viewModel: AddTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    AddTaskForm(
        taskDetails = viewModel.taskUiState.taskDetails,
        onValueChange = viewModel::updateUiState,
        onSavePressed = {
            coroutineScope.launch {
                viewModel.saveItem()
                navigateBack()
            }
        },
        modifier = modifier
    )
}

@Composable
fun AddTaskForm(
    taskDetails: TaskDetails,
    onValueChange: (TaskDetails) -> Unit,
    onSavePressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Name Field (compulsory)
        OutlinedTextField(
            value = taskDetails.name,
            onValueChange = { onValueChange(taskDetails.copy(name = it)) },
            label = { Text(text = stringResource(R.string.task_name)) },
            modifier = Modifier.fillMaxWidth(),
        )
        // Notes Field (optional)
        OutlinedTextField(
            value = taskDetails.notes,
            onValueChange = { onValueChange(taskDetails.copy(notes = it)) },
            placeholder = { Text(text = stringResource(R.string.add_note_placeholder)) },
            label = { Text(text = stringResource(R.string.add_note_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )
        // Notifications Toggle
        ToggleWithTextRow(
            text = stringResource(R.string.notifications_toggle),
            checked = taskDetails.notificationsEnabled,
            onCheckedChange = { onValueChange(taskDetails.copy(notificationsEnabled = it)) }
        )
        // Recurring Toggle
        ToggleWithTextRow(
            text = "Recurring",
            checked = taskDetails.recurringType != null,
            onCheckedChange = { onValueChange(taskDetails.copy(recurringType = RecurringType.Daily)) }
        )
        
        // Date Picker (optional)
        DateSelector(
            savedDate = taskDetails.date,
            onConfirmDate = {
                if (it != null) onValueChange(taskDetails.copy(date = it.toUtcDate()))
            }
        )
        TimeSelector(
            savedTime = taskDetails.time,
            onConfirmTime = {hour, minute ->
                onValueChange(
                    taskDetails.copy(time = LocalTime(hour, minute))
                )
            }
        )

        //TODO: Save task details to database
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { onSavePressed() },
            ) {
                Text(text = "Add Task")
            }
        }
    }
}

@Composable
fun ToggleWithTextRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    savedDate: LocalDate?,
    onConfirmDate: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    initialUTCDateInMillis: Long =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochMillis(TimeZone.UTC)
) {

    val datePickerState = rememberDatePickerState(
        yearRange = 2024..2025,
        initialSelectedDateMillis = initialUTCDateInMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Text(
            text = stringResource(R.string.date_select),
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = savedDate?.format(LocalDate.Formats.ISO_BASIC) ?:
                    getCurrentDate().format(LocalDate.Formats.ISO_BASIC),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_icon),
                    contentDescription = stringResource(R.string.calendar_icon_desc),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size))
                )
            }
        }
    }

    // Lambda function for dismissing the Date Picker dialog
    val dismissDatePicker = {
        showDatePicker = false
        datePickerState.selectedDateMillis =
            savedDate?.toEpochMillis(TimeZone.UTC) ?: initialUTCDateInMillis
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = dismissDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmDate(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = dismissDatePicker
                ) { Text("Cancel") }
            },
            modifier = Modifier.fillMaxSize()
        )
        {
            DatePicker(state = datePickerState)
        }
    }
}


// Time Picker component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    savedTime: LocalTime?,
    onConfirmTime: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: add way for them to clear selected time
    val timePickerState = rememberTimePickerState()
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var timeChosen by rememberSaveable { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Text(text = stringResource(R.string.time_select), style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = savedTime?.format(LocalTime.Formats.ISO) ?: "",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = {
                showTimePicker = true
                timeChosen = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.clock_icon),
                    contentDescription = stringResource(R.string.time_picker_button),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size))
                )
            }
        }
    }
    val onDismissRequest = { showTimePicker = false }
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissRequest
                ) { Text("Cancel") }
            },
            modifier = Modifier

        ) {
            TimePicker(state = timePickerState)
        }
    }
}



        
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddTaskFormPreview() {
    var uiState by remember{ mutableStateOf(TaskUiState()) }
    ProductivityGameTheme {
        AddTaskForm(
            taskDetails = uiState.taskDetails,
            onValueChange = {
                uiState = TaskUiState(taskDetails = it)
            },
            onSavePressed = {})
    }
}

