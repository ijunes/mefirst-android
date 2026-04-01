package com.ijunes.mefirst.main

import android.net.Uri
import com.ijunes.mefirst.common.data.Message

/**
 * Aggregated UI state for [MainScreen], produced in [MainActivity] by combining flows from
 * the active ViewModels.
 *
 * @property conversation Ordered list of today's notes for the active mode, sourced from
 * [com.ijunes.today.presentation.TodayViewModel.conversation].
 * @property entries Past journal entries grouped by day (epoch-ms key), sourced from
 * [com.ijunes.entries.presentation.EntriesViewModel.entries].
 * @property isRecording Whether a voice recording is currently in progress, sourced from
 * [com.ijunes.today.presentation.TodayViewModel.isRecording].
 * @property isWorkMode Whether the app is in work mode, sourced from
 * [AppModeViewModel.isWorkMode]. Controls theming and which data repository is active.
 * @property pendingImageUri An image URI staged for sending but not yet persisted, sourced from
 * [com.ijunes.today.presentation.TodayViewModel.pendingImageUri]. Non-null while the user has
 * selected an image (gallery or camera) but has not yet tapped Send.
 */
data class MainScreenUiState(
    val conversation: List<Message> = emptyList(),
    val entries: Map<Long, List<Message>> = emptyMap(),
    val isRecording: Boolean = false,
    val isWorkMode: Boolean = false,
    val pendingImageUri: Uri? = null,
)
