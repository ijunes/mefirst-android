package com.ijunes.mefirst.entries.repository

import com.ijunes.entries.data.EntriesRepository
import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntriesRepositoryImpl(private val database: MeFirstDatabase) : EntriesRepository {

    override fun getAllEntriesByDate(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<EntryEntity>> {
        return database.entriesDao().getEntriesByDate(startOfDay, endOfDay)
    }

    override fun getAllEntries(): Flow<Map<Long, List<EntryEntity>>> {
        return database.entriesDao().getAllEntries().map { entries ->
            entries.groupBy { entry -> normalizeToMidnight(entry.timeStamp) }
        }
    }
}
