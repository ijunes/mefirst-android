package com.ijunes.mefirst.settings.presentation

import android.app.Application
import android.net.Uri
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.mefirst.settings.backup.BackupManager
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockApp: Application
    private lateinit var mockStateHolder: SettingsStateHolder
    private lateinit var mockBackupManager: BackupManager
    private lateinit var viewModel: SettingsViewModelImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockApp = mockk(relaxed = true)
        mockStateHolder = mockk(relaxed = true) {
            every { flushHour } returns MutableStateFlow(0)
            every { flushMinute } returns MutableStateFlow(0)
            every { pinHash } returns null
        }
        mockBackupManager = mockk(relaxed = true)

        viewModel = SettingsViewModelImpl(mockApp, mockStateHolder, mockBackupManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ── performBackup ─────────────────────────────────────────────────────────

    @Test
    fun `performBackup calls backup manager and sets Success result`() = runTest {
        val uri = mockk<Uri>()
        every { mockBackupManager.backup(uri) } just runs

        viewModel.performBackup(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isBackingUp)
        assertEquals(BackupResult.Success, state.backupResult)
    }

    @Test
    fun `performBackup sets Error result when backup throws`() = runTest {
        val uri = mockk<Uri>()
        every { mockBackupManager.backup(uri) } throws RuntimeException("disk full")

        viewModel.performBackup(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isBackingUp)
        assertEquals(BackupResult.Error("disk full"), state.backupResult)
    }

    // ── performRestore ────────────────────────────────────────────────────────

    @Test
    fun `performRestore calls backup manager and sets Success result`() = runTest {
        val uri = mockk<Uri>()
        every { mockBackupManager.restore(uri) } just runs

        viewModel.performRestore(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRestoring)
        assertEquals(BackupResult.Success, state.backupResult)
    }

    @Test
    fun `performRestore sets Error result when restore throws`() = runTest {
        val uri = mockk<Uri>()
        every { mockBackupManager.restore(uri) } throws RuntimeException("corrupt zip")

        viewModel.performRestore(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRestoring)
        assertEquals(BackupResult.Error("corrupt zip"), state.backupResult)
    }

    // ── dismissBackupResult ───────────────────────────────────────────────────

    @Test
    fun `dismissBackupResult clears backupResult`() = runTest {
        val uri = mockk<Uri>()
        every { mockBackupManager.backup(uri) } just runs
        viewModel.performBackup(uri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissBackupResult()

        assertNull(viewModel.uiState.value.backupResult)
    }

    // ── PIN ───────────────────────────────────────────────────────────────────

    @Test
    fun `setPin delegates to stateHolder and sets hasPin to true`() {
        viewModel.setPin("1234")

        verify { mockStateHolder.setPin("1234") }
        assertTrue(viewModel.uiState.value.hasPin)
    }

    @Test
    fun `removePin returns false and does not remove when pin is wrong`() {
        every { mockStateHolder.verifyPin("0000") } returns false

        val result = viewModel.removePin("0000")

        assertFalse(result)
        verify(exactly = 0) { mockStateHolder.removePin() }
    }

    @Test
    fun `removePin returns true and clears hasPin when pin is correct`() {
        every { mockStateHolder.verifyPin("1234") } returns true

        val result = viewModel.removePin("1234")

        assertTrue(result)
        verify { mockStateHolder.removePin() }
        assertFalse(viewModel.uiState.value.hasPin)
    }

    @Test
    fun `verifyPin delegates to stateHolder`() {
        every { mockStateHolder.verifyPin("5678") } returns true
        assertTrue(viewModel.verifyPin("5678"))

        every { mockStateHolder.verifyPin("0000") } returns false
        assertFalse(viewModel.verifyPin("0000"))
    }
}
