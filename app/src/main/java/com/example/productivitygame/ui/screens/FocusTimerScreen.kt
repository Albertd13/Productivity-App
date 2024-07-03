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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
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
import com.example.productivitygame.ui.viewmodels.TimerViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

sealed class CountdownItem(open val duration: Duration)
    class ShortBreak(duration: Duration): CountdownItem(duration)
    class LongBreak(duration: Duration): CountdownItem(duration)
    class WorkSegment(duration: Duration): CountdownItem(duration)

object TimerDestination : NavigationDestination {
    override val route = "timer"
    override val titleRes = R.string.focus_timer_title
}
@Composable
fun FocusTimerScreen(
    viewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    if(viewModel.timerUiState.isTimerInitialised) {
        TimerSection(
            durationLeftInSegmentMillis = viewModel.timerUiState.segmentDurationLeftMillis,
            totalDurationLeftMillis = viewModel.timerUiState.totalDurationLeftMillis,
            timeElapsedMillis = viewModel.focusPlanSequenceTotalTimeMillis - viewModel.timerUiState.totalDurationLeftMillis,
            onClickPause = viewModel::pauseTimer,
            onClickResume = viewModel::resumeTimer,
            isTimerRunning = viewModel.timerUiState.isTimerRunning,
            onClickNext = viewModel::startNextSegment,
            timerColorStops = viewModel.colorStops,
            currentCountdownItem = viewModel.timerUiState.countdownItem,
            onClickDelete = viewModel::deleteTimer
        )
    }
    else {
        InitialiseTimerSection(
            onClickStart = {
                viewModel.initialiseTimer(it)
                viewModel.startTimer()
            }
        )
    }
}

@Composable
fun InitialiseTimerSection(onClickStart: (Duration) -> Unit = {}){
    var hoursSelected by rememberSaveable { mutableIntStateOf(0) }
    var minutesSelected by rememberSaveable { mutableIntStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BetterTextField(
                    value = hoursSelected.toString(),
                    onValueChange = {
                        val changed = it.toIntOrNull()
                        if (changed != null) hoursSelected = changed
                    },

                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = "hours")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BetterTextField(
                    value = minutesSelected.toString(),
                    onValueChange = {
                        val changed = it.toIntOrNull()
                        if (changed != null) minutesSelected = changed
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(text = "minutes")
            }
        }
        Button(onClick = { onClickStart(hoursSelected.hours + minutesSelected.minutes) }) {
            Text(text = stringResource(R.string.start_button))
        }
    }
}

@Composable
fun TimerSection(
    modifier: Modifier = Modifier,
    durationLeftInSegmentMillis: Long = 0,
    totalDurationLeftMillis: Long = 0,
    timeElapsedMillis: Long = 0,
    isTimerRunning: Boolean = false,
    currentCountdownItem: CountdownItem,
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
            colorStops = timerColorStops,
            currentCountdownItem = currentCountdownItem
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
    currentCountdownItem: CountdownItem,
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
    val sweepAngle = timeElapsedMillis.toFloat() / (totalDurationLeftMillis + timeElapsedMillis + 1)
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
            Button(onClick = onClickNext) {
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
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
        )
    }
}

@Preview (showBackground = true, showSystemUi = true)
@Composable
fun CountdownSegmentPreview() {
    ProductivityGameTheme {
        TimerSection(
            onClickPause = { /*TODO*/ },
            onClickNext = { /*TODO*/ },
            onClickResume = {},
            onClickDelete = {},
            currentCountdownItem = WorkSegment(0.minutes),
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
            currentCountdownItem = WorkSegment(20.minutes),
            colorStops = getColorStops(POMODORO.generateFocusSequence(2.hours))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InitialiseTimerSectionPreview() {
    ProductivityGameTheme {
        InitialiseTimerSection()
    }
}