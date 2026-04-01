package com.ijunes.mefirst.today.repository

import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.WorkEntryEntity
import com.ijunes.mefirst.database.entity.WorkTodayEntity
import com.ijunes.today.data.WorkTodayRepository
import kotlinx.coroutines.flow.Flow

class WorkTodayRepositoryImpl(private val database: MeFirstDatabase) : WorkTodayRepository {

    override fun getAllNotes(): Flow<List<WorkTodayEntity>> {
        return database.workTodayDao().getAll()
    }

    override suspend fun insertNote(note: WorkTodayEntity) {
        database.workTodayDao().insert(note)
    }

    override suspend fun flushTodayEntries() {
        val entryList = database.workTodayDao().getAllOnce().map { note ->
            WorkEntryEntity(
                timeStamp = note.timeStamp,
                text = note.noteText,
                mediaType = note.mediaType,
                mediaPath = note.mediaPath,
                waveformPath = note.waveformPath
            )
        }
        database.workEntriesDao().addAllNoteEntries(*entryList.toTypedArray())
        database.workTodayDao().deleteAll()
    }

    override suspend fun deleteTodayNote(timestamp: Long) {
        database.workTodayDao().delete(WorkTodayEntity(timestamp))
    }
}
