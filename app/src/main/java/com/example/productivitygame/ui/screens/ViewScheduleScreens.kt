package com.example.productivitygame.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
import com.example.productivitygame.ui.utils.getCurrentDate
import com.example.productivitygame.ui.viewmodels.ScheduleTaskState
import com.example.productivitygame.ui.viewmodels.ScheduleViewModel
import com.example.productivitygame.ui.viewmodels.TaskDetails
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.YearMonth

object ScheduleDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScheduleScreen(
    navigateToNewTask: () -> Unit,
    navigateToEditTask: (TaskDetails) -> Unit,
    viewModel: ScheduleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scheduleUiState = viewModel.scheduleUiState
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigateToNewTask() },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_task)
                    )
                }
        },
        bottomBar = {  }
    ) { innerPadding ->
        WeekCalendarSegment(
            dateSelected = scheduleUiState.dateSelected,
            onSelectDate = {
                viewModel.updateScheduleUiState(
                    scheduleUiState.copy(dateSelected = it)
                )
            },
        )
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

        TaskBody(
            onClearTaskSwipe = { swipeToDismissBoxValue, taskDetails ->
                coroutineScope.launch { viewModel.deleteTask(taskDetails) }
                true
            },
            timedTaskState = timedTaskState,
            todoTaskState = todoTaskState,
            onToggleNotif = {
                coroutineScope.launch { viewModel.updateTask(it) }
            },
            contentPadding = innerPadding
        )
    }
}


@Composable
private fun WeekCalendarSegment(
    dateSelected: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    //TODO: convert all variable init to kotlinx datetime methods
    val currentDate = remember { getCurrentDate() }
    val currentMonth = remember { YearMonth.now() }
    val startDate = remember { currentMonth.minusMonths(6).atStartOfMonth() } // Adjust as needed
    val endDate = remember { currentMonth.plusMonths(48).atEndOfMonth() } // Adjust as needed
    val weekCalendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = currentDate.toJavaLocalDate(),
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    WeekCalendar(
        state = weekCalendarState,
        dayContent = {
            val date = it.date.toKotlinLocalDate()
            Day(
                date = date,
                isSelected = dateSelected == date,
                onClick = { dayDate -> onSelectDate(dayDate) }
            )
        },
        modifier = modifier
    )
}

private val dayOfMonthFormat = LocalDate.Format {
    dayOfMonth()
}
@Composable
private fun Day(date: LocalDate, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.dayOfWeek.toString().substring(0..2),
                fontSize = 12.sp,
                color = Color.Black,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = date.format(dayOfMonthFormat),
                fontSize = 14.sp,
                color = if (isSelected) Color.Green else Color.Black,
                fontWeight = FontWeight.Bold,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color.Green)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskBody(
    onClearTaskSwipe: (SwipeToDismissBoxValue, TaskDetails) -> Boolean,
    timedTaskState: ScheduleTaskState,
    todoTaskState: ScheduleTaskState,
    onToggleNotif: (TaskDetails) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(modifier = modifier) {
        TaskList(
            taskList = timedTaskState.taskList,
            onClearTaskSwipe = onClearTaskSwipe,
            onToggleNotif = onToggleNotif,
            contentPadding = contentPadding
        )
        TaskList(
            taskList = todoTaskState.taskList,
            onClearTaskSwipe = onClearTaskSwipe,
            onToggleNotif = onToggleNotif,
            contentPadding = contentPadding
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskList(
    modifier: Modifier = Modifier,
    onToggleNotif: (TaskDetails) -> Unit = {},
    onClearTaskSwipe: (SwipeToDismissBoxValue, TaskDetails) -> Boolean,
    taskList: List<TaskDetails> = listOf(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(taskList) {
            TaskCard(
                onClearTaskSwipe = onClearTaskSwipe,
                taskDetails = it,
                onToggleNotif = onToggleNotif
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    taskDetails: TaskDetails,
    modifier: Modifier = Modifier,
    onToggleNotif: (TaskDetails) -> Unit,
            // function should handle both task deletion and completion
    onClearTaskSwipe: (SwipeToDismissBoxValue, TaskDetails) -> Boolean,
) {
    var cardHeightDp by remember { mutableStateOf(0.dp) }
    var boxHeightDp by remember { mutableStateOf(0.dp) }

    val localDensity = LocalDensity.current

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { onClearTaskSwipe(it, taskDetails) }
    )
    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        backgroundContent = {
            SwipeBackground(
                dismissDirection = swipeToDismissBoxState.dismissDirection,
                backgroundHeight = cardHeightDp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(boxHeightDp)
            )
        },

    ) {
        Box(
            modifier = modifier.onGloballyPositioned { coordinates ->
                boxHeightDp = with(localDensity) { coordinates.size.height.toDp() }
            }
        ) {
            Card(
                onClick = { /*TODO: View/Edit task details*/ },
                modifier = modifier
                    .wrapContentHeight()
                    .padding(vertical = 10.dp)
                    .onGloballyPositioned { coordinates ->
                        cardHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                    }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = taskDetails.name,
                    )
                    IconButton(
                        onClick = {
                            onToggleNotif(
                                taskDetails.copy(notificationsEnabled = !taskDetails.notificationsEnabled)
                            )
                        }
                    ) {
                        if (taskDetails.notificationsEnabled)
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_notifications_none_24),
                                contentDescription = stringResource(R.string.notifications_disabled)
                            )
                        else
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_notifications_active_24),
                                contentDescription = stringResource(R.string.notifications_enabled)
                            )

                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(
                        id =
                        if (taskDetails.recurringType != null) R.drawable.baseline_repeat_24
                        else R.drawable.baseline_looks_one_24
                    ),
                    contentDescription = null
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    backgroundHeight: Dp,
    modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        when (dismissDirection) {
            SwipeToDismissBoxValue.Settled -> Color.Transparent
            SwipeToDismissBoxValue.StartToEnd -> Color.Green
            SwipeToDismissBoxValue.EndToStart -> Color.Red
        }, label = "SwipeToDismiss Background"
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(backgroundHeight)
                .background(color)
                .padding(horizontal = 10.dp),
            horizontalArrangement =
                if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Arrangement.End
                } else { Arrangement.Start },
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(
                    id = if (dismissDirection == SwipeToDismissBoxValue.EndToStart)
                        R.drawable.baseline_delete_24 else R.drawable.baseline_done_24
                ),
                contentDescription = null
            )
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
            onToggleNotif = {}
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
            onClearTaskSwipe = {_,_ -> true}
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

