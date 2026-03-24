package com.ijunes.mefirst

import com.ijunes.mefirst.common.data.database.MeFirstDatabase
import com.ijunes.mefirst.entries.data.EntriesDao
import com.ijunes.mefirst.today.data.dao.TodayDao
import com.ijunes.mefirst.entries.data.EntryEntity
import com.ijunes.mefirst.common.data.model.MediaType
import com.ijunes.mefirst.today.data.NoteEntity
import com.ijunes.mefirst.today.data.repository.TodayRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TodayRepositoryImplTest {

    private lateinit var mockTodayDao: TodayDao
    private lateinit var mockEntriesDao: EntriesDao
    private lateinit var mockDb: MeFirstDatabase
    private lateinit var repo: TodayRepositoryImpl

    @Before
    fun setUp() {
        mockTodayDao = mockk(relaxed = true)
        mockEntriesDao = mockk(relaxed = true)
        mockDb = mockk {
            every { todayDao() } returns mockTodayDao
            every { entriesDao() } returns mockEntriesDao
        }
        repo = TodayRepositoryImpl(mockDb)
    }

    @Test
    fun `insertNote delegates to todayDao insert`() = runTest {
        val note = NoteEntity(timeStamp = 1L, noteText = "hello")
        repo.insertNote(note)
        verify { mockTodayDao.insert(note) }
    }

    @Test
    fun `flushTodayEntries calls addAllNoteEntries with converted entries`() = runTest {
        val notes = listOf(
            NoteEntity(1000L, "note 1", MediaType.TEXT),
            NoteEntity(2000L, "note 2", MediaType.TEXT),
        )
        every { mockTodayDao.getAll() } returns flowOf(notes)

        val insertedEntries = mutableListOf<EntryEntity>()
        every { mockEntriesDao.addAllNoteEntries(*anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            insertedEntries.addAll(args[0] as Array<EntryEntity>)
        }

        repo.flushTodayEntries()

        assertEquals(2, insertedEntries.size)
        assertEquals(1000L, insertedEntries[0].timeStamp)
        assertEquals("note 1", insertedEntries[0].text)
        assertEquals(2000L, insertedEntries[1].timeStamp)
    }

    @Test
    fun `flushTodayEntries clears today table after inserting entries`() = runTest {
        every { mockTodayDao.getAll() } returns flowOf(
            listOf(NoteEntity(1L, "note", MediaType.TEXT))
        )

        repo.flushTodayEntries()

        verify { mockTodayDao.deleteAll() }
    }

    @Test
    fun `flushTodayEntries preserves all media fields when converting to EntryEntity`() = runTest {
        val note = NoteEntity(
            timeStamp = 500L,
            noteText = null,
            mediaType = MediaType.VOICE,
            mediaPath = "file://audio.m4a",
            waveformPath = "file://waveform.png"
        )
        every { mockTodayDao.getAll() } returns flowOf(listOf(note))

        val insertedEntries = mutableListOf<EntryEntity>()
        every { mockEntriesDao.addAllNoteEntries(*anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            insertedEntries.addAll(args[0] as Array<EntryEntity>)
        }

        repo.flushTodayEntries()

        val entry = insertedEntries.single()
        assertEquals(MediaType.VOICE, entry.mediaType)
        assertEquals("file://audio.m4a", entry.mediaPath)
        assertEquals("file://waveform.png", entry.waveformPath)
    }
}
