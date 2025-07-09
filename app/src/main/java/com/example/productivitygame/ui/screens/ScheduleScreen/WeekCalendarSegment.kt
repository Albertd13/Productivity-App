package com.example.productivitygame.ui.screens.ScheduleScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitygame.ui.utils.getCurrentDate
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.YearMonth

@Composable
fun WeekCalendarSegment(
    modifier: Modifier = Modifier,
    deadlineDates: Set<LocalDate> = emptySet(),
    dateSelected: LocalDate,
    onSelectDate: (dateSelected: LocalDate) -> Unit,
) {
    val currentDate = remember { getCurrentDate() }
    val currentMonth = remember { YearMonth.now() }
    val startDate = remember {
        currentMonth.minusMonths(MONTHS_BEFORE_CURRENT).atStartOfMonth()
    } // Adjust as needed
    val endDate = remember {
        currentMonth.plusMonths(MONTHS_AFTER_CURRENT).atEndOfMonth()
    } // Adjust as needed
    val weekCalendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = currentDate.toJavaLocalDate(),
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    LaunchedEffect(dateSelected) {
        weekCalendarState.animateScrollToWeek(dateSelected.toJavaLocalDate())
    }
    WeekCalendar(
        state = weekCalendarState,
        dayContent = {
            val date = it.date.toKotlinLocalDate()
            Day(
                date = date,
                isDeadline = date in deadlineDates,
                isSelected = date == dateSelected,
                onClick = { dayDate -> onSelectDate(dayDate) }
            )
        },
        modifier = modifier
    )
}

@Composable
private fun Day(
    date: LocalDate,
    isDeadline: Boolean = false,
    isSelected: Boolean,
    onClick: (dateOfDayClicked: LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.dayOfWeek.toString().substring(0..2),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = date.format(LocalDate.Format { dayOfMonth() }),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDeadline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color.Green)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}