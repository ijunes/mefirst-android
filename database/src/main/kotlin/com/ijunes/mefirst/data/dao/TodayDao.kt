package com.ijunes.mefirst.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.model.NoteMode
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayDao {

    @Query("SELECT * FROM today WHERE mode = :mode")
    fun getAll(mode: NoteMode): Flow<List<NoteEntity>>

    @Query("SELECT * FROM today WHERE mode = :mode")
    suspend fun getAllOnce(mode: NoteMode): List<NoteEntity>

    @Insert
    fun insert(note: NoteEntity)

    @Query("DELETE FROM today WHERE id = :id")
    fun delete(id: String)

    @Query("DELETE FROM today WHERE mode = :mode")
    fun deleteAll(mode: NoteMode)

}
