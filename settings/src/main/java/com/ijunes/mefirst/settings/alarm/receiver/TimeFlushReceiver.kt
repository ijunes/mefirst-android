package com.ijunes.mefirst.settings.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.today.data.TodayRepository
import com.ijunes.today.data.WorkTodayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

class TimeFlushReceiver : BroadcastReceiver() {

    private val todayRepo: TodayRepository by inject(TodayRepository::class.java)
    private val workTodayRepo: WorkTodayRepository by inject(WorkTodayRepository::class.java)
    private val settingsStateHolder: SettingsStateHolder by inject(SettingsStateHolder::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MidnightAlarmScheduler.ACTION_MIDNIGHT_FLUSH -> {
                val result = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        todayRepo.flushTodayEntries()
                        workTodayRepo.flushTodayEntries()
                    } finally {
                        result.finish()
                    }
                }
                MidnightAlarmScheduler.schedule(
                    context,
                    settingsStateHolder.flushHour.value,
                    settingsStateHolder.flushMinute.value,
                )
            }
            Intent.ACTION_BOOT_COMPLETED -> MidnightAlarmScheduler.schedule(
                context,
                settingsStateHolder.flushHour.value,
                settingsStateHolder.flushMinute.value,
            )
        }
    }
}
