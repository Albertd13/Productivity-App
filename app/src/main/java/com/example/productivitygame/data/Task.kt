package com.example.productivitygame.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "TaskList"
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "notes")
    val taskNotes: String,

    @ColumnInfo(name = "recurring_type")
    val recurringType: RecurringType?,

    @ColumnInfo(name = "productive")
    val productive: Boolean,

    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean,

    @ColumnInfo(name = "date")
    val datetimeInstant: Instant,

    @ColumnInfo(name = "time")
    val hasTime: Boolean,

    @ColumnInfo(name = "duration")
    val durationInMillis: Int,

    @Embedded
    val reward: TaskReward?,

    @ColumnInfo(name = "difficulty")
    val difficulty: TaskDifficulty?,
)

data class TaskReward(
    val xpGain: Int? = null,
    val goldGain: Int? = null,
    val intGain: Int? = null,
    val strGain: Int? = null,
    val dexGain: Int? = null,
    val conGain: Int? = null
)
