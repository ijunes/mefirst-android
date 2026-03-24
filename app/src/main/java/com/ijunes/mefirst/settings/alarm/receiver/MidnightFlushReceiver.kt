package com.ijunes.mefirst.settings.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.mefirst.today.data.repository.TodayRepositoryImpl
import com.ijunes.mefirst.today.data.repository.WorkTodayRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class MidnightFlushReceiver : BroadcastReceiver() {

    private val todayRepo: TodayRepositoryImpl by inject(TodayRepositoryImpl::class.java)
    private val workTodayRepo: WorkTodayRepositoryImpl by inject(WorkTodayRepositoryImpl::class.java)
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
