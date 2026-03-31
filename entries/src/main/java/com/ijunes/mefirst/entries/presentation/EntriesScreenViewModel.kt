package com.ijunes.mefirst.entries.presentation

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ijunes.mefirst.entries.repository.EntriesRepository
import com.ijunes.mefirst.entries.repository.EntriesRepositoryImpl
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.entries.repository.WorkEntriesRepository
import com.ijunes.mefirst.entries.repository.WorkEntriesRepositoryImpl
import com.ijunes.mefirst.common.data.MessageItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import org.koin.java.KoinJavaComponent.inject

class EntriesScreenViewModel : ViewModel() {

    private val personalRepo: EntriesRepository by inject(EntriesRepositoryImpl::class.java)
    private val workRepo: WorkEntriesRepository by inject(WorkEntriesRepositoryImpl::class.java)
    private val modeHolder: ModeStateHolder by inject(ModeStateHolder::class.java)

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: StateFlow<Map<Long, List<MessageItem>>> = modeHolder.isWorkMode
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
