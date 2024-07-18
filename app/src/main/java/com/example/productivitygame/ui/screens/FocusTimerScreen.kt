package com.example.productivitygame.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
import com.example.productivitygame.ui.TimerContainer
import com.example.productivitygame.ui.components.BetterTextField
import com.example.productivitygame.ui.theme.ProductivityGameTheme
import com.example.productivitygame.ui.utils.DEFAULT_DURATION_FORMAT
import com.example.productivitygame.ui.utils.POMODORO
import com.example.productivitygame.ui.utils.THICK_STROKE_WIDTH
import com.example.productivitygame.ui.utils.THIN_STROKE_WIDTH
import com.example.productivitygame.ui.utils.darken
import com.example.productivitygame.ui.utils.format
import com.example.productivitygame.ui.utils.generateFocusSequence
import com.example.productivitygame.ui.utils.getColorStops
import com.example.productivitygame.ui.viewmodels.TimeSelectorState
import com.example.productivitygame.ui.viewmodels.TimerViewModel
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

sealed interface CountdownItem { val duration: Duration }
    data class ShortBreak(override val duration: Duration): CountdownItem
    data class LongBreak(override val duration: Duration): CountdownItem
    data class WorkSegment(override val duration: Duration): CountdownItem

object TimerDestination : NavigationDestination {
    override val route = "timer"
    override val titleRes = R.string.focus_timer_title
    const val focusPlanNameArg = "focus_plan_name"
    // optional arg for focus plan name, if none provided, use default focus plan
    val routeWithArgs = "$route?$focusPlanNameArg={$focusPlanNameArg}"
}
@Composable
fun FocusTimerScreen(
    viewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToFocusPlanSelection: () -> Unit,
) {
    if(TimerContainer.isTimerInitialised) {
        TimerSection(
            durationLeftInSegmentMillis = TimerContainer.segmentDurationLeftMillis,
            totalDurationLeftMillis = TimerContainer.totalDurationLeftMillis,
            timeElapsedMillis = TimerContainer.focusPlanSequenceTotalTimeMillis- TimerContainer.totalDurationLeftMillis,
            onClickPause = viewModel::pauseTimer,
            onClickResume = viewModel::resumeTimer,
            isTimerRunning = TimerContainer.isTimerRunning,
            onClickNext = viewModel::startNextSegment,
            timerColorStops = viewModel.colorStops,
            onClickDelete = viewModel::deleteTimer
        )
    }
    else {
        InitialiseTimerSection(
            isStartButtonEnabled = !TimerContainer.isTimerInitialised,
            onClickStart = {
                viewModel.initialiseTimer(it)
            },
            focusPlanSelectedDetails = viewModel.selectedFocusPlanDetails,
            navigateToFocusPlanSelection = navigateToFocusPlanSelection,
            timeSelectorState = viewModel.timeSelectedState
        )
    }
}

@Composable
fun InitialiseTimerSection(
    isStartButtonEnabled: Boolean = true,
    onClickStart: (Duration) -> Unit = {},
    navigateToFocusPlanSelection: () -> Unit = {},
    focusPlanSelectedDetails: FocusPlanDetails = POMODORO,
    timeSelectorState: TimeSelectorState,
    onSelectorStateChange: (TimeSelectorState) -> Unit = {}
){

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FocusPlanSelectionCard(
            navigateToFocusPlanSelection = navigateToFocusPlanSelection,
            focusPlanSelected = focusPlanSelectedDetails
        )
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BetterTextField(
                    value = timeSelectorState.hoursSelected.toString(),
                    onValueChange = {
                        val changed = it.toIntOrNull()
                        if (changed != null && changed in 0..12) {
                            onSelectorStateChange(timeSelectorState.copy(hoursSelected = changed))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = "hours")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BetterTextField(
                    value = timeSelectorState.minutesSelected.toString(),
                    onValueChange = {
                        val changed = it.toIntOrNull()
                        if (changed != null && changed in 0..59) {
                            onSelectorStateChange(timeSelectorState.copy(minutesSelected = changed))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = "minutes")
            }
        }
        Button(
            onClick = {
                onClickStart(timeSelectorState.hoursSelected.hours + timeSelectorState.minutesSelected.minutes)
            },
            enabled = isStartButtonEnabled
        ) {
            Text(text = stringResource(R.string.start_button))
        }
    }
}

@Composable
fun FocusPlanSelectionCard(
    navigateToFocusPlanSelection: () -> Unit,
    modifier: Modifier = Modifier,
    focusPlanSelected: FocusPlanDetails
) {
    Card(
        onClick = navigateToFocusPlanSelection,
        modifier = modifier
    ) {
        Text(text = focusPlanSelected.name)
        Text(text = "Work Duration: ${focusPlanSelected.workDuration.format(DEFAULT_DURATION_FORMAT)}")
        Text(text = "Short Break Duration: ${focusPlanSelected.shortBreakDuration.format(DEFAULT_DURATION_FORMAT)}")
        Text(text = "Long Break after ${focusPlanSelected.cycles} cycles: ${focusPlanSelected.shortBreakDuration.format(DEFAULT_DURATION_FORMAT)}")
    }
}

@Composable
fun TimerSection(
    durationLeftInSegmentMillis: Long = 0,
    totalDurationLeftMillis: Long = 0,
    timeElapsedMillis: Long = 0,
    isTimerRunning: Boolean = false,
    timerColorStops: Array<Pair<Float, Color>>,
    onClickPause: () -> Unit,
    // Moves on to next segment (e.g. if its work now, next could be a break period)
    onClickNext: () -> Unit,
    onClickResume: () -> Unit,
    onClickDelete: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TimerCircle(
            durationLeftInSegmentMillis = durationLeftInSegmentMillis,
            totalDurationLeftMillis = totalDurationLeftMillis,
            timeElapsedMillis = timeElapsedMillis,
            onClickNext = onClickNext,
            colorStops = timerColorStops
        )
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pause button when running, Resume button when
            Button(onClick = if (isTimerRunning) onClickPause else onClickResume) {
                Text(
                    text = stringResource(
                        if (isTimerRunning) R.string.pause_button else R.string.resume_button
                    )
                )
            }
            Button(onClick = onClickDelete) {
                Text(text = "Delete")
            }
        }
    }
}
@Composable
fun TimerCircle(
    colorStops: Array<Pair<Float, Color>>,
    timeElapsedMillis: Long = 0,
    durationLeftInSegmentMillis: Long = 0,
    totalDurationLeftMillis: Long = 0,
    onClickNext: () -> Unit
) {
    // Completed/WIP events
    val darkColorStops = colorStops.map {
        it.first to it.second.darken(0.6.toFloat())
    }.toTypedArray()
    val sweepAngle = 360 * timeElapsedMillis.toFloat() / (totalDurationLeftMillis + timeElapsedMillis + 1)
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(10.dp)
            .background(
                shape = CircleShape,
                color = Color.LightGray
            ),
        contentAlignment = Alignment.Center
    ){
        ColoredStopWatchBorder(
            colorStops = darkColorStops,
            strokeWidth = THIN_STROKE_WIDTH
        )
        ColoredStopWatchBorder(
            colorStops = colorStops,
            sweepAngle = sweepAngle,
            strokeWidth = THICK_STROKE_WIDTH
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Total Time Left")
            Text(text = totalDurationLeftMillis.toDuration(DurationUnit.MILLISECONDS).format(DEFAULT_DURATION_FORMAT))
            Text(text = "Remaining Time")
            Text(
                text = durationLeftInSegmentMillis.toDuration(DurationUnit.MILLISECONDS).format(DEFAULT_DURATION_FORMAT),
                style = MaterialTheme.typography.displayMedium
            )
            Button(onClick = onClickNext, enabled = durationLeftInSegmentMillis.toInt() == 0) {
                Text(text = stringResource(R.string.next_section))
            }
        }
    }
}

@Composable
fun ColoredStopWatchBorder(
    // color starts from 12 o'clock by default
    colorStops: Array<Pair<Float, Color>>,
    startAngle: Float = 0f,
    strokeWidth: Float = 5.0f,
    sweepAngle: Float = 360f,
) {
    Box(modifier = Modifier.rotate(-90f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val brush = Brush.sweepGradient(
                        colorStops = colorStops,
                        center = Offset.Unspecified,
                    )
                    onDrawWithContent {
                        drawArc(
                            brush = brush,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),

                            )
                    }
                }
        )
    }
}
val hourRange = 0..23
val minuteRange = 0..59
val secondRange = 0..59
private fun getTargetIndex(intList: List<Int>, targetItem: Int): Int =
    intList.indexOf(targetItem) + ((Int.MAX_VALUE / 2) / intList.size) * intList.size - 1
@Composable
fun DurationSelector(
    modifier: Modifier = Modifier,
    currentSelectorState: TimeSelectorState,
    onSelectorStateChange: (TimeSelectorState) -> Unit = {}
){
    val intList = (0..23).toList()
    var isManualEntryEnabled by rememberSaveable { mutableStateOf(false) }
    val scrollerState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            coroutineScope.launch {
                scrollerState.scrollToItem(getTargetIndex(intList, 3))
            }
        }
    ) {
        Text(text = "5 HOURS")
    }
    Column {
        Row {
            Column(
                modifier = modifier
                    .weight(1f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "hours", style = MaterialTheme.typography.labelLarge)
                if (!isManualEntryEnabled) {
                    InfiniteCircularNumberList(
                        itemHeight = 70.dp,
                        intList = hourRange.toList(),
                        initialItem = 0,
                        textStyle = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textColor = Color.Gray,
                        selectedTextColor = Color.Black,
                        scrollState = scrollerState,
                        onNumSelected = {
                            onSelectorStateChange(
                                currentSelectorState.copy(
                                    hoursSelected = it
                                )
                            )
                        },
                        onNumClicked = { isManualEntryEnabled = true }
                    )
                } else {
                    BetterTextField(
                        value = currentSelectorState.hoursSelected.toString(),
                        onValueChange = {
                            if (it.toIntOrNull() in hourRange) {
                                onSelectorStateChange(
                                    currentSelectorState.copy(hoursSelected = it.toInt())
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                    )
                }
            }
            Column(
                modifier = modifier
                    .weight(1f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "minutes", style = MaterialTheme.typography.labelLarge)
                InfiniteCircularNumberList(
                    itemHeight = 70.dp,
                    intList = minuteRange.toList(),
                    initialItem = 0,
                    textStyle = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textColor = Color.Gray,
                    selectedTextColor = Color.Black,
                    onNumSelected = {
                        onSelectorStateChange(
                            currentSelectorState.copy(
                                minutesSelected = it
                            )
                        )
                    },
                    onNumClicked = { isManualEntryEnabled = true }
                )
            }
            Column(
                modifier = modifier
                    .weight(1f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "seconds", style = MaterialTheme.typography.labelLarge)
                InfiniteCircularNumberList(
                    itemHeight = 70.dp,
                    intList = secondRange.toList(),
                    initialItem = 0,
                    textStyle = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textColor = Color.Gray,
                    selectedTextColor = Color.Black,
                    onNumSelected = {
                        onSelectorStateChange(
                            currentSelectorState.copy(
                                secondsSelected = it
                            )
                        )
                    },
                    onNumClicked = { isManualEntryEnabled = true }
                )
            }
        }
    }
}

const val numberOfDisplayedItems = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteCircularNumberList(
    modifier: Modifier = Modifier,
    itemHeight: Dp,
    scrollState: LazyListState = rememberLazyListState(),
    intList: List<Int>,
    initialItem: Int,
    selectedItemScaleFact: Float = 1.5f,
    textStyle: TextStyle,
    textColor: Color,
    selectedTextColor: Color,
    onNumSelected: (item: Int) -> Unit = {},
    onNumClicked: (item: Int) -> Unit = {},
) {
    // target index near middle of lazy column
    val targetIndex = remember {
        getTargetIndex(intList, initialItem)
    }
    LaunchedEffect(Unit) {
        scrollState.scrollToItem(targetIndex)
    }

    val itemHalfHeight = LocalDensity.current.run { itemHeight.toPx() / 2f }

    var lastSelectedIndex by remember { mutableIntStateOf(targetIndex) }

    LazyColumn(
        modifier = modifier
            .height(itemHeight * numberOfDisplayedItems),
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState)
    ) {
        items(
            count = Int.MAX_VALUE,
            itemContent = { lazyColumnIndex ->
                val item = intList[lazyColumnIndex % intList.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable {
                            onNumClicked(item)
                        }
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight = (itemHalfHeight * numberOfDisplayedItems)
                            val isSelected =
                                (y in parentHalfHeight - itemHalfHeight..parentHalfHeight + itemHalfHeight)
                            val index = lazyColumnIndex - 1
                            if (isSelected && lastSelectedIndex != index) {
                                onNumSelected(item)
                                lastSelectedIndex = index
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        style = textStyle,
                        color = if (lastSelectedIndex == lazyColumnIndex)
                            selectedTextColor else textColor,
                        fontSize = if (lastSelectedIndex == lazyColumnIndex) {
                            textStyle.fontSize * selectedItemScaleFact
                        } else {
                            textStyle.fontSize
                        }
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DurationSelectorPreview() {
    ProductivityGameTheme {
        DurationSelector(currentSelectorState = TimeSelectorState())
    }
}
/*
@Preview(showBackground = true)
@Composable
fun CircularNumberListPreview() {
    ProductivityGameTheme {
        InfiniteCircularNumberList(
            itemHeight = 80.dp,
            intList = (0..59).toList(),
            initialItem = 0,
            textStyle = MaterialTheme.typography.displaySmall,
            textColor = Color.Gray,
            selectedTextColor = Color.Black
        )
    }
}

 */
@Preview (showBackground = true, showSystemUi = true)
@Composable
fun CountdownSegmentPreview() {
    ProductivityGameTheme {
        TimerSection(
            onClickPause = { /*TODO*/ },
            onClickNext = { /*TODO*/ },
            onClickResume = {},
            onClickDelete = {},
            timerColorStops = getColorStops(POMODORO.generateFocusSequence(2.hours))
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TimerCirclePreview() {
    ProductivityGameTheme {
        TimerCircle(
            durationLeftInSegmentMillis = 20.minutes.inWholeMilliseconds,
            onClickNext = {},
            colorStops = getColorStops(POMODORO.generateFocusSequence(2.hours))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InitialiseTimerSectionPreview() {
    ProductivityGameTheme {
        InitialiseTimerSection(timeSelectorState = TimeSelectorState())
    }
}