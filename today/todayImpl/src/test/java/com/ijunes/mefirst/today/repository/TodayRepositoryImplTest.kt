package com.ijunes.mefirst.today.repository

import com.ijunes.mefirst.data.dao.EntriesDao
import com.ijunes.mefirst.data.dao.TodayDao
import com.ijunes.mefirst.database.MeFirstDatabase
import com.ijunes.mefirst.database.entity.EntryEntity
import com.ijunes.mefirst.database.entity.NoteEntity
import com.ijunes.mefirst.database.model.MediaType
import com.ijunes.mefirst.database.model.NoteMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
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
        coEvery { mockTodayDao.getAllOnce(NoteMode.PERSONAL) } returns notes

        val insertedEntries = mutableListOf<EntryEntity>()
        every { mockEntriesDao.addAllNoteEntries(*anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            insertedEntries.addAll(args[0] as Array<EntryEntity>)
        }

        repo.flushTodayEntries(NoteMode.PERSONAL)

        Assert.assertEquals(2, insertedEntries.size)
        Assert.assertEquals(1000L, insertedEntries[0].timeStamp)
        Assert.assertEquals("note 1", insertedEntries[0].text)
        Assert.assertEquals(2000L, insertedEntries[1].timeStamp)
    }

    @Test
    fun `flushTodayEntries clears today table after inserting entries`() = runTest {
        coEvery { mockTodayDao.getAllOnce(NoteMode.PERSONAL) } returns listOf(NoteEntity(1L, "note", MediaType.TEXT))

        repo.flushTodayEntries(NoteMode.PERSONAL)

        verify { mockTodayDao.deleteAll(NoteMode.PERSONAL) }
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
        coEvery { mockTodayDao.getAllOnce(NoteMode.PERSONAL) } returns listOf(note)

        val insertedEntries = mutableListOf<EntryEntity>()
        every { mockEntriesDao.addAllNoteEntries(*anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            insertedEntries.addAll(args[0] as Array<EntryEntity>)
        }

        repo.flushTodayEntries(NoteMode.PERSONAL)

        val entry = insertedEntries.single()
        Assert.assertEquals(MediaType.VOICE, entry.mediaType)
        Assert.assertEquals("file://audio.m4a", entry.mediaPath)
        Assert.assertEquals("file://waveform.png", entry.waveformPath)
    }
}
