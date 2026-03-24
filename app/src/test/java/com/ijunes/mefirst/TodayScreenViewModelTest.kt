package com.ijunes.mefirst

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.today.data.repository.TodayRepositoryImpl
import com.ijunes.mefirst.today.data.repository.WorkTodayRepositoryImpl
import com.ijunes.mefirst.today.domain.TodayAction
import com.ijunes.mefirst.main.MainAction
import com.ijunes.mefirst.today.presentation.TodayScreenViewModel
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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.io.File

/**
 * Unit tests for [TodayScreenViewModel.handleEvent] dispatch logic.
 *
 * The ViewModel resolves its dependencies via KoinJavaComponent using the *concrete*
 * implementation class as the key, so each mock is registered in the test Koin module
 * under its concrete class.
 */
class TodayScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockPersonalRepo: TodayRepositoryImpl
    private lateinit var mockWorkRepo: WorkTodayRepositoryImpl
    private lateinit var mockModeHolder: ModeStateHolder
    private lateinit var mockApp: Application

    private val modeFlow = MutableStateFlow(false)

    private lateinit var viewModel: TodayScreenViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Relaxed mocks: un-stubbed calls return safe defaults (emptyFlow, Unit, etc.)
        mockPersonalRepo = mockk(relaxed = true)
        mockWorkRepo = mockk(relaxed = true)
        mockModeHolder = mockk(relaxed = true) {
            every { isWorkMode } returns modeFlow
        }
        mockApp = mockk(relaxed = true) {
            every { filesDir } returns File(System.getProperty("java.io.tmpdir")!!)
            every { packageName } returns "com.ijunes.mefirst"
            every { checkSelfPermission(Manifest.permission.RECORD_AUDIO) } returns
                PackageManager.PERMISSION_DENIED
        }

        // Ensure getAllNotes() returns an empty flow so stateIn doesn't get null
        coEvery { mockPersonalRepo.getAllNotes() } returns emptyFlow()
        coEvery { mockWorkRepo.getAllNotes() } returns emptyFlow()

        startKoin {
            modules(module {
                factory<TodayRepositoryImpl> { mockPersonalRepo }
                factory<WorkTodayRepositoryImpl> { mockWorkRepo }
                single { mockModeHolder }
            })
        }

        viewModel = TodayScreenViewModel(mockApp)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        stopKoin()
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
        val job = backgroundScope.launch { viewModel.activityCommands.collect { received = it } }

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
        val job = backgroundScope.launch { viewModel.activityCommands.collect { received = it } }

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
        val job = backgroundScope.launch { viewModel.activityCommands.collect { received = it } }

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
