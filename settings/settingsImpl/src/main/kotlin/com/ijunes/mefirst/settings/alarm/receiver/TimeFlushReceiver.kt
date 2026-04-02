package com.ijunes.mefirst.settings.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ijunes.mefirst.settings.alarm.MidnightAlarmScheduler
import com.ijunes.mefirst.common.state.SettingsStateHolder
import com.ijunes.mefirst.database.model.NoteMode
import com.ijunes.today.data.TodayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

class TimeFlushReceiver : BroadcastReceiver() {

    // BroadcastReceivers cannot use constructor injection because Android instantiates them
    // directly. KoinJavaComponent.inject() is the standard Koin workaround for this constraint.
    private val todayRepo: TodayRepository by inject(TodayRepository::class.java)
    private val settingsStateHolder: SettingsStateHolder by inject(SettingsStateHolder::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MidnightAlarmScheduler.ACTION_MIDNIGHT_FLUSH -> {
                val result = goAsync()
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                scope.launch {
                    try {
                        flush(todayRepo)
                    } finally {
                        result.finish()
                        scope.cancel()
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

    // Internal visibility allows unit tests to call flush() directly with a mock repo,
    // working around the BroadcastReceiver constructor injection constraint.
    internal suspend fun flush(repo: TodayRepository) {
        repo.flushTodayEntries(NoteMode.PERSONAL)
        repo.flushTodayEntries(NoteMode.WORK)
    }
}
