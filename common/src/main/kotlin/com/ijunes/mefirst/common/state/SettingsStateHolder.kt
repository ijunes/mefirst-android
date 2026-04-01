package com.ijunes.mefirst.common.state

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

private object Settings {
    const val PREFS_NAME = "app_prefs"
    const val FLUSH_HOUR = "flush_hour"
    const val FLUSH_MINUTE = "flush_minute"
    const val PIN_HASH = "pin_hash"
}

class SettingsStateHolder(context: Context) {

    private val prefs = context.getSharedPreferences(Settings.PREFS_NAME, Context.MODE_PRIVATE)

    private val _flushHour = MutableStateFlow(prefs.getInt(Settings.FLUSH_HOUR, 0))
    val flushHour: StateFlow<Int> = _flushHour.asStateFlow()

    private val _flushMinute = MutableStateFlow(prefs.getInt(Settings.FLUSH_MINUTE, 0))
    val flushMinute: StateFlow<Int> = _flushMinute.asStateFlow()

    fun setFlushTime(hour: Int, minute: Int) {
        _flushHour.value = hour
        _flushMinute.value = minute
        prefs.edit {
            putInt(Settings.FLUSH_HOUR, hour)
            putInt(Settings.FLUSH_MINUTE, minute)
        }
    }

    val pinHash: String? get() = prefs.getString(Settings.PIN_HASH, null)

    fun setPin(pin: String) {
        prefs.edit { putString(Settings.PIN_HASH, pin.sha256()) }
    }

    fun removePin() {
        prefs.edit { remove(Settings.PIN_HASH) }
    }

    fun verifyPin(pin: String): Boolean = pinHash != null && pin.sha256() == pinHash

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
