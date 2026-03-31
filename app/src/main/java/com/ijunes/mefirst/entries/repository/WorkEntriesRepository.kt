package com.ijunes.mefirst.entries.repository

import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

interface WorkEntriesRepository {

    suspend fun getAllEntriesByDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkEntryEntity>>

    suspend fun getAllEntries(): Flow<Map<Long, List<WorkEntryEntity>>>
}

class WorkEntriesRepositoryImpl(private val database: MeFirstDatabase) : WorkEntriesRepository {

    override suspend fun getAllEntriesByDate(
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<WorkEntryEntity>> {
        return database.workEntriesDao().getEntriesByDate(startOfDay, endOfDay)
    }

    override suspend fun getAllEntries(): Flow<Map<Long, List<WorkEntryEntity>>> {
        return database.workEntriesDao().getAllEntries().map { entries ->
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
