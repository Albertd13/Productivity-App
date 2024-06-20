package com.example.productivitygame.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant

@Entity(
    tableName = "TaskList",
    foreignKeys = [ForeignKey(
        entity = RecurringCategory::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("recurringCatId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["recurringCatId"])]
    )
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val recurringCatId: Int,

    val name: String,

    val notes: String,

    val productive: Boolean,

    val notificationsEnabled: Boolean,

    val datetimeInstant: Instant,

    val hasTime: Boolean,

    val durationInMillis: Int,

    @Embedded
    val reward: TaskReward?,

    val difficulty: TaskDifficulty?,
)

@Entity(
    tableName = "RecurringCats",
    //indices = [Index(value = ["type", "interval", "daysOfWeek"], unique = true)]
)
data class RecurringCategory (
    // 1st value is for a dummy record for tasks without recurring cats
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // null value is for dummy record
    val type: RecurringType?,
    val interval: DateTimeUnit,
    // only non-null for RecurringType.Weekly
    val daysOfWeek: Set<DayOfWeek>?
)

data class TaskReward(
    val xpGain: Int? = null,
    val goldGain: Int? = null,
    val intGain: Int? = null,
    val strGain: Int? = null,
    val dexGain: Int? = null,
    val conGain: Int? = null
)

// Currently unrequired since theres no retrieval of Recurring Tasks YET
/*
data class RecurringTask(
    @Embedded val recurringCategory: RecurringCategory,
    @Relation(
        parentColumn = "recurringCatId",
        entityColumn = "id"
    )
    val tasks: List<Task>
)
*/