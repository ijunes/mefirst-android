package com.ijunes.entries.data

import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.model.NoteMode
import kotlinx.coroutines.flow.Flow

interface EntriesRepository {
    fun getAllEntriesByDate(startOfDay: Long, endOfDay: Long, mode: NoteMode): Flow<List<EntryEntity>>
    fun getAllEntries(mode: NoteMode): Flow<Map<Long, List<EntryEntity>>>
}
