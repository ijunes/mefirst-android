package com.ijunes.mefirst.today.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.database.model.NoteMode
import com.ijunes.today.data.TodayRepository
import com.ijunes.today.domain.TodayAction
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class TodayScreenViewModelImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepo: TodayRepository
    private lateinit var mockModeHolder: ModeStateHolder
    private lateinit var mockApp: Application

    private val modeFlow = MutableStateFlow(false)

    private lateinit var viewModel: TodayScreenViewModelImpl

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockRepo = mockk(relaxed = true)
        mockModeHolder = mockk(relaxed = true) {
            every { isWorkMode } returns modeFlow
        }
        mockApp = mockk(relaxed = true) {
            every { filesDir } returns File(System.getProperty("java.io.tmpdir")!!)
            every { packageName } returns "com.ijunes.mefirst"
            every { checkSelfPermission(Manifest.permission.RECORD_AUDIO) } returns
                PackageManager.PERMISSION_DENIED
        }

        every { mockRepo.getAllNotes(any()) } returns emptyFlow()

        viewModel = TodayScreenViewModelImpl(mockApp, mockRepo, mockModeHolder)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ── SendChat ──────────────────────────────────────────────────────────────

    @Test
    fun `SendChat with text inserts personal note when in personal mode`() = runTest {
        modeFlow.value = false

        viewModel.handleEvent(MainAction.SendChat("hello"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.insertNote(match { it.noteText == "hello" && it.mode == NoteMode.PERSONAL }) }
    }

    @Test
    fun `SendChat with text inserts work note when in work mode`() = runTest {
        modeFlow.value = true

        viewModel.handleEvent(MainAction.SendChat("stand-up done"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.insertNote(match { it.noteText == "stand-up done" && it.mode == NoteMode.WORK }) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `SendChat empty without permission emits RequestRecordPermission`() = runTest {
        every {
            mockApp.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED

        var received: TodayAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { received = it }
        }

        viewModel.handleEvent(MainAction.SendChat(""))

        assertEquals(TodayAction.RequestRecordPermission, received)
        job.cancel()
    }

    // ── DeleteToday ───────────────────────────────────────────────────────────

    @Test
    fun `DeleteToday flushes personal repo in personal mode`() = runTest {
        modeFlow.value = false

        viewModel.handleEvent(MainAction.DeleteToday)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.flushTodayEntries(NoteMode.PERSONAL) }
    }

    @Test
    fun `DeleteToday flushes work repo in work mode`() = runTest {
        modeFlow.value = true

        viewModel.handleEvent(MainAction.DeleteToday)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.flushTodayEntries(NoteMode.WORK) }
    }

    // ── OpenGallery ───────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OpenGallery emits LaunchGallery command`() = runTest {
        var received: TodayAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { received = it }
        }

        viewModel.handleEvent(MainAction.OpenGallery)

        assertEquals(TodayAction.LaunchGallery, received)
        job.cancel()
    }

    // ── OpenCamera ────────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OpenCamera emits LaunchCamera command with URI from FileProvider`() = runTest {
        val fakeUri = mockk<Uri>()
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns fakeUri

        var received: TodayAction? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { received = it }
        }

        viewModel.handleEvent(MainAction.OpenCamera)

        assertEquals(TodayAction.LaunchCamera(fakeUri), received)
        job.cancel()
    }

    // ── SetWorkMode ───────────────────────────────────────────────────────────

    @Test
    fun `SetWorkMode delegates to ModeStateHolder`() {
        viewModel.handleEvent(MainAction.SetWorkMode(true))
        verify { mockModeHolder.setWorkMode(true) }
    }
}
