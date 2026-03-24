package com.ijunes.mefirst.common.state

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class SettingsStateHolder(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _flushHour = MutableStateFlow(prefs.getInt("flush_hour", 0))
    val flushHour: StateFlow<Int> = _flushHour.asStateFlow()

    private val _flushMinute = MutableStateFlow(prefs.getInt("flush_minute", 0))
    val flushMinute: StateFlow<Int> = _flushMinute.asStateFlow()

    fun setFlushTime(hour: Int, minute: Int) {
        _flushHour.value = hour
        _flushMinute.value = minute
        prefs.edit {
            putInt("flush_hour", hour)
            putInt("flush_minute", minute)
        }
    }

    val pinHash: String? get() = prefs.getString("pin_hash", null)

    fun setPin(pin: String) {
        prefs.edit { putString("pin_hash", pin.sha256()) }
    }

    fun removePin() {
        prefs.edit { remove("pin_hash") }
    }

    fun verifyPin(pin: String): Boolean = pinHash != null && pin.sha256() == pinHash

    private fun String.sha256(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
