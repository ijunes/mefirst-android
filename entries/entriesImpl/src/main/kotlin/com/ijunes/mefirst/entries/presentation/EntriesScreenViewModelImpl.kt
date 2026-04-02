package com.ijunes.mefirst.entries.presentation

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.ijunes.entries.data.EntriesRepository
import com.ijunes.entries.presentation.EntriesViewModel
import com.ijunes.mefirst.common.data.Message
import com.ijunes.mefirst.common.state.ModeStateHolder
import com.ijunes.mefirst.database.model.NoteMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EntriesScreenViewModelImpl(
    private val repo: EntriesRepository,
    private val modeHolder: ModeStateHolder,
) : EntriesViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val entries: StateFlow<Map<Long, List<Message>>> = modeHolder.isWorkMode
        .flatMapLatest { isWork ->
            val mode = if (isWork) NoteMode.WORK else NoteMode.PERSONAL
            repo.getAllEntries(mode).map { grouped ->
                grouped.mapValues { (_, dayEntries) ->
                    dayEntries.map {
                        Message(
                            id = it.id,
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
        .catch { e -> Log.e("EntriesViewModel", "Failed to load entries", e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
