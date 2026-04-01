package com.ijunes.mefirst.main

import com.ijunes.mefirst.common.data.Message

data class MainScreenUiState(
    val conversation: List<Message> = emptyList(),
    val entries: Map<Long, List<Message>> = emptyMap(),
    val isRecording: Boolean = false,
    val isWorkMode: Boolean = false,
)
