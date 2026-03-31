package com.ijunes.entries.data

import com.ijunes.mefirst.database.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

interface WorkEntriesRepository {
    suspend fun getAllEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkEntryEntity>>
    suspend fun getAllEntries(): Flow<Map<Long, List<WorkEntryEntity>>>
}
