package com.example.productivitygame.ui.screens.ScheduleScreen

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

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

@Composable
fun ViewScheduleScreen(
    // new Task has initial selected date set to the currently selected date
    navigateToNewTask: (currentSelectedDate: LocalDate) -> Unit,
    navigateToEditTask: (taskId: Int) -> Unit,
    viewModel: ScheduleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scheduleUiState by viewModel.scheduleUiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var deleteConfirmation by rememberSaveable {  mutableStateOf(false) }

    val timedTaskState by viewModel.timedTaskState.collectAsStateWithLifecycle()
    val todoTaskState by viewModel.todoTaskState.collectAsStateWithLifecycle()
    val deadlineDates by viewModel.getAllDatesWithDeadlines().collectAsStateWithLifecycle()

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
                selectedDate = scheduleUiState.dateSelected,
                deadlineDates = deadlineDates
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
                                        scheduleUiState.copy(pendingDeleteTask = taskDetails)
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
                    description = stringResource(R.string.delete_task_confirmation),
                    onDismissRequest = {
                    },
                    onConfirmRequest = {
                        coroutineScope.launch {
                            if (scheduleUiState.pendingDeleteTask != null) {
                                viewModel.deleteTask(scheduleUiState.pendingDeleteTask!!)
                            }
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
    modifier: Modifier = Modifier,
    deadlineDates: Set<LocalDate> = setOf()
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        actions = {
            SimpleDateSelector(
                onConfirmDate = onSelectCalendarDate,
                selectedDate = selectedDate,
                deadlineDates = deadlineDates
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
    deadlineDates: Set<LocalDate> = setOf()
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMillis(TimeZone.UTC),
        yearRange = getCurrentDate().year - 10 .. getCurrentDate().year + 10,
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
            CustomDatePicker(initialDate = selectedDate,
                deadlineDates = deadlineDates,
                onConfirmDate = { date ->
                    onConfirmDate(date)
                    showDatePicker = false
                }
            )

        }
    }
}

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

