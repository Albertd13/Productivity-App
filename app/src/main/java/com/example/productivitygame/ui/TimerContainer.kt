package com.example.productivitygame.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.productivitygame.R
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.foreground.ACTION_NEXT_SEGMENT
import com.example.productivitygame.foreground.ACTION_PAUSE_TIMER
import com.example.productivitygame.foreground.ACTION_RESUME_TIMER
import com.example.productivitygame.ui.screens.CountdownItem
import com.example.productivitygame.ui.screens.WorkSegment
import com.example.productivitygame.ui.viewmodels.MyCountDownTimer


sealed class TimerState(
    val currentActionIconRes: Int,
    val title: String,
    val intentAction: String
) {
    data object RUNNING : TimerState(R.drawable.baseline_pause_40, "Pause", ACTION_PAUSE_TIMER)
    data object PAUSED : TimerState(R.drawable.baseline_play_arrow_40, "Resume", ACTION_RESUME_TIMER)
    // 0 as the text should appear instead of icon
    data object SEGMENT_ENDED : TimerState(0, "Next", ACTION_NEXT_SEGMENT)
}

// TODO: create a Chronometer for foreground service functions
object TimerContainer {
    private var countDownTimer: MyCountDownTimer? = null
    var isTimerInitialised by mutableStateOf(false)
    // TODO: remove this flag and replace usages with timerState
    var isTimerRunning by mutableStateOf(false)
        private set
    var workCompletionProgress = 0 to 0
        private set
    private var timerState: TimerState = TimerState.RUNNING
        set(value) {
            field = value
            onTimerStateChanged?.invoke(value)
        }
    // Listeners from foreground service
    // can be changed to empty list to accommodate multiple listeners
    private var onSegmentMillisChanged: ((Long) -> Unit)? = null
    // true represents timer running, false represents timer paused
    private var onTimerStateChanged: ((TimerState) -> Unit)? = null
    // Called whenever final timer in sequence is completed
    private var onFinalTimerFinished: (() -> Unit)? = null
    private var onNewCountdownItem: ((CountdownItem) -> Unit)? = null


    var segmentDurationLeftMillis by mutableLongStateOf(0)
        private set
    var totalDurationLeftMillis by mutableLongStateOf(0)
        private set
    var focusPlanSequenceTotalTimeMillis: Long = 0
        private set

    var focusPlanSequence: List<CountdownItem> = emptyList()
        private set
    var focusPlan: FocusPlanDetails? = null
        private set
    // index of current item counting down, -1 means not started
    private var focusPlanSequenceIndex = -1

    private fun setNextSegmentTimer() {
        if (focusPlanSequenceIndex >= focusPlanSequence.size - 1) return
        if (getCurrentItem() is WorkSegment) {
            workCompletionProgress = workCompletionProgress.first + 1 to workCompletionProgress.second
        }
        val currentCountdownItem = focusPlanSequence[++focusPlanSequenceIndex]
        //Log.d("TIMER", "$countdownItem")
        Log.d("CALLBACK", "${onNewCountdownItem != null}")
        onNewCountdownItem?.invoke(currentCountdownItem)
        countDownTimer = getCountdownTimer(currentCountdownItem.duration.inWholeMilliseconds)
    }
    fun setFocusPlanAndSequence(newFocusPlan: FocusPlanDetails, newFocusPlanSequence: List<CountdownItem>) {
        focusPlan = newFocusPlan
        focusPlanSequence = newFocusPlanSequence
        focusPlanSequenceTotalTimeMillis = focusPlanSequence
            .sumOf { it.duration.inWholeMilliseconds }
        segmentDurationLeftMillis = 0
        totalDurationLeftMillis = focusPlanSequenceTotalTimeMillis
        workCompletionProgress = 0 to focusPlanSequence.count { it is WorkSegment }
    }
    private fun getCurrentItem() = if (focusPlanSequenceIndex == -1) null else focusPlanSequence[focusPlanSequenceIndex]
    // Only for use for foreground service for now
    fun setOnSegmentMillisChangedListener(listener: (Long) -> Unit) {
        onSegmentMillisChanged = listener
    }
    fun setOnNewCountdownItemListener(listener: (CountdownItem) -> Unit) {
        onNewCountdownItem = listener
        Log.d("CALLBACK", "Countdown Item listener set")
    }
    fun setOnTimerRunStateChangedListener(listener: (TimerState) -> Unit) {
        onTimerStateChanged = listener
    }

    fun setOnTimerFinishedListener(listener: () -> Unit) {
        onFinalTimerFinished = listener
    }

    fun deleteTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isTimerRunning = false
        isTimerInitialised = false
        focusPlan = null
        focusPlanSequence = emptyList()
        focusPlanSequenceIndex = -1
    }
    fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timerState = TimerState.PAUSED
    }
    private fun startTimer() {
        countDownTimer?.start()
        isTimerRunning = true
        timerState = TimerState.RUNNING
    }
    fun resumeTimer() {
        countDownTimer = getCountdownTimer(segmentDurationLeftMillis)
        startTimer()
    }
    fun startNextSegment() {
        setNextSegmentTimer()
        isTimerInitialised = true
        startTimer()
    }
    private fun getCountdownTimer(millisInFuture: Long) =
        MyCountDownTimer(
            millisInFuture = millisInFuture,
            countDownInterval = 1000,
            onTickCallback = {
                if (it / 1000 != segmentDurationLeftMillis / 1000) {
                    totalDurationLeftMillis -= segmentDurationLeftMillis - it
                    segmentDurationLeftMillis = it
                    onSegmentMillisChanged?.invoke(it)
                }
            },
            // different callback for last timer to trigger notification
            onFinishCallback = if (focusPlanSequenceIndex < focusPlanSequence.size - 1) {
                { timerState = TimerState.SEGMENT_ENDED }
            } else {
                {
                    onFinalTimerFinished?.invoke()
                    deleteTimer()
                    Log.d("TIMER", "TIMER FINISHED")
                }
            }
        )
}