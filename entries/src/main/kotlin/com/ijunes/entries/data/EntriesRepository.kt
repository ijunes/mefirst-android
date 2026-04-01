package com.ijunes.entries.data

import com.ijunes.mefirst.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

interface EntriesRepository {
    fun getAllEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<EntryEntity>>
    fun getAllEntries(): Flow<Map<Long, List<EntryEntity>>>
}
