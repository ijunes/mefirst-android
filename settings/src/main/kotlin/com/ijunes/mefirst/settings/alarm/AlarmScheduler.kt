package com.ijunes.mefirst.settings.alarm

import android.content.Context

/**
 * Contract for scheduling the daily flush alarm.
 *
 * Defined in the `:settings` API module so that `:app` can trigger the initial alarm on startup
 * without a direct dependency on `:settings:settingsImpl`. The concrete implementation
 * ([com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler]) lives in `:settings:settingsImpl`
 * and is bound to this interface via Koin in `:settings:settingsApp`.
 */
interface AlarmScheduler {
    fun schedule(context: Context, hour: Int = 0, minute: Int = 0)
}
