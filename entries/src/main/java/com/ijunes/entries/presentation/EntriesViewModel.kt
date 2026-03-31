package com.ijunes.entries.presentation

import androidx.lifecycle.ViewModel
import com.ijunes.mefirst.common.data.MessageItem
import kotlinx.coroutines.flow.StateFlow

abstract class EntriesViewModel : ViewModel() {
    abstract val entries: StateFlow<Map<Long, List<MessageItem>>>
}
