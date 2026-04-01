package com.ijunes.mefirst.common.state

import android.content.Context
import androidx.core.content.edit

class OnboardingStateHolder(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    val isOnboardingComplete: Boolean
        get() = prefs.getBoolean(ONBOARDING_COMPLETE, false)

    fun markComplete() {
        prefs.edit { putBoolean(ONBOARDING_COMPLETE, true) }
    }

    companion object {
        const val ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
