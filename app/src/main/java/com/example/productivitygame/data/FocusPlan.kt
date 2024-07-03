package com.example.productivitygame.data

import kotlin.time.Duration

data class FocusPlan(
    val name: String,
    val workDuration: Duration,
    val shortBreakDuration: Duration,
    // If cycles is not null, long break should not be null either
    val longBreakDuration: Duration? = null,
    // number of work+short break cycles before longer break
    val cycles: Int? = null
)
