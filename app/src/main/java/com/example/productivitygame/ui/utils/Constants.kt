package com.example.productivitygame.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.productivitygame.data.FocusPlanDetails
import kotlin.time.Duration.Companion.minutes

// Timer Screen Constants
const val DEFAULT_DURATION_FORMAT = "%02d:%02d:%02d"
// Table Names
const val FOCUS_PLAN_TABLE= "FocusPlan"
const val RECURRING_CAT_TABLE = "RecurringCats"

val WORK_COLOR = Color(255, 0, 0)
val SHORT_BREAK_COLOR = Color(0, 255, 0)
val LONG_BREAK_COLOR = Color(0, 0, 255)

const val THIN_STROKE_WIDTH = 6.0f
const val THICK_STROKE_WIDTH = 15.0f


val POMODORO = FocusPlanDetails(
    workDuration = 20.minutes,
    shortBreakDuration = 5.minutes,
    cycles = 4,
    longBreakDuration = 15.minutes,
    name = "POMODORO"
)