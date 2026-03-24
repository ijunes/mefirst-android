package com.ijunes.mefirst.today.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ijunes.mefirst.today.data.WorkTodayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkTodayDao {

    @Query("SELECT * FROM work_today")
    fun getAll(): Flow<List<WorkTodayEntity>>

    @Insert
    fun insert(note: WorkTodayEntity)

    @Insert
    fun insertAll(vararg notes: WorkTodayEntity)

    @Delete
    fun delete(note: WorkTodayEntity)

    @Query("DELETE FROM work_today")
    fun deleteAll()
}
