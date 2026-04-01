package com.ijunes.mefirst.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ijunes.mefirst.database.entity.WorkTodayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkTodayDao {

    /**
     * This method supports fetching from work_today table as a flow to update UI screens
     */
    @Query("SELECT * FROM work_today")
    fun getAll(): Flow<List<WorkTodayEntity>>

    /**
     * Used to support the flush operation that does not require a flow to be emitted
     */
    @Query("SELECT * FROM work_today")
    suspend fun getAllOnce(): List<WorkTodayEntity>

    /**
     * This method supports updating the work_today table with a new message entry
     */
    @Insert
    fun insert(note: WorkTodayEntity)

    @Insert
    fun insertAll(vararg notes: WorkTodayEntity)

    /**
     * This method supports removing a single entry from work_today based on user modification
     */
    @Delete
    fun delete(note: WorkTodayEntity)

    @Query("DELETE FROM work_today")
    fun deleteAll()
}
