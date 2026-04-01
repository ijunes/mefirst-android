package com.ijunes.mefirst.today.repository

import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.today.data.TodayRepository
import kotlinx.coroutines.flow.Flow

class TodayRepositoryImpl(private val database: MeFirstDatabase): TodayRepository {

    override fun getAllNotes(): Flow<List<NoteEntity>> {
        return database.todayDao().getAll()
    }

    override suspend fun insertNote(note: NoteEntity) {
        database.todayDao().insert(note)
    }

    override suspend fun flushTodayEntries() {
        val entryList = database.todayDao().getAllOnce().map { note ->
            EntryEntity(
                timeStamp = note.timeStamp,
                text = note.noteText,
                mediaType = note.mediaType,
                mediaPath = note.mediaPath,
                waveformPath = note.waveformPath
            )
        }
        database.entriesDao().addAllNoteEntries(*entryList.toTypedArray())
        database.todayDao().deleteAll()
    }

    override suspend fun deleteTodayNote(timestamp: Long) {
        database.todayDao().delete(NoteEntity(timestamp))
    }

}