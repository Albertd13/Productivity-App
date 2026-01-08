package com.example.productivitygame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
import com.example.productivitygame.ui.components.BetterTextField
import com.example.productivitygame.ui.components.DefaultTopAppBar
import com.example.productivitygame.ui.components.TimePickerDialog
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.utils.toEpochMillis
import com.example.productivitygame.ui.utils.toUtcDate
import com.example.productivitygame.ui.viewmodels.modify_task_models.AddTaskViewModel
import com.example.productivitygame.ui.viewmodels.modify_task_models.EditTaskViewModel
import com.example.productivitygame.ui.viewmodels.modify_task_models.TaskDetails
import com.example.productivitygame.ui.viewmodels.modify_task_models.TaskSelectableDates
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import java.util.Locale

object AddTaskDestination : NavigationDestination {
    override val route = "add_task"
    override val titleRes = R.string.add_task_title
    const val selectedDateInUTCMillisArg = "selected_date"
    val routeWithArgs = "$route?$selectedDateInUTCMillisArg={$selectedDateInUTCMillisArg}"
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    SaveTaskForm(
        navigateBack = navigateBack,
        taskDetails = viewModel.taskUiState.taskDetails,
        datePickerState = viewModel.datePickerState,
        onValueChange = viewModel::updateUiState,
        onSavePressed = {
            coroutineScope.launch {
                viewModel.saveItem()
            }
            navigateBack()
        },
        onOpenDatePickerDialog = { viewModel.updateDatePickerState(it) },
        updateSelectableDates = { viewModel.getCurrentSelectableDates() },
        isEntryValid = viewModel.taskUiState.isEntryValid,
        modifier = modifier,
        topAppBarTitle = stringResource(AddTaskDestination.titleRes)
    )
}

object EditTaskDestination : NavigationDestination {
    override val route = "edit_task"
    override val titleRes = R.string.edit_task_title
    const val taskIdArg = "taskId"
    val routeWithArgs = "$route/{$taskIdArg}"
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    SaveTaskForm(
        navigateBack = navigateBack,
        topAppBarTitle = stringResource(id = EditTaskDestination.titleRes),
        taskDetails = viewModel.taskUiState.taskDetails,
        datePickerState = viewModel.datePickerState,
        onValueChange = viewModel::updateUiState,
        onSavePressed = {
            coroutineScope.launch {
                viewModel.updateItem()
                navigateBack()
            }
        },
        onOpenDatePickerDialog = { viewModel.updateDatePickerState(it) },
        updateSelectableDates = viewModel::getCurrentSelectableDates,
        isEntryValid = viewModel.taskUiState.isEntryValid,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveTaskForm(
    topAppBarTitle: String,
    taskDetails: TaskDetails,
    datePickerState: DatePickerState,
    onValueChange: (TaskDetails) -> Unit,
    onSavePressed: () -> Unit,
    onOpenDatePickerDialog: (DatePickerState) -> Unit,
    updateSelectableDates: () -> TaskSelectableDates,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit = {},
    isEntryValid: Boolean = false
) {
    var isInvalidInputPopupVisible by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = isInvalidInputPopupVisible) {
        if (isInvalidInputPopupVisible) {
            delay(3000) // Change to the duration you want (e.g., 3000ms = 3 seconds)
            isInvalidInputPopupVisible = false
        }
    }
    var recurringSelectorEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    if (taskDetails.recurringType != null) {
        recurringSelectorEnabled = true
    }
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                titleText = topAppBarTitle,
                navigateBack = navigateBack,
                canNavigateBack = true
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column {
                // Name Field (compulsory)
                OutlinedTextField(
                    value = taskDetails.name,
                    onValueChange = { onValueChange(taskDetails.copy(name = it)) },
                    label = { Text(text = stringResource(R.string.task_name), style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.labelLarge
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
                    label = { Text(text = stringResource(R.string.add_note_label), style = MaterialTheme.typography.bodyMedium) },
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
                // Deadline Toggle
                ToggleWithTextRow(
                    text = "Set as Deadline",
                    checked = taskDetails.isDeadline,
                    onCheckedChange = { onValueChange(taskDetails.copy(isDeadline = it)) }
                )
                // Recurring Toggle
                RecurringToggle(
                    recurringType = taskDetails.recurringType,
                    selectorEnabled = recurringSelectorEnabled,
                    onCheckedChange = {
                        recurringSelectorEnabled = it
                        if (!it) onValueChange(taskDetails.copy(recurringType = null))
                    },
                    onTypeChange = { onValueChange(taskDetails.copy(recurringType = it)) },
                    selectedDays = taskDetails.selectedDays,
                    onSelectDay = {
                        with(taskDetails) {
                            val newSet =
                                if (selectedDays.contains(it)) selectedDays.minusElement(it)
                                else selectedDays.plusElement(it)
                            onValueChange(copy(selectedDays = newSet))
                        }
                    }
                )
                // Date Picker (optional)
                DateSelectorRow(
                    savedDate = taskDetails.date,
                    onConfirmDate = {
                        if (it != null) onValueChange(taskDetails.copy(date = it.toUtcDate()))
                        if (it != null) onValueChange(taskDetails.copy(date = it.toUtcDate()))
                    },
                    taskDetails = taskDetails,
                    datePickerState = datePickerState,
                    onOpenDatePickerDialog = onOpenDatePickerDialog,
                    updateSelectableDates = updateSelectableDates
                )
                TimeSelector(
                    savedTime = taskDetails.time,
                    onConfirmTime = { hour, minute ->
                        onValueChange(
                            taskDetails.copy(time = LocalTime(hour, minute))
                        )
                    }
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            if (isEntryValid) {
                                onSavePressed()
                            } else {
                                isInvalidInputPopupVisible = true
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.save_task), style = MaterialTheme.typography.labelMedium)
                    }
                    if (isInvalidInputPopupVisible) {
                        Text(text = "Invalid Input", color = Color.Red)
                    }
                }

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
    val recurringTypeClass = recurringTypeSelected?.let { it::class }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        ) {

            OutlinedButton(
                onClick = { onTypeChange(RecurringType.Daily()) },
                colors =
                if (recurringTypeClass == RecurringType.Daily::class)
                    selectedButtonColor else defaultButtonColor
            ) {
                Text(text = stringResource(R.string.daily_type))
            }
            OutlinedButton(
                onClick = { onTypeChange(RecurringType.Weekly()) },
                colors =
                if (recurringTypeClass == RecurringType.Weekly::class) selectedButtonColor
                else defaultButtonColor
            ) {
                Text(text = stringResource(R.string.weekly_type))
            }

            OutlinedButton(
                onClick = {
                    onTypeChange(RecurringType.Monthly().apply {interval = DateTimeUnit.MONTH})
                },
                colors =
                if (recurringTypeClass == RecurringType.Monthly::class) selectedButtonColor
                else defaultButtonColor
            ) {
                Text(text = stringResource(R.string.monthly_type))
            }

            OutlinedButton(
                onClick = { onTypeChange(RecurringType.Custom()) },
                colors =
                if (recurringTypeClass == RecurringType.Custom::class) selectedButtonColor
                else defaultButtonColor
            ) {
                BetterTextField(
                    placeholder = { Text(text = "Custom") },
                    value = customTypeText,
                    enabled = recurringTypeClass == RecurringType.Custom::class,
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
        if (recurringTypeClass == RecurringType.Weekly::class) {
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
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier)
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
        modifier = modifier.fillMaxWidth()
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
fun DateSelectorRow(
    savedDate: LocalDate?,
    onConfirmDate: (Long?) -> Unit,
    taskDetails: TaskDetails,
    datePickerState: DatePickerState,
    onOpenDatePickerDialog: (DatePickerState) -> Unit,
    updateSelectableDates: () -> TaskSelectableDates,
    modifier: Modifier = Modifier,
    initialUTCDateMillis: Long =
        getCurrentDate().toEpochMillis(TimeZone.UTC),
) {
    val customDateFormat = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); dayOfMonth()
    }
    var showDatePicker by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ) {
        Text(
            text = if (taskDetails.recurringType != null) stringResource(id = R.string.start_date_select)
            else stringResource(R.string.date_select),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = savedDate?.format(customDateFormat) ?:
                    getCurrentDate().format(customDateFormat),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = {
                    onOpenDatePickerDialog(
                        with(datePickerState){
                            val newSelectableDates = updateSelectableDates()
                            DatePickerState(
                                locale = Locale.getDefault(),
                                initialSelectedDateMillis =
                                    if (savedDate == null) {
                                        initialUTCDateMillis
                                    } else {
                                        val originalSelection = savedDate.toEpochMillis(TimeZone.UTC)
                                        if (newSelectableDates.isSelectableDate(originalSelection)) {
                                            originalSelection
                                        } else null
                                    },
                                yearRange = yearRange,
                                initialDisplayMode = displayMode,
                                selectableDates = newSelectableDates
                            )
                        }
                    )
                    showDatePicker = true
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_icon),
                    contentDescription = stringResource(R.string.calendar_icon_desc),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size))
                )
            }
        }
    }

    // Lambda function for dismissing the Date Picker dialog
    fun dismissDatePicker() {
        showDatePicker = false
        datePickerState.selectedDateMillis =
            savedDate?.toEpochMillis(TimeZone.UTC) ?: initialUTCDateMillis
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = ::dismissDatePicker,
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
                    onClick = ::dismissDatePicker
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
        Text(text = stringResource(R.string.time_select), style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = savedTime?.format(customTimeFormat) ?: "",
                style = MaterialTheme.typography.bodyMedium
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
/*
@OptIn(ExperimentalMaterial3Api::class)
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
            onSavePressed = {},
            )
    }
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SaveTaskFormPreview() {
    SaveTaskForm(
        topAppBarTitle = "Test Form",
        taskDetails = TaskDetails(),
        datePickerState = DatePickerState(
            yearRange = getCurrentDate().year - 10 ..
                getCurrentDate().year + 10,
            locale = Locale.getDefault(),
            initialSelectedDateMillis = getCurrentDate().toEpochMillis(TimeZone.UTC),
        ),
        onValueChange = {},
        onSavePressed = {},
        onOpenDatePickerDialog = {},
        updateSelectableDates = {
            TaskSelectableDates(
                getCurrentDate().toEpochMillis(TimeZone.UTC),
                true,
                emptySet()
            )
        }
    )
}
