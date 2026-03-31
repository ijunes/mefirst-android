package com.ijunes.mefirst.entries.presentation

import com.ijunes.mefirst.common.data.MessageItem

data class EntriesScreenUiModel(
    val entries: Map<Long, List<MessageItem>>
)