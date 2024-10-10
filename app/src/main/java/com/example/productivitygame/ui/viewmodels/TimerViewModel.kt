package com.example.productivitygame.ui.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.data.dao.FocusPlanDao
import com.example.productivitygame.data.toFocusPlanDetails
import com.example.productivitygame.foreground.TimerServiceManager
import com.example.productivitygame.ui.TimerContainer
import com.example.productivitygame.ui.screens.TimerDestination
import com.example.productivitygame.ui.utils.POMODORO
import com.example.productivitygame.ui.utils.generateFocusSequence
import com.example.productivitygame.ui.utils.getColorStops
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

class TimerViewModel(
    private val timerServiceManager: TimerServiceManager,
    private val focusPlanDao: FocusPlanDao,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    // Default is POMODORO name for now
    private val focusPlanName: String =
        savedStateHandle[TimerDestination.focusPlanNameArg] ?: POMODORO.name
    var selectedFocusPlanDetails: FocusPlanDetails by mutableStateOf(POMODORO)
        private set
    var colorStops: Array<Pair<Float, Color>> = arrayOf(0f to Color.Transparent, 1.0f to Color.Transparent)
    var timeSelectedState by mutableStateOf(TimeSelectorState())
        private set
    fun updateTimeSelectedState(timeSelectorState: TimeSelectorState) {
        timeSelectedState = timeSelectorState
    }
    init {
        viewModelScope.launch {
            val focusPlan = focusPlanDao.getFocusPlan(focusPlanName)
            if (focusPlan != null)
                selectedFocusPlanDetails = focusPlan.toFocusPlanDetails()
        }
        if (TimerContainer.isTimerInitialised && TimerContainer.focusPlanSequence.isNotEmpty()) {
            colorStops = getColorStops(TimerContainer.focusPlanSequence)
        }
    }

    fun initialiseTimer() {
        Log.d("INIT_TIMER", "$timeSelectedState")
        val totalWorkTime = timeSelectedState.toDuration()
        val focusPlanSequence = selectedFocusPlanDetails.generateFocusSequence(totalWorkTime)
        Log.d("INIT_TIMER", "$focusPlanSequence")


        TimerContainer.setFocusPlanAndSequence(
            newFocusPlan = selectedFocusPlanDetails,
            newFocusPlanSequence = focusPlanSequence
        )
        colorStops = getColorStops(focusPlanSequence)
        timerServiceManager.getNotificationPermission()
        TimerContainer.startNextSegment()
        timerServiceManager.initTimerService()
    }

    fun deleteTimer() {
        TimerContainer.deleteTimer()
        timerServiceManager.cancelTimerService()
    }

    fun startNextSegment() {
        TimerContainer.startNextSegment()
    }
    fun pauseTimer() {
        TimerContainer.pauseTimer()
    }

    fun resumeTimer() {
        TimerContainer.resumeTimer()
    }
}

data class TimeSelectorState(
    val hoursSelected: Int = 0,
    val minutesSelected: Int = 0,
    val secondsSelected: Int = 0
) {
    fun toDuration(): Duration =
        hoursSelected.hours + minutesSelected.minutes + secondsSelected.seconds
}