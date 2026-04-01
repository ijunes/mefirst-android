package com.ijunes.entries.presentation

import com.ijunes.mefirst.common.data.Message

data class EntriesScreenUiModel(
    val entries: Map<Long, List<Message>>
)
