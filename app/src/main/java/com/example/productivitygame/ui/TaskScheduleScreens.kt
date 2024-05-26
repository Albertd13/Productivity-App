package com.example.productivitygame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.ui.theme.ProductivityGameTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
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
    var recurringSelectorEnabled by rememberSaveable {
        mutableStateOf(false)
    }
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
            placeholder = {
                Text(
                    text = stringResource(R.string.add_note_placeholder),
                    fontWeight = FontWeight.Light
                )
            },
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
        RecurringToggle(
            recurringType = taskDetails.recurringType,
            selectorEnabled = recurringSelectorEnabled,
            onCheckedChange = {
                recurringSelectorEnabled = it
            },
            onTypeChange = { onValueChange(taskDetails.copy(recurringType = it)) },
            selectedDays = taskDetails.selectedDays,
            onSelectDay = {
                with(taskDetails){
                    val newSet =
                        if (selectedDays.contains(it)) selectedDays.minusElement(it)
                        else selectedDays.plusElement(it)
                    onValueChange(copy(selectedDays = newSet))
                }
            }
        )

        // Date Picker (optional)
        DateSelector(
            savedDate = taskDetails.date,
            onConfirmDate = {
                if (it != null) onValueChange(taskDetails.copy(date = it.toUtcDate()))
            },
            isRecurring = recurringSelectorEnabled
        )
        TimeSelector(
            savedTime = taskDetails.time,
            onConfirmTime = {hour, minute ->
                onValueChange(
                    taskDetails.copy(time = LocalTime(hour, minute))
                )
            }
        )

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
fun RecurringToggle(
    recurringType: RecurringType?,
    selectedDays: Set<DayOfWeek>,
    onSelectDay: (DayOfWeek) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onTypeChange: (RecurringType) -> Unit,
    modifier: Modifier = Modifier,
    selectorEnabled: Boolean = false
    ) {
    ToggleWithTextRow(
        text = "Recurring",
        checked = selectorEnabled,
        onCheckedChange = onCheckedChange
    )
    if (selectorEnabled)
        RecurringTypeSelector(
            recurringTypeSelected = recurringType,
            onTypeChange = onTypeChange,
            selectedDays = selectedDays,
            onSelectDay = onSelectDay
        )
}

@Composable
fun RecurringTypeSelector(
    selectedDays: Set<DayOfWeek>,
    onSelectDay: (DayOfWeek) -> Unit,
    recurringTypeSelected: RecurringType?,
    onTypeChange: (RecurringType) -> Unit,
    modifier: Modifier = Modifier
) {
    var customTypeText: String by rememberSaveable {
        mutableStateOf("")
    }
    val selectedButtonColor = ButtonDefaults.outlinedButtonColors(containerColor = Color.Green)
    val defaultButtonColor = ButtonDefaults.outlinedButtonColors()
    val recurringTypeSelectedClass = recurringTypeSelected?.let { it::class }

    Column {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = { onTypeChange(RecurringType.Daily()) },
                colors =
                if (recurringTypeSelectedClass == RecurringType.Daily::class)
                    selectedButtonColor else defaultButtonColor
            ) {
                Text(text = stringResource(R.string.daily_type))
            }
            OutlinedButton(
                onClick = { onTypeChange(RecurringType.Weekly()) },
                colors =
                if (recurringTypeSelectedClass == RecurringType.Weekly::class) selectedButtonColor
                else defaultButtonColor
            ) {
                Text(text = stringResource(R.string.weekly_type))
            }

            OutlinedButton(
                onClick = {
                    onTypeChange(RecurringType.Monthly().apply {interval = DateTimeUnit.MONTH})
                },
                colors =
                if (recurringTypeSelectedClass == RecurringType.Monthly::class) selectedButtonColor
                else defaultButtonColor
            ) {
                Text(text = stringResource(R.string.monthly_type))
            }

            OutlinedButton(
                onClick = { onTypeChange(RecurringType.Custom()) },
                colors =
                if (recurringTypeSelectedClass == RecurringType.Custom::class) selectedButtonColor
                else defaultButtonColor
            ) {
                BetterTextField(
                    placeholder = { Text(text = "Custom") },
                    value = customTypeText,
                    enabled = recurringTypeSelectedClass == RecurringType.Custom::class,
                    onValueChange = {
                        customTypeText = it
                        val customInterval = customTypeText.toIntOrNull()
                        if ((customInterval != null) and (customInterval in 1..365))
                            recurringTypeSelected!!.interval = DateTimeUnit.DAY * 7
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }


        }
        if (recurringTypeSelectedClass == RecurringType.Weekly::class) {
            DayOfWeekSelector(
                selectedDays = selectedDays,
                onSelectDay = onSelectDay
            )
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
        Text(text = text, style = MaterialTheme.typography.titleLarge, modifier = Modifier)
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onSelectDay: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedButtonColor = ButtonDefaults.outlinedButtonColors(containerColor = Color.Green)
    val defaultButtonColor = ButtonDefaults.outlinedButtonColors()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (dayOfWeek in DayOfWeek.entries) {
            OutlinedButton(
                onClick = { onSelectDay(dayOfWeek) },
                colors = if (dayOfWeek in selectedDays) selectedButtonColor else defaultButtonColor,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.defaultMinSize(minWidth = 40.dp, minHeight = 40.dp),
            ) {
                Text(
                    text = dayOfWeek.name.substring(0..0),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    savedDate: LocalDate?,
    onConfirmDate: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    initialUTCDateMillis: Long =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochMillis(TimeZone.UTC),
    isRecurring: Boolean = false,
) {
    val customDateFormat = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); dayOfMonth()
    }
    val datePickerState = rememberDatePickerState(
        yearRange = 2024..2025,
        initialSelectedDateMillis = initialUTCDateMillis,
        selectableDates = TaskDefaultSelectableDates(initialUTCDateMillis)
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
            text = if (isRecurring) stringResource(id = R.string.start_date_select)
            else stringResource(R.string.date_select),
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = savedDate?.format(customDateFormat) ?:
                    getCurrentDate().format(customDateFormat),
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
            savedDate?.toEpochMillis(TimeZone.UTC) ?: initialUTCDateMillis
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
    val customTimeFormat = LocalTime.Format {
        hour(); char(':'); minute()
    }

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
                text = savedTime?.format(customTimeFormat) ?: "",
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

