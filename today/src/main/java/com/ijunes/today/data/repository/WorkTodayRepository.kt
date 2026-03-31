package com.ijunes.today.data.repository

import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.WorkEntryEntity
import com.ijunes.mefirst.database.entity.WorkTodayEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

interface WorkTodayRepository {

    suspend fun getAllNotes(): Flow<List<WorkTodayEntity>>

    suspend fun insertNote(note: WorkTodayEntity)

    suspend fun flushTodayEntries()
}

class WorkTodayRepositoryImpl(private val database: MeFirstDatabase) : WorkTodayRepository {

    override suspend fun getAllNotes(): Flow<List<WorkTodayEntity>> {
        return database.workTodayDao().getAll()
    }

    override suspend fun insertNote(note: WorkTodayEntity) {
        database.workTodayDao().insert(note)
    }

    override suspend fun flushTodayEntries() {
        database.workTodayDao().getAll().map { noteList ->
            noteList.map { note ->
                WorkEntryEntity(
                    timeStamp = note.timeStamp,
                    text = note.noteText,
                    mediaType = note.mediaType,
                    mediaPath = note.mediaPath,
                    waveformPath = note.waveformPath
                )
            }
        }.collect { entryList ->
            database.workEntriesDao().addAllNoteEntries(*entryList.toTypedArray())
            database.workTodayDao().deleteAll()
        }
    }
}
