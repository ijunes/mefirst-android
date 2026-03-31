package com.ijunes.mefirst.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ijunes.mefirst.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayDao {

    @Query("SELECT * FROM today")
    fun getAll(): Flow<List<NoteEntity>>

    @Insert
    fun insert(note: NoteEntity)
    @Insert
    fun insertAll(vararg notes: NoteEntity)

    @Delete
    fun delete(note: NoteEntity)

    @Query("DELETE FROM today")
    fun deleteAll()

}