package com.ijunes.mefirst.settings.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import org.koin.java.KoinJavaComponent.inject

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsStateHolder: SettingsStateHolder by inject(SettingsStateHolder::class.java)
    private val backupManager: BackupManager by inject(BackupManager::class.java)

    private val _activityCommands = MutableSharedFlow<SettingsAction>(extraBufferCapacity = 1)
    val activityCommands: SharedFlow<SettingsAction> = _activityCommands.asSharedFlow()

    private val _uiState = MutableStateFlow(
        SettingsUiState(hasPin = false)
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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

    fun setFlushTime(hour: Int, minute: Int) {
        settingsStateHolder.setFlushTime(hour, minute)
        MidnightAlarmScheduler.schedule(getApplication(), hour, minute)
    }

    fun startBackup() {
        _activityCommands.tryEmit(SettingsAction.LaunchBackupPicker)
    }

    fun performBackup(uri: Uri) {
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

    fun startRestore() {
        _activityCommands.tryEmit(SettingsAction.LaunchRestorePicker)
    }

    fun performRestore(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRestoring = true, backupResult = null) }
            try {
                backupManager.restore(uri)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRestoring = false, backupResult = BackupResult.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    fun setPin(pin: String) {
        settingsStateHolder.setPin(pin)
        _uiState.update { it.copy(hasPin = true) }
    }

    fun removePin(currentPin: String): Boolean {
        if (!settingsStateHolder.verifyPin(currentPin)) return false
        settingsStateHolder.removePin()
        _uiState.update { it.copy(hasPin = false) }
        return true
    }

    fun verifyPin(pin: String): Boolean = settingsStateHolder.verifyPin(pin)

    fun dismissBackupResult() {
        _uiState.update { it.copy(backupResult = null) }
    }
}
