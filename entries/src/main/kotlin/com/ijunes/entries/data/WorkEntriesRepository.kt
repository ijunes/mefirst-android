package com.ijunes.entries.data

import com.ijunes.mefirst.database.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

interface WorkEntriesRepository {
    fun getAllEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkEntryEntity>>
    fun getAllEntries(): Flow<Map<Long, List<WorkEntryEntity>>>
}
