package com.example.productivitygame.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant

@Dao
interface RecurringCatAndTaskDao {
    // returns rowId of inserted item
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Task)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecurringCategory): Long

    @Update
    suspend fun update(item: Task)
    @Update
    suspend fun update(item: RecurringCategory)

    @Delete
    suspend fun delete(item: Task)
    @Delete
    suspend fun delete(item: RecurringCategory)

    // if it doesnt exist in database insert and return id
    fun getIdOfRecurringCat(recurringCategory: RecurringCategory): Int =
        getIdOf(recurringCategory.interval, recurringCategory.type, recurringCategory.daysOfWeek) ?:
        getIdFromRowId(runBlocking { insert(recurringCategory.copy(id = 0)) })
    @Query("SELECT id FROM RecurringCats WHERE type = :recurringType AND interval = :interval AND daysOfWeek = :daysOfWeek LIMIT 1")
    fun getIdOf(interval: DateTimeUnit, recurringType: RecurringType?, daysOfWeek: Set<DayOfWeek>?): Int?

    // Get the id value given an internal row id
    @Query("SELECT id FROM RecurringCats WHERE rowid = :rowId LIMIT 1")
    fun getIdFromRowId(rowId: Long): Int

    @Query("DELETE FROM TaskList WHERE " +
            "recurringCatId = :recurringCatId AND name = :taskName")
    suspend fun deleteRecurringTasksWithName(recurringCatId: Int, taskName: String): Int

    @Transaction
    suspend fun insertRecurringTasks(
        recurringCategory: RecurringCategory,
        insertedTasks: List<Task>
    ) {
        val recurringCatId = getIdOfRecurringCat(recurringCategory)
        insertedTasks.forEach {
            insert(it.copy(recurringCatId = recurringCatId))
        }
        Log.d("INSERT", "Inserted Recurring Tasks")
    }
    @Transaction
    @Query("SELECT * FROM TaskList WHERE " +
            "datetimeInstant >= :instantStart AND " +
            "datetimeInstant < :instantEnd AND " +
            "hasTime = :hasTime ORDER BY datetimeInstant")
    fun getTasksFromInstantRange(
        instantStart: Instant,
        instantEnd: Instant,
        hasTime: Boolean
    ): Flow<List<TaskAndRecurringCat>>
    //to query specific dates just need to provide day start instant and day end instant
    @Transaction
    @Query("SELECT * FROM TaskList WHERE id = :taskId")
    suspend fun getTaskWithId(taskId: Int): TaskAndRecurringCat

}

data class TaskAndRecurringCat(
    @Embedded
    val task: Task,
    @Relation(
        parentColumn = "recurringCatId",
        entityColumn = "id"
    )
    val recurringCategory: RecurringCategory
)

