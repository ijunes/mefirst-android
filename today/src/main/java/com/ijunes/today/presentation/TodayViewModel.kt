package com.ijunes.today.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.ijunes.mefirst.common.action.MainAction
import com.ijunes.mefirst.common.data.MessageItem
import com.ijunes.today.domain.TodayAction
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class TodayViewModel(application: Application) : AndroidViewModel(application) {
    abstract val conversation: StateFlow<List<MessageItem>>
    abstract val isRecording: StateFlow<Boolean>
    abstract val activityCommands: SharedFlow<TodayAction>
    abstract fun handleEvent(event: MainAction)
    abstract fun startRecording()
    abstract fun insertImageNote(uri: Uri)
}
