package com.example.productivitygame.data

import kotlinx.datetime.DateTimeUnit

enum class TaskDifficulty {
    Easy,
    Manageable,
    Challenging,
    ExtremelyDifficult
}

sealed interface RecurringType {
    var interval: DateTimeUnit
    class Daily: RecurringType {override var interval: DateTimeUnit = DateTimeUnit.DAY}
    class Weekly: RecurringType {
        override var interval: DateTimeUnit = DateTimeUnit.DAY * 7
    }
    class Monthly: RecurringType {override var interval: DateTimeUnit = DateTimeUnit.MONTH}
    class Custom: RecurringType {override var interval: DateTimeUnit = DateTimeUnit.DAY}
}