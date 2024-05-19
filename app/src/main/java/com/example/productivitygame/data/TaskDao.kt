package com.example.productivitygame.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Task)

    @Update
    suspend fun update(item: Task)

    @Delete
    suspend fun delete(item: Task)

    @Query("SELECT * from TaskList WHERE date = :date AND time IS NOT NULL")
    fun getTasksWithTimeOnDate(date: String): Flow<List<Task>>
}