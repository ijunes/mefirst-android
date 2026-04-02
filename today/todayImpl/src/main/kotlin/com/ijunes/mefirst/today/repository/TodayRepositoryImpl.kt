package com.ijunes.mefirst.today.repository

import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.model.NoteMode
import com.ijunes.today.data.TodayRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TodayRepositoryImpl(private val database: MeFirstDatabase) : TodayRepository {

    override fun getAllNotes(mode: NoteMode): Flow<List<NoteEntity>> {
        return database.todayDao().getAll(mode)
    }

    override suspend fun insertNote(note: NoteEntity) {
        database.todayDao().insert(note)
    }

    override suspend fun flushTodayEntries(mode: NoteMode) {
        val entryList = database.todayDao().getAllOnce(mode).map { note ->
            EntryEntity(
                id = UUID.randomUUID().toString(),
                timeStamp = note.timeStamp,
                text = note.noteText,
                mediaType = note.mediaType,
                mediaPath = note.mediaPath,
                waveformPath = note.waveformPath,
                mode = mode,
            )
        }
        database.entriesDao().addAllNoteEntries(*entryList.toTypedArray())
        database.todayDao().deleteAll(mode)
    }

    override suspend fun deleteTodayNote(id: String) {
        database.todayDao().delete(id)
    }

}
