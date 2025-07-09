package com.example.productivitygame.ui.screens.ScheduleScreen

import CustomDatePicker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
import com.example.productivitygame.ui.components.ConfirmationAlert
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.utils.toEpochMillis
import com.example.productivitygame.ui.viewmodels.ScheduleViewModel
import com.example.productivitygame.ui.viewmodels.modify_task_models.TaskDetails
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.plus

object ScheduleDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}
val customDateFormat = LocalDate.Format {
    dayOfMonth(); char(' '); monthName(MonthNames.ENGLISH_FULL); char(' '); year()
}

// Calendar left and right limits, goes from start of first month to end of last month
const val MONTHS_BEFORE_CURRENT: Long = 6
const val MONTHS_AFTER_CURRENT: Long = 48

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScheduleScreen(
    // new Task has initial selected date set to the currently selected date
    navigateToNewTask: (currentSelectedDate: LocalDate) -> Unit,
    navigateToEditTask: (taskId: Int) -> Unit,
    viewModel: ScheduleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scheduleUiState = viewModel.scheduleUiState
    val coroutineScope = rememberCoroutineScope()
    var deleteConfirmation by rememberSaveable {  mutableStateOf(false) }
    val timedTaskState by viewModel.getTasksOnDate(
        scheduleUiState.dateSelected,
        scheduleUiState.dateSelected.plus(1, DateTimeUnit.DAY),
        hasTime = true
    ).collectAsState()

    val todoTaskState by viewModel.getTasksOnDate(
        scheduleUiState.dateSelected,
        scheduleUiState.dateSelected.plus(1, DateTimeUnit.DAY),
        hasTime = false
    ).collectAsState()

    val deadlineDates by viewModel.getAllDatesWithDeadlines().collectAsState()

    Scaffold(
        floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigateToNewTask(scheduleUiState.dateSelected) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(20.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_task)
                    )
                }
        },
        bottomBar = {  },
        topBar = {
            ViewScheduleTopAppBar(
                title = scheduleUiState.dateSelected.format(customDateFormat),
                onSelectCalendarDate = {
                    if (it != null)
                        viewModel.updateScheduleUiState(
                            scheduleUiState.copy(dateSelected = it)
                        )
                },
                selectedDate = scheduleUiState.dateSelected
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column {
                WeekCalendarSegment(
                    dateSelected = scheduleUiState.dateSelected,
                    onSelectDate = {
                        viewModel.updateScheduleUiState(
                            scheduleUiState.copy(dateSelected = it)
                        )
                    },
                    deadlineDates = deadlineDates
                )
                TaskBody(
                    onClearTaskSwipe = { swipeToDismissBoxValue, taskDetails ->
                        when (swipeToDismissBoxValue) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.updateScheduleUiState(
                                    scheduleUiState.copy(selectedTaskToDelete = taskDetails)
                                )
                                deleteConfirmation = true
                                false
                            }
                            SwipeToDismissBoxValue.StartToEnd -> {
                                coroutineScope.launch { viewModel.completeTask(taskDetails) }
                                true
                            }
                            else -> true
                        }
                    },
                    onToggleNotif = {
                        coroutineScope.launch { viewModel.toggleTaskNotification(it) }
                    },
                    onClickTask = {
                        //TODO: provide a View TaskDetails screen with an OPTION to edit instead of directly edit
                        navigateToEditTask(it)
                    },
                    timedTaskState = timedTaskState,
                    todoTaskState = todoTaskState,
                )
            }
            if (deleteConfirmation) {
                ConfirmationAlert(
                    title = stringResource(R.string.delete_task_title),
                    description = "Are you sure you want to delete the task?",
                    onDismissRequest = {
                        deleteConfirmation = false
                    },
                    onConfirmRequest = {
                        coroutineScope.launch {
                            viewModel.deleteTask(scheduleUiState.selectedTaskToDelete)
                            deleteConfirmation = false
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScheduleTopAppBar(
    title: String,
    onSelectCalendarDate: (dateSelected: LocalDate?) -> Unit,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        actions = {
            SimpleDateSelector(
                onConfirmDate = onSelectCalendarDate,
                selectedDate = selectedDate,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDateSelector(
    onConfirmDate: (LocalDate?) -> Unit,
    selectedDate: LocalDate,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMillis(TimeZone.UTC),
        yearRange = 2024..2025,
    )
    LaunchedEffect(key1 = selectedDate) {
        datePickerState.selectedDateMillis = selectedDate.toEpochMillis(TimeZone.UTC)
    }

    IconButton(
        onClick = { showDatePicker = true }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.calendar_icon),
            contentDescription = stringResource(R.string.calendar_icon_desc),
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size))
        )
    }
    if (showDatePicker) {
        BasicAlertDialog(onDismissRequest = { showDatePicker = false }) {
            CustomDatePicker(initialDate = selectedDate, onConfirmDate = { date ->
                onConfirmDate(date)
                showDatePicker = false
            })

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TaskCardPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TaskCard(
            taskDetails = TaskDetails(name = "Test Task 1", recurringType = RecurringType.Weekly()),
            onClearTaskSwipe = {_,_ -> true},
            onToggleNotif = {},
            onClickTask = {}
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TaskListPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TaskList(
            taskList = listOf(
                TaskDetails(name = "Weekly Recurring", recurringType = RecurringType.Weekly()),
                TaskDetails(name = "Weekly Recurring", recurringType = RecurringType.Weekly()),
                TaskDetails(name = "Monthly Recurring", recurringType = RecurringType.Monthly()),
                TaskDetails(name = "Daily Recurring", recurringType = RecurringType.Daily()),
                TaskDetails(name = "Not Recurring")
            ),
            onClearTaskSwipe = {_,_ -> true},
            onClickTask = {}
        )
    }
}

@Preview
@Composable
fun WeekCalendarPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        WeekCalendarSegment(dateSelected = getCurrentDate(), onSelectDate = {})
    }
}

