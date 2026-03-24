package com.ijunes.mefirst.today.data.repository

import com.ijunes.mefirst.common.data.database.MeFirstDatabase
import com.ijunes.mefirst.entries.data.EntryEntity
import com.ijunes.mefirst.today.data.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TodayRepository {

    suspend fun getAllNotes(): Flow<List<NoteEntity>>

    suspend fun insertNote(note: NoteEntity)

    suspend fun flushTodayEntries()

}

class TodayRepositoryImpl(private val database: MeFirstDatabase): TodayRepository {

    override suspend fun getAllNotes(): Flow<List<NoteEntity>> {
        return database.todayDao().getAll()
    }

    override suspend fun insertNote(note: NoteEntity) {
        database.todayDao().insert(note)
    }

    override suspend fun flushTodayEntries() {
        database.todayDao().getAll().map { noteList ->
            noteList.map { note ->
                EntryEntity(
                    timeStamp = note.timeStamp,
                    text = note.noteText,
                    mediaType = note.mediaType,
                    mediaPath = note.mediaPath,
                    waveformPath = note.waveformPath
                )
            }
        }.collect { entryList ->
            database.entriesDao().addAllNoteEntries(*entryList.toTypedArray())
            database.todayDao().deleteAll()
        }
    }

}