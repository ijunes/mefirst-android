package com.ijunes.mefirst.entries.repository

import com.ijunes.entries.data.WorkEntriesRepository
import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkEntriesRepositoryImpl(private val database: MeFirstDatabase) : WorkEntriesRepository {

    override fun getAllEntriesByDate(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<WorkEntryEntity>> {
        return database.workEntriesDao().getEntriesByDate(startOfDay, endOfDay)
    }

    override fun getAllEntries(): Flow<Map<Long, List<WorkEntryEntity>>> {
        return database.workEntriesDao().getAllEntries().map { entries ->
            entries.groupBy { entry -> normalizeToMidnight(entry.timeStamp) }
        }
    }
}
