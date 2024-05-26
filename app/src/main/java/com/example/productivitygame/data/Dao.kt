package com.example.productivitygame.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek

@Dao
interface RecurringCatAndTaskDao {
    // returns rowId of inserted item
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Task)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: RecurringCategory): Long

    suspend fun getIdOfCat(recurringCategory: RecurringCategory): Int? =
        getIdOf(recurringCategory.type, recurringCategory.interval, recurringCategory.daysOfWeek)

    @Query("SELECT id FROM RecurringCats WHERE type = :type AND interval = :interval AND daysOfWeek = :daysOfWeek")
    suspend fun getIdOf(type: RecurringType?, interval: DateTimeUnit, daysOfWeek: Set<DayOfWeek>?): Int?
    @Update
    suspend fun update(item: Task)
    @Update
    suspend fun update(item: RecurringCategory)

    @Delete
    suspend fun delete(item: Task)
    @Delete
    suspend fun delete(item: RecurringCategory)

    // Get the id value given an internal row id
    @Query("SELECT id FROM RecurringCats WHERE rowid = :rowId")
    suspend fun getIdFromRowId(rowId: Long): Int
    @Transaction
    suspend fun insertRecurringTasks(
        recurringCategory: RecurringCategory,
        insertedTasks: List<Task>
    ) {
        val recurringCatId = getIdOfCat(recurringCategory) ?: getIdFromRowId(insert(recurringCategory))
        insertedTasks.forEach {
            insert(it.copy(recurringCatId = recurringCatId))
        }
        Log.d("DONE", "Insertion of recurring task completed")
    }
}
