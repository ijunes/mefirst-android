package com.ijunes.today.data

import com.ijunes.mefirst.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access contract for personal (non-work) notes.
 *
 * Implementations are provided by `:today:todayImpl` and bound via Koin in `:today:todayApp`.
 */
interface TodayRepository {

    /**
     * Deletes a specific note by timestamp id that belongs to the current day.
     *
     * @param timestamp - id of the note to delete
     */
    suspend fun deleteTodayNote(timestamp: Long)

    /**
     * Returns a [Flow] that emits the full list of personal notes whenever the underlying data
     * changes. The flow remains active for the lifetime of the collector.
     *
     * @return [Flow] of [NoteEntity]s
     */
    suspend fun getAllNotes(): Flow<List<NoteEntity>>

    /** Persists a new personal [note] to the data store.
     *
     * @param note - note to persist
     */
    suspend fun insertNote(note: NoteEntity)

    /**
     * Deletes all personal notes that belong to the current day.
     * Called by the midnight alarm scheduler to reset the feed at the configured flush time.
     */
    suspend fun flushTodayEntries()



}