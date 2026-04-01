package com.ijunes.mefirst.today.presentation

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.today.data.TodayRepository
import com.ijunes.today.data.WorkTodayRepository
import com.ijunes.today.domain.TodayAction
import io.mockk.coEvery
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for [TodayScreenViewModelImpl.handleEvent] dispatch logic.
 *
 * Repositories and [ModeStateHolder] are mocked and registered in a test Koin module under their
 * interface types, matching the production bindings in `:today:todayApp`.
 */
class TodayScreenViewModelImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockPersonalRepo: TodayRepository
    private lateinit var mockWorkRepo: WorkTodayRepository
    private lateinit var mockModeHolder: ModeStateHolder
    private lateinit var mockApp: Application

    private val modeFlow = MutableStateFlow(false)

    private lateinit var viewModel: TodayScreenViewModelImpl

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockPersonalRepo = mockk(relaxed = true)
        mockWorkRepo = mockk(relaxed = true)
        mockModeHolder = mockk(relaxed = true) {
            every { isWorkMode } returns modeFlow
        }
        mockApp = mockk(relaxed = true) {
            every { filesDir } returns File(System.getProperty("kotlin.io.tmpdir")!!)
            every { packageName } returns "com.ijunes.mefirst"
            every { checkSelfPermission(Manifest.permission.RECORD_AUDIO) } returns
                PackageManager.PERMISSION_DENIED
        }

        coEvery { mockPersonalRepo.getAllNotes() } returns emptyFlow()
        coEvery { mockWorkRepo.getAllNotes() } returns emptyFlow()

        viewModel = TodayScreenViewModelImpl(mockApp, mockPersonalRepo, mockWorkRepo, mockModeHolder)
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
        every { mockModeHolder.isWorkMode.value } returns false

        viewModel.handleEvent(MainAction.SendChat("hello"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockPersonalRepo.insertNote(match { it.noteText == "hello" }) }
    }

    @Test
    fun `SendChat with text inserts work note when in work mode`() = runTest {
        every { mockModeHolder.isWorkMode.value } returns true

        viewModel.handleEvent(MainAction.SendChat("stand-up done"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockWorkRepo.insertNote(match { it.noteText == "stand-up done" }) }
    }

    @Test
    fun `SendChat empty without permission emits RequestRecordPermission`() = runTest {
        every {
            mockApp.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED

        var received: TodayAction? = null
        val job = backgroundScope.launch { viewModel.actions.collect { received = it } }

        viewModel.handleEvent(MainAction.SendChat(""))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TodayAction.RequestRecordPermission, received)
        job.cancel()
    }

    // ── DeleteToday ───────────────────────────────────────────────────────────

    @Test
    fun `DeleteToday flushes personal repo in personal mode`() = runTest {
        every { mockModeHolder.isWorkMode.value } returns false

        viewModel.handleEvent(MainAction.DeleteToday)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockPersonalRepo.flushTodayEntries() }
    }

    @Test
    fun `DeleteToday flushes work repo in work mode`() = runTest {
        every { mockModeHolder.isWorkMode.value } returns true

        viewModel.handleEvent(MainAction.DeleteToday)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockWorkRepo.flushTodayEntries() }
    }

    // ── OpenGallery ───────────────────────────────────────────────────────────

    @Test
    fun `OpenGallery emits LaunchGallery command`() = runTest {
        var received: TodayAction? = null
        val job = backgroundScope.launch { viewModel.actions.collect { received = it } }

        viewModel.handleEvent(MainAction.OpenGallery)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TodayAction.LaunchGallery, received)
        job.cancel()
    }

    // ── OpenCamera ────────────────────────────────────────────────────────────

    @Test
    fun `OpenCamera emits LaunchCamera command with URI from FileProvider`() = runTest {
        val fakeUri = mockk<Uri>()
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns fakeUri

        var received: TodayAction? = null
        val job = backgroundScope.launch { viewModel.actions.collect { received = it } }

        viewModel.handleEvent(MainAction.OpenCamera)
        testDispatcher.scheduler.advanceUntilIdle()

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
