package com.example.productivitygame.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.productivitygame.data.dao.FocusPlanDao
import com.example.productivitygame.data.dao.RecurringCatAndTaskDao
import com.example.productivitygame.ui.utils.FOCUS_PLAN_TABLE
import com.example.productivitygame.ui.utils.POMODORO
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlin.math.pow
import kotlin.reflect.full.createInstance
import kotlin.time.Duration.Companion.minutes


class Converters {
    private val recurringTypeMap = mapOf(
        RecurringType.Daily::class.simpleName to RecurringType.Daily::class,
        RecurringType.Weekly::class.simpleName to RecurringType.Weekly::class,
        RecurringType.Monthly::class.simpleName to RecurringType.Monthly::class,
        RecurringType.Custom::class.simpleName to RecurringType.Custom::class
    )
    @TypeConverter
    fun instantFromEpochMillis(epochMillis: Long): Instant =
        Instant.fromEpochMilliseconds(epochMillis)

    @TypeConverter
    fun instantToEpochMillis(date: Instant): Long =
        date.toEpochMilliseconds()

    @TypeConverter
    fun fromDateTimeUnit(dateTimeUnit: DateTimeUnit): String =
        when (dateTimeUnit) {
            is DateTimeUnit.DayBased -> "DayBased,${dateTimeUnit.days}"
            is DateTimeUnit.MonthBased -> "MonthBased,${dateTimeUnit.months}"
            is DateTimeUnit.TimeBased -> "TimeBased,${dateTimeUnit.nanoseconds}"
            else -> throw IllegalArgumentException("Unknown DateTimeUnit type")
        }

    @TypeConverter
    fun toDateTimeUnit(unitString: String): DateTimeUnit {
        val parts = unitString.split(',')
        return when (parts[0]) {
            "DayBased" -> DateTimeUnit.DayBased(parts[1].toInt())
            "MonthBased" -> DateTimeUnit.MonthBased(parts[1].toInt())
            "TimeBased" -> DateTimeUnit.TimeBased(parts[1].toLong())
            else -> throw IllegalArgumentException("Unknown DateTimeUnit type")
        }
    }

    @TypeConverter
    fun recurringTypeFromString(recurringTypeString: String): RecurringType? =
        recurringTypeMap[recurringTypeString]?.createInstance()

    @TypeConverter
    fun recurringTypeToString(recurringType: RecurringType?): String =
        recurringType?.let { it::class.simpleName } ?: "None"

    private fun getBitRepresent(dayOfWeek: DayOfWeek): Int = 2.0.pow(dayOfWeek.ordinal).toInt()

    // Uses bitmask - 0 means no selected days
    @TypeConverter
    fun daysOfWeekSetToInt(selectedDays: Set<DayOfWeek>?): Int =
        selectedDays?.fold(0) { sum, dayOfWeek -> sum + getBitRepresent(dayOfWeek) } ?: 0

    @TypeConverter
    fun intToDaysOfWeekSet(selectedDays: Int): Set<DayOfWeek>? =
        if (selectedDays != 0) {
            buildSet {
                for (dayOfWeek in DayOfWeek.entries) {
                    // if > 0, means that particular day is selected
                    if ((selectedDays and getBitRepresent(dayOfWeek)) > 0)
                        add(dayOfWeek)
                }
            }
        } else null
}

val defaultFocusPlans: List<FocusPlan> = listOf(
    POMODORO.toFocusPlan(),
    FocusPlan(
        name = "50/20",
        workDurationInMillis = 50.minutes.inWholeMilliseconds,
        shortBreakDurationInMillis = 20.minutes.inWholeMilliseconds,
        longBreakDurationInMillis = null,
        cycles = null
    )
)

@Database(
    entities = [Task::class, RecurringCategory::class, FocusPlan::class],
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun recurringTaskDao(): RecurringCatAndTaskDao
    abstract fun focusPlanDao(): FocusPlanDao

    companion object {
        @Volatile
        private var Instance: TaskDatabase? = null
        fun getDatabase(context: Context): TaskDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TaskDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("TASK_DATABASE", "onCreate")
                            // Populate the database with null recurring type
                            db.beginTransaction()
                            defaultFocusPlans.forEach {
                                val x = db.insert(FOCUS_PLAN_TABLE, SQLiteDatabase.CONFLICT_REPLACE, it.toContentValues())
                                Log.d("TASK_DATABASE", "onCreate: $x")

                            }
                            db.setTransactionSuccessful()
                            db.endTransaction()
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}