package com.ijunes.mefirst.settings.alarm.receiver

import com.ijunes.mefirst.database.model.NoteMode
import com.ijunes.today.data.TodayRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimeFlushReceiverTest {

    private val mockRepo = mockk<TodayRepository>(relaxed = true)
    private val receiver = TimeFlushReceiver()

    @Test
    fun `flush calls flushTodayEntries for PERSONAL mode`() = runTest {
        receiver.flush(mockRepo)
        coVerify { mockRepo.flushTodayEntries(NoteMode.PERSONAL) }
    }

    @Test
    fun `flush calls flushTodayEntries for WORK mode`() = runTest {
        receiver.flush(mockRepo)
        coVerify { mockRepo.flushTodayEntries(NoteMode.WORK) }
    }

    @Test
    fun `flush calls both modes in a single invocation`() = runTest {
        receiver.flush(mockRepo)
        coVerify(exactly = 1) { mockRepo.flushTodayEntries(NoteMode.PERSONAL) }
        coVerify(exactly = 1) { mockRepo.flushTodayEntries(NoteMode.WORK) }
    }
}
