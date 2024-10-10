package com.example.productivitygame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
import com.example.productivitygame.ui.TimerContainer
import com.example.productivitygame.ui.components.DurationSelector
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
    if (TimerContainer.isTimerInitialised) {
        TimerSection(
            durationLeftInSegmentMillis = TimerContainer.segmentDurationLeftMillis,
            totalDurationLeftMillis = TimerContainer.totalDurationLeftMillis,
            timeElapsedMillis = TimerContainer.focusPlanSequenceTotalTimeMillis - TimerContainer.totalDurationLeftMillis,
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
            onClickStart = viewModel::initialiseTimer,
            focusPlanSelectedDetails = viewModel.selectedFocusPlanDetails,
            navigateToFocusPlanSelection = navigateToFocusPlanSelection,
            timeSelectorState = viewModel.timeSelectedState,
            onSelectorStateChange = viewModel::updateTimeSelectedState
        )
    }
}

@Composable
fun InitialiseTimerSection(
    isStartButtonEnabled: Boolean = true,
    onClickStart: () -> Unit = {},
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
            focusPlanDetails = focusPlanSelectedDetails
        )
        DurationSelector(
            currentSelectorState = timeSelectorState,
            onSelectorStateChange = onSelectorStateChange
        )
        Button(
            onClick = onClickStart,
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
    focusPlanDetails: FocusPlanDetails
) {
    FocusPlanItem(
        focusPlanDetails = focusPlanDetails,
        onSelectFocusPlan = { navigateToFocusPlanSelection() },
        modifier = modifier
    )
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
fun InfiniteCircularNumberListPreview() {
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