package com.ijunes.mefirst.entries.repository

import com.ijunes.entries.data.EntriesRepository
import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.model.NoteMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntriesRepositoryImpl(private val database: MeFirstDatabase) : EntriesRepository {

    override fun getAllEntriesByDate(
        startOfDay: Long,
        endOfDay: Long,
        mode: NoteMode,
    ): Flow<List<EntryEntity>> {
        return database.entriesDao().getEntriesByDate(startOfDay, endOfDay, mode)
    }

    // Groups all entries in memory after a single DB fetch. Acceptable given personal-scale
    // datasets (typically <500 entries). If entry counts grow significantly, push the date
    // truncation into SQL via a generated column or Room view.
    override fun getAllEntries(mode: NoteMode): Flow<Map<Long, List<EntryEntity>>> {
        return database.entriesDao().getAllEntries(mode).map { entries ->
            entries.groupBy { entry -> normalizeToMidnight(entry.timeStamp) }
        }
    }
}
