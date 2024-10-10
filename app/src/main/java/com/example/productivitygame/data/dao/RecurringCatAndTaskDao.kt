package com.example.productivitygame.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.productivitygame.data.RecurringCategory
import com.example.productivitygame.data.RecurringType
import com.example.productivitygame.data.Task
import com.example.productivitygame.data.TaskAndRecurringCat
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant

@Dao
interface RecurringCatAndTaskDao {
    // returns rowId of inserted item
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(taskList: List<Task>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: RecurringCategory): Long

    @Update
    suspend fun update(item: Task)
    @Update
    suspend fun update(item: RecurringCategory)

    @Delete
    suspend fun delete(item: Task)
    @Delete
    suspend fun delete(item: RecurringCategory)

    // if it doesnt exist in database insert and return id
    @Transaction
    suspend fun getIdOfRecurringCat(recurringCategory: RecurringCategory): Int =
        getIdOf(recurringCategory.interval, recurringCategory.type, recurringCategory.daysOfWeek) ?:
        getIdFromRowId(insert(recurringCategory.copy(id = 0)))

    @Query("SELECT id FROM RecurringCats WHERE type = :recurringType AND interval = :interval AND daysOfWeek = :daysOfWeek LIMIT 1")
    suspend fun getIdOf(interval: DateTimeUnit, recurringType: RecurringType?, daysOfWeek: Set<DayOfWeek>?): Int?

    // Get the id value given an internal row id
    @Query("SELECT id FROM RecurringCats WHERE rowid = :rowId LIMIT 1")
    suspend fun getIdFromRowId(rowId: Long): Int

    @Query("SELECT * FROM TaskList WHERE rowid IN (:rowIdList)")
    suspend fun getTasksFromRowIds(rowIdList: List<Long>): List<Task>
    /**
     Returns a list of taskIds of the deleted tasks
     */
    @Transaction
    suspend fun deleteTasksByCatIdAndName(recurringCatId: Int, taskName: String): List<Int> {
        val ids = getMatchingTaskIds(recurringCatId, taskName)
        deleteTasksWithIds(ids)
        return ids
    }

    @Query("SELECT id FROM TaskList WHERE " +
            "recurringCatId = :recurringCatId AND name = :taskName")
    suspend fun getMatchingTaskIds(recurringCatId: Int, taskName: String): List<Int>

    @Query("DELETE FROM TaskList WHERE id IN (:idList)")
    suspend fun deleteTasksWithIds(idList: List<Int>)

    @Transaction
    suspend fun insertAndReturnRecurringTasks(
        recurringCategory: RecurringCategory,
        insertedTasks: List<Task>
    ): List<Task> = getTasksFromRowIds(
        insertRecurringTasks(recurringCategory, insertedTasks)
    )

    @Transaction
    suspend fun insertRecurringTasks(
        recurringCategory: RecurringCategory,
        tasksToInsert: List<Task>
    ): List<Long> {
        val recurringCatId = getIdOfRecurringCat(recurringCategory)
        return insert(tasksToInsert.map { it.copy(recurringCatId = recurringCatId) })
    }

    // to query specific dates just need to provide day start instant and day end instant
    @Query("SELECT * FROM TaskList WHERE " +
            "datetimeInstant >= :instantStart AND " +
            "datetimeInstant < :instantEnd AND " +
            "hasTime = :hasTime ORDER BY datetimeInstant")
    fun getTasksFromInstantRange(
        instantStart: Instant,
        instantEnd: Instant,
        hasTime: Boolean
    ): Flow<List<TaskAndRecurringCat>>

    @Query("SELECT dateTimeInstant FROM TaskList WHERE isDeadline = 1")
    fun getAllDatesWIthDeadlines(): Flow<List<Instant>>

    @Query("SELECT * FROM TaskList WHERE id = :taskId")
    suspend fun getTaskWithId(taskId: Int): TaskAndRecurringCat
}
