package com.ijunes.mefirst.entries.presentation

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.ijunes.entries.data.EntriesRepository
import com.ijunes.entries.data.WorkEntriesRepository
import com.ijunes.entries.presentation.EntriesViewModel
import com.ijunes.mefirst.common.data.MessageItem
import com.ijunes.mefirst.common.state.ModeStateHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EntriesScreenViewModelImpl(
    private val personalRepo: EntriesRepository,
    private val workRepo: WorkEntriesRepository,
    private val modeHolder: ModeStateHolder,
) : EntriesViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val entries: StateFlow<Map<Long, List<MessageItem>>> = modeHolder.isWorkMode
        .flatMapLatest { isWork ->
            if (isWork) {
                workRepo.getAllEntries().map { grouped ->
                    grouped.mapValues { (_, dayEntries) ->
                        dayEntries.map {
                            MessageItem(
                                timeStamp = it.timeStamp,
                                text = it.text,
                                mediaType = it.mediaType,
                                mediaPath = it.mediaPath?.toUri(),
                                waveformPath = it.waveformPath?.toUri()
                            )
                        }
                    }
                }
            } else {
                personalRepo.getAllEntries().map { grouped ->
                    grouped.mapValues { (_, dayEntries) ->
                        dayEntries.map {
                            MessageItem(
                                timeStamp = it.timeStamp,
                                text = it.text,
                                mediaType = it.mediaType,
                                mediaPath = it.mediaPath?.toUri(),
                                waveformPath = it.waveformPath?.toUri()
                            )
                        }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
