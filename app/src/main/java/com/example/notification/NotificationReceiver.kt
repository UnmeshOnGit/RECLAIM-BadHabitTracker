package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                    NotificationHelper.scheduleDailyReminders(context)
                } else {
                    val (title, body) = NotificationHelper.getMilestoneMessage(context)
                    NotificationHelper.showNotification(context, title, body)
                    
                    // Reschedule for the next day
                    NotificationHelper.scheduleDailyReminders(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
