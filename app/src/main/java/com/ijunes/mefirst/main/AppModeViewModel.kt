package com.ijunes.mefirst.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ijunes.mefirst.common.state.ModeStateHolder
import kotlinx.coroutines.flow.StateFlow
import org.koin.java.KoinJavaComponent.inject

class AppModeViewModel(application: Application) : AndroidViewModel(application) {

    private val modeHolder: ModeStateHolder by inject(ModeStateHolder::class.java)

    val isWorkMode: StateFlow<Boolean> = modeHolder.isWorkMode

}
