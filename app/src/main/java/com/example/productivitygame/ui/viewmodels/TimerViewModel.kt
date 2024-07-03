package com.example.productivitygame.ui.viewmodels

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.productivitygame.data.FocusPlan
import com.example.productivitygame.ui.screens.CountdownItem
import com.example.productivitygame.ui.utils.POMODORO
import com.example.productivitygame.ui.screens.WorkSegment
import com.example.productivitygame.ui.utils.generateFocusSequence
import com.example.productivitygame.ui.utils.getColorStops
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class MyCountDownTimer(
    millisInFuture: Long,
    val onTickCallback: (millisUntilFinished: Long) -> Unit,
    val onFinishCallback: () -> Unit,
    countDownInterval: Long
) : CountDownTimer(millisInFuture, countDownInterval) {
    override fun onTick(millisUntilFinished: Long) {
        onTickCallback(millisUntilFinished)
    }
    override fun onFinish() {
        // Code to execute when the timer finishes
        onFinishCallback()
    }
}
class TimerViewModel: ViewModel() {

    var timerUiState by mutableStateOf( TimerUiState() )
        private set

    private var currentSegmentTimer: MyCountDownTimer? = null

    private lateinit var focusPlanSequence: MutableList<CountdownItem>

    var focusPlanSequenceTotalTimeMillis: Long = 0

    var colorStops: Array<Pair<Float, Color>> = arrayOf(0f to Color.Transparent, 1.0f to Color.Transparent)
    fun initialiseTimer(totalWorkTime: Duration) {
        updateUiState(uiState = timerUiState.copy(isTimerInitialised = true))
        focusPlanSequence = timerUiState.focusPlan.generateFocusSequence(totalWorkTime).toMutableList()
        colorStops = getColorStops(focusPlanSequence)
        focusPlanSequenceTotalTimeMillis = focusPlanSequence
            .sumOf { it.duration.inWholeMilliseconds }
        updateUiState(uiState = timerUiState.copy(
            totalDurationLeftMillis = focusPlanSequenceTotalTimeMillis)
        )
        setNextSegmentTimer()
    }
    fun deleteTimer() {
        updateUiState(uiState = timerUiState.copy(isTimerInitialised = false))
        currentSegmentTimer?.cancel()
        currentSegmentTimer = null
    }
    private fun setNextSegmentTimer() {
        if (focusPlanSequence.size == 0) return
        val countdownItem: CountdownItem = focusPlanSequence.removeAt(0)
        //Log.d("TIMER", "$countdownItem")
        currentSegmentTimer = getCountdownTimer(countdownItem.duration.inWholeMilliseconds)
    }
    private fun getCountdownTimer(millisInFuture: Long) =
        MyCountDownTimer(
            millisInFuture = millisInFuture,
            countDownInterval = 1000,
            onTickCallback = {
                if (it / 1000 != timerUiState.segmentDurationLeftMillis / 1000)
                updateUiState(
                    timerUiState.copy(
                        segmentDurationLeftMillis = it,
                        totalDurationLeftMillis = timerUiState.totalDurationLeftMillis - 1000)
                )
            },
            onFinishCallback = { }
        )
    fun startNextSegment() {
        setNextSegmentTimer()
        startTimer()
    }
    fun pauseTimer() {
        currentSegmentTimer?.cancel()
        updateUiState(timerUiState.copy(isTimerRunning = false))
    }

    fun resumeTimer() {
        currentSegmentTimer = getCountdownTimer(timerUiState.segmentDurationLeftMillis)
        startTimer()
    }

    fun startTimer() {
        currentSegmentTimer?.start()
        updateUiState(timerUiState.copy(isTimerRunning = true))
    }

    fun updateUiState(uiState: TimerUiState) {
        timerUiState = uiState
    }
}

data class TimerUiState(
    val isTimerRunning: Boolean = false,
    val isTimerInitialised: Boolean = false,
    val segmentDurationLeftMillis: Long = 0,
    val totalDurationLeftMillis: Long = 0,
    val focusPlan: FocusPlan = POMODORO,
    val countdownItem: CountdownItem = WorkSegment(0.minutes)
)