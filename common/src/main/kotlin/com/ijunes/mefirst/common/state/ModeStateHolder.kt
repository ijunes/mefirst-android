package com.ijunes.mefirst.common.state

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

class ModeStateHolder(context: Context) {

    private val prefs = context.getSharedPreferences("mode_prefs", Context.MODE_PRIVATE)

    private val _isWorkMode = MutableStateFlow(prefs.getBoolean("is_work_mode", false))
    val isWorkMode: StateFlow<Boolean> = _isWorkMode.asStateFlow()

    fun setWorkMode(isWork: Boolean) {
        _isWorkMode.value = isWork
        prefs.edit { putBoolean("is_work_mode", isWork) }
    }
}
