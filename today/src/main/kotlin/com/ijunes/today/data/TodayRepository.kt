package com.ijunes.today.data

import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.model.NoteMode
import kotlinx.coroutines.flow.Flow

interface TodayRepository {

    suspend fun deleteTodayNote(timestamp: Long, mode: NoteMode)

    fun getAllNotes(mode: NoteMode): Flow<List<NoteEntity>>

    suspend fun insertNote(note: NoteEntity)

    suspend fun flushTodayEntries(mode: NoteMode)

}
