package com.example.productivitygame.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.productivitygame.data.FocusPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusPlanDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(focusPlan: FocusPlan)
    /*
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(focusPlanList: List<FocusPlan>)
     */

    @Update
    suspend fun update(item: FocusPlan)

    @Delete
    suspend fun delete(item: FocusPlan)

    @Query("SELECT * FROM FocusPlan")
    fun getAllFocusPlans(): Flow<List<FocusPlan>>


    @Query("SELECT * FROM FocusPlan WHERE name = :name LIMIT 1")
    suspend fun getFocusPlan(name: String): FocusPlan?
}