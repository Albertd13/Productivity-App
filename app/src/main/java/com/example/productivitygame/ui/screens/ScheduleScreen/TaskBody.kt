package com.example.productivitygame.ui.screens.ScheduleScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.productivitygame.R
import com.example.productivitygame.ui.viewmodels.ScheduleTaskState
import com.example.productivitygame.ui.viewmodels.modify_task_models.TaskDetails

@Composable
fun TaskBody(
    onClearTaskSwipe: (SwipeToDismissBoxValue, TaskDetails) -> Boolean,
    onToggleNotif: (taskToggled: TaskDetails) -> Unit,
    onClickTask: (taskId: Int) -> Unit,
    timedTaskState: ScheduleTaskState,
    todoTaskState: ScheduleTaskState,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState
    ) {
        if (timedTaskState.taskList.isNotEmpty()) {
           item(key = "header_scheduled") {
               Text(
                   text = "Scheduled Tasks",
                   modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                   textAlign = TextAlign.Center
               )
           }
           items(
               items = timedTaskState.taskList,
               key = { task -> task.taskId }
           ) {
               TaskCard(
                   taskDetails = it,
                   onToggleNotif = onToggleNotif,
                   onClickTask = onClickTask,
                   onClearTaskSwipe = onClearTaskSwipe
               )
           }
       }
        if (todoTaskState.taskList.isNotEmpty()) {
            item(key = "header_todo") {
               Text(
                   text = "To-Do Tasks",
                   modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                   textAlign = TextAlign.Center
               )
           }
           items(
               items = todoTaskState.taskList,
               key = { task -> task.taskId }
           ) {
               TaskCard(
                   taskDetails = it,
                   onToggleNotif = onToggleNotif,
                   onClickTask = onClickTask,
                   onClearTaskSwipe = onClearTaskSwipe
               )
           }
       }
    }
}

@Composable
fun TaskCard(
    taskDetails: TaskDetails,
    modifier: Modifier = Modifier,
    onToggleNotif: (TaskDetails) -> Unit,
    onClickTask: (Int) -> Unit,
    // function should handle both task deletion and completion
    onClearTaskSwipe: (SwipeToDismissBoxValue, TaskDetails) -> Boolean,
) {
    var cardHeightDp by remember { mutableStateOf(0.dp) }
    var boxHeightDp by remember { mutableStateOf(0.dp) }
    val cardColor = if(taskDetails.isDeadline)
        MaterialTheme.colorScheme.errorContainer else
            MaterialTheme.colorScheme.secondaryContainer
    val cardContentColor = if(taskDetails.isDeadline)
        MaterialTheme.colorScheme.onErrorContainer else
            MaterialTheme.colorScheme.onSecondaryContainer

    val localDensity = LocalDensity.current
    val threshold: Float = with(LocalConfiguration.current) {
        (this.screenWidthDp * 0.75f).dp.value
    }

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { onClearTaskSwipe(it, taskDetails) },
        positionalThreshold = { threshold }
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
                onClick = { onClickTask(taskDetails.taskId) },
                modifier = modifier
                    .wrapContentHeight()
                    .padding(vertical = 10.dp)
                    .onGloballyPositioned { coordinates ->
                        cardHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                    },
                colors = CardColors(
                    containerColor = cardColor,
                    contentColor = cardContentColor,
                    disabledContainerColor = cardColor.copy(alpha = 0.5f),
                    disabledContentColor = cardContentColor.copy(alpha = 0.5f),
                ),
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
                    IconButton(onClick = { onToggleNotif(taskDetails) }) {
                        if (taskDetails.notificationsEnabled)
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_notifications_active_24),
                                contentDescription = stringResource(R.string.notifications_disabled)
                            )
                        else
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_notifications_none_24),
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

@Composable
fun SwipeBackground(
    dismissDirection: SwipeToDismissBoxValue,
    backgroundHeight: Dp,
    modifier: Modifier = Modifier
) {
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
                } else {
                    Arrangement.Start
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
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