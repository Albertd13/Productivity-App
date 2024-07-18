package com.example.productivitygame.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.productivitygame.ui.screens.CountdownItem
import com.example.productivitygame.ui.screens.LongBreak
import com.example.productivitygame.ui.screens.ShortBreak
import com.example.productivitygame.ui.screens.WorkSegment
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Gives the epoch Millis to LocalDate in UTC
fun getCurrentDate(zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.todayIn(zone)

fun getColorStops(focusSequence: List<CountdownItem>): Array<Pair<Float, Color>> {
    val totalTimeInMillis = focusSequence.sumOf { it.duration.inWholeMilliseconds }
    val colorStops = Array(focusSequence.size * 2) {
        0f to Color.Transparent
    }
    var currentFloat = 0f
    for (i in focusSequence.indices) {
        val currentCountdownItem = focusSequence[i]
        val color = when (currentCountdownItem) {
            is WorkSegment -> WORK_COLOR
            is ShortBreak -> SHORT_BREAK_COLOR
            is LongBreak -> LONG_BREAK_COLOR
        }
        colorStops[i * 2] = currentFloat to color
        currentFloat += currentCountdownItem.duration.inWholeMilliseconds.toFloat() / totalTimeInMillis
        colorStops[i * 2 + 1] = currentFloat to color
    }
    return colorStops
}

