package com.ijunes.today.data

import com.ijunes.mefirst.database.entity.WorkTodayEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access contract for work-mode notes.
 *
 * Mirrors [TodayRepository] but operates on [WorkTodayEntity] records, keeping personal and
 * work feeds fully isolated. Implementations are provided by `:today:todayImpl` and bound via
 * Koin in `:today:todayApp`.
 */
interface WorkTodayRepository {

    /**
     * Returns a [Flow] that emits the full list of work notes whenever the underlying data
     * changes. The flow remains active for the lifetime of the collector.
     */
    suspend fun getAllNotes(): Flow<List<WorkTodayEntity>>

    /** Persists a new work [note] to the data store. */
    suspend fun insertNote(note: WorkTodayEntity)

    /**
     * Deletes all work notes that belong to the current day.
     * Called by the midnight alarm scheduler to reset the feed at the configured flush time.
     */
    suspend fun flushTodayEntries()
}