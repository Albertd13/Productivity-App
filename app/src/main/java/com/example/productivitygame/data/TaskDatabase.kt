package com.example.productivitygame.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.Instant


class Converters {
    @TypeConverter
    fun instantFromEpochMillis(epochMillis: Long): Instant {
        return Instant.fromEpochMilliseconds(epochMillis)
    }

    @TypeConverter
    fun instantToEpochMillis(date: Instant): Long {
        return date.toEpochMilliseconds()
    }
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
    companion object {
        @Volatile
        private var Instance: TaskDatabase? = null
        fun getDatabase(context: Context): TaskDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TaskDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}