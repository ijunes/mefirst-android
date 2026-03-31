package com.ijunes.mefirst.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntriesDao {

    @Query("SELECT * FROM entries")
    fun getAllEntries(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE timeStamp BETWEEN :startOfDay AND :endOfDay")
    fun getEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<EntryEntity>>

    @Insert
    fun addAllNoteEntries(vararg entries: EntryEntity)

    @Query("DELETE FROM entries")
    fun deleteAll()

}

@Dao
interface WorkEntriesDao {

    @Query("SELECT * FROM work_entries")
    fun getAllEntries(): Flow<List<WorkEntryEntity>>

    @Query("SELECT * FROM work_entries WHERE timeStamp BETWEEN :startOfDay AND :endOfDay")
    fun getEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkEntryEntity>>

    @Insert
    fun addAllNoteEntries(vararg entries: WorkEntryEntity)

    @Query("DELETE FROM work_entries")
    fun deleteAll()
}
