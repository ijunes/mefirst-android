package com.ijunes.mefirst.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ijunes.mefirst.common.state.ModeStateHolder
import kotlinx.coroutines.flow.StateFlow

class AppModeViewModel(
    application: Application,
    modeHolder: ModeStateHolder,
) : AndroidViewModel(application) {

    val isWorkMode: StateFlow<Boolean> = modeHolder.isWorkMode

}
