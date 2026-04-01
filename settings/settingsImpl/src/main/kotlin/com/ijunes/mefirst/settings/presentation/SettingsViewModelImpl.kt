package com.ijunes.mefirst.settings.presentation

import android.app.Application
import android.net.Uri
import com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler
import com.ijunes.mefirst.settings.backup.BackupManager
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.mefirst.settings.domain.SettingsAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class SettingsViewModelImpl(
    application: Application,
    private val settingsStateHolder: SettingsStateHolder,
    private val backupManager: BackupManager,
) : SettingsViewModel(application) {

    private val _activityCommands = MutableSharedFlow<SettingsAction>(extraBufferCapacity = 64)
    override val actions: SharedFlow<SettingsAction> = _activityCommands.asSharedFlow()

    private val _uiState = MutableStateFlow(
        SettingsUiState(hasPin = false)
    )
    override val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsStateHolder.flushHour,
                settingsStateHolder.flushMinute,
            ) { hour, minute -> hour to minute }
                .collect { (hour, minute) ->
                    _uiState.update {
                        it.copy(
                            flushHour = hour,
                            flushMinute = minute,
                            hasPin = settingsStateHolder.pinHash != null,
                        )
                    }
                }
        }
    }

    override fun setFlushTime(hour: Int, minute: Int) {
        settingsStateHolder.setFlushTime(hour, minute)
        MidnightAlarmScheduler.schedule(getApplication(), hour, minute)
    }

    override fun startBackup() {
        _activityCommands.tryEmit(SettingsAction.LaunchBackupPicker)
    }

    override fun performBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isBackingUp = true, backupResult = null) }
            try {
                backupManager.backup(uri)
                _uiState.update { it.copy(isBackingUp = false, backupResult = BackupResult.Success) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isBackingUp = false, backupResult = BackupResult.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    override fun startRestore() {
        _activityCommands.tryEmit(SettingsAction.LaunchRestorePicker)
    }

    override fun performRestore(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRestoring = true, backupResult = null) }
            try {
                backupManager.restore(uri)
                _uiState.update { it.copy(isRestoring = false, backupResult = BackupResult.Success) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRestoring = false, backupResult = BackupResult.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    override fun setPin(pin: String) {
        settingsStateHolder.setPin(pin)
        _uiState.update { it.copy(hasPin = true) }
    }

    override fun removePin(currentPin: String): Boolean {
        if (!settingsStateHolder.verifyPin(currentPin)) return false
        settingsStateHolder.removePin()
        _uiState.update { it.copy(hasPin = false) }
        return true
    }

    override fun verifyPin(pin: String): Boolean = settingsStateHolder.verifyPin(pin)

    override fun dismissBackupResult() {
        _uiState.update { it.copy(backupResult = null) }
    }
}
