package com.ijunes.mefirst.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.model.NoteMode
import kotlinx.coroutines.flow.Flow

@Dao
interface EntriesDao {

    @Query("SELECT * FROM entries WHERE mode = :mode")
    fun getAllEntries(mode: NoteMode): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE timeStamp BETWEEN :startOfDay AND :endOfDay AND mode = :mode")
    fun getEntriesByDate(startOfDay: Long, endOfDay: Long, mode: NoteMode): Flow<List<EntryEntity>>

    @Insert
    suspend fun addAllNoteEntries(vararg entries: EntryEntity)

    @Query("DELETE FROM entries WHERE mode = :mode")
    suspend fun deleteAll(mode: NoteMode)

}
