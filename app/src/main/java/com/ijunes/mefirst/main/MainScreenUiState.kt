package com.ijunes.mefirst.main

import com.ijunes.mefirst.common.data.MessageItem

data class MainScreenUiState(
    val conversation: List<MessageItem> = emptyList(),
    val entries: Map<Long, List<MessageItem>> = emptyMap(),
    val isRecording: Boolean = false,
    val isWorkMode: Boolean = false,
)
