package com.ijunes.entries.data

import com.ijunes.mefirst.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

interface EntriesRepository {
    suspend fun getAllEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<EntryEntity>>
    suspend fun getAllEntries(): Flow<Map<Long, List<EntryEntity>>>
}
