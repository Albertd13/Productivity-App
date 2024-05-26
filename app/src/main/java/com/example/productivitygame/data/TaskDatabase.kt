package com.example.productivitygame.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlin.math.pow
import kotlin.reflect.full.createInstance


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
    // Uses bitmask
    @TypeConverter
    fun daysOfWeekSetToInt(selectedDays: Set<DayOfWeek>?): Int? =
        selectedDays?.fold(0) { sum, dayOfWeek -> sum + getBitRepresent(dayOfWeek) }

    @TypeConverter
    fun daysOfWeekSetToInt(selectedDays: Int?): Set<DayOfWeek>? =
        if (selectedDays != null) {
            buildSet {
                for (dayOfWeek in DayOfWeek.entries) {
                    // if > 0, means that particular day is selected
                    if ((selectedDays and getBitRepresent(dayOfWeek)) > 0)
                        add(dayOfWeek)
                }
            }
        } else null



}

@Database(entities = [Task::class, RecurringCategory::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun recurringTaskDao(): RecurringCatAndTaskDao

    companion object {
        @Volatile
        private var Instance: TaskDatabase? = null
        fun getDatabase(context: Context): TaskDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TaskDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    /* Pre Populate with dummy data (not done)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate the database with null recurring type
                            val userDao = Instance?.userDao()
                        }
                    })
                     */
                    .build()
                    .also { Instance = it }
            }
        }
    }
}