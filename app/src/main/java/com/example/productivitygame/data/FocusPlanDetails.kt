package com.example.productivitygame.data

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class FocusPlanDetails(
    val name: String = "",
    val workDuration: Duration = 0.minutes,
    val shortBreakDuration: Duration = 0.minutes,
    // If cycles is not null, long break should not be null either
    val longBreakDuration: Duration? = null,
    // number of work+short break cycles before longer break
    val cycles: Int? = null
)

fun FocusPlanDetails.toFocusPlan(): FocusPlan =
    FocusPlan(
        name = name,
        workDurationInMillis = workDuration.inWholeMilliseconds,
        shortBreakDurationInMillis = shortBreakDuration.inWholeMilliseconds,
        longBreakDurationInMillis = longBreakDuration?.inWholeMilliseconds,
        cycles = cycles
    )
fun FocusPlan.toFocusPlanDetails(): FocusPlanDetails =
    FocusPlanDetails(
        name = name,
        workDuration = workDurationInMillis.toDuration(DurationUnit.MILLISECONDS),
        shortBreakDuration = shortBreakDurationInMillis.toDuration(DurationUnit.MILLISECONDS),
        longBreakDuration = longBreakDurationInMillis?.toDuration(DurationUnit.MILLISECONDS),
        cycles = cycles
    )
