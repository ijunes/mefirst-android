package com.ijunes.mefirst.entries.repository

import com.ijunes.entries.data.EntriesRepository
import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class EntriesRepositoryImpl(private val database: MeFirstDatabase) : EntriesRepository {

    override suspend fun getAllEntriesByDate(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<EntryEntity>> {
        return database.entriesDao().getEntriesByDate(startOfDay, endOfDay)
    }

    override suspend fun getAllEntries(): Flow<Map<Long, List<EntryEntity>>> {
        return database.entriesDao().getAllEntries().map { entries ->
            entries.groupBy { entry ->
                Calendar.getInstance().apply {
                    timeInMillis = entry.timeStamp
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        }
    }
}
