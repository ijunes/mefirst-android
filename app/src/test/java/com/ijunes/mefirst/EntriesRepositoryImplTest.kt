package com.ijunes.mefirst

import com.ijunes.mefirst.common.data.database.MeFirstDatabase
import com.ijunes.mefirst.entries.data.EntriesDao
import com.ijunes.mefirst.entries.data.EntryEntity
import com.ijunes.mefirst.common.data.model.MediaType
import com.ijunes.mefirst.entries.repository.EntriesRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@Suppress("SameParameterValue")
class EntriesRepositoryImplTest {

    private lateinit var mockDao: EntriesDao
    private lateinit var mockDb: MeFirstDatabase
    private lateinit var repo: EntriesRepositoryImpl

    @Before
    fun setUp() {
        mockDao = mockk()
        mockDb = mockk { every { entriesDao() } returns mockDao }
        repo = EntriesRepositoryImpl(mockDb)
    }

    /** Returns midnight timestamp for the given date in the local timezone. */
    private fun midnightOf(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun entryAt(year: Int, month: Int, day: Int, hour: Int): EntryEntity {
        val ts = Calendar.getInstance().apply {
            set(year, month, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return EntryEntity(timeStamp = ts, text = "entry", mediaType = MediaType.TEXT)
    }

    @Test
    fun `getAllEntries groups two entries from the same day under one key`() = runTest {
        val morning = entryAt(2024, Calendar.MARCH, 10, 9)
        val evening = entryAt(2024, Calendar.MARCH, 10, 20)
        every { mockDao.getAllEntries() } returns flowOf(listOf(morning, evening))

        val result = repo.getAllEntries().first()

        assertEquals(1, result.size)
        assertEquals(2, result.values.first().size)
    }

    @Test
    fun `getAllEntries places entries from different days under separate keys`() = runTest {
        val dayOne = entryAt(2024, Calendar.MARCH, 10, 9)
        val dayTwo = entryAt(2024, Calendar.MARCH, 11, 9)
        every { mockDao.getAllEntries() } returns flowOf(listOf(dayOne, dayTwo))

        val result = repo.getAllEntries().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `getAllEntries key is normalized to midnight of the entry date`() = runTest {
        val entry = entryAt(2024, Calendar.MARCH, 10, 14)
        every { mockDao.getAllEntries() } returns flowOf(listOf(entry))

        val result = repo.getAllEntries().first()
        val key = result.keys.first()

        val cal = Calendar.getInstance().apply { timeInMillis = key }
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getAllEntries returns empty map when dao returns no entries`() = runTest {
        every { mockDao.getAllEntries() } returns flowOf(emptyList())

        val result = repo.getAllEntries().first()

        assertEquals(emptyMap<Long, List<EntryEntity>>(), result)
    }
}
