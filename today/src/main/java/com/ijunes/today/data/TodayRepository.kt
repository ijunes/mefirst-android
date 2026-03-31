package com.ijunes.today.data

import com.ijunes.mefirst.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

interface TodayRepository {

    suspend fun getAllNotes(): Flow<List<NoteEntity>>

    suspend fun insertNote(note: NoteEntity)

    suspend fun flushTodayEntries()

}