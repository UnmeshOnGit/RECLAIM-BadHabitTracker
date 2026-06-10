package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.HabitEntry
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "milestone_notifications"
    const val CHANNEL_NAME = "Milestone Reminders"
    const val CHANNEL_DESC = "Schedules morning and evening notifications to guide you toward your next recovery milestone."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun getMilestoneMessage(context: Context): Pair<String, String> {
        val database = AppDatabase.getDatabase(context)
        val habitsList = database.habitDao().getAllHabits().firstOrNull() ?: emptyList()
        val entriesList = database.habitEntryDao().getAllEntries().firstOrNull() ?: emptyList()

        val activeHabits = habitsList.filter { !it.isArchived }

        if (activeHabits.isEmpty()) {
            return Pair(
                "🌸 Start Your Journey Today",
                "Define a habit to track and reclaim control of your daily routine!"
            )
        }

        var maxCurrentStreak = 0
        var streakHabitName = ""

        for (habit in activeHabits) {
            val habitEntries = entriesList.filter { it.habitId == habit.id }
            val currentStreak = calculateCurrentStreak(habitEntries)
            if (currentStreak >= maxCurrentStreak) {
                maxCurrentStreak = currentStreak
                streakHabitName = habit.name
            }
        }

        // Determine next milestone
        val title: String
        val body: String

        if (maxCurrentStreak == 0) {
            title = "🛡️ Begin Your First Streak"
            body = "Log your first clean day for '$streakHabitName' on the calendar to work of your 3-day milestone!"
        } else if (maxCurrentStreak < 3) {
            val daysLeft = 3 - maxCurrentStreak
            title = "🌟 Short Walk to First Milestone"
            body = "Only $daysLeft more ${if (daysLeft == 1) "day" else "days"} of control for '$streakHabitName' to achieve your milestone: First Clean Steps! You can do this!"
        } else if (maxCurrentStreak < 7) {
            val daysLeft = 7 - maxCurrentStreak
            title = "⚡ Leveling Up Your Resiliency"
            body = "Keep pushed! Just $daysLeft ${if (daysLeft == 1) "day" else "days"} of control left to reach 'Iron Will' (7-day streak) for '$streakHabitName'!"
        } else if (maxCurrentStreak < 30) {
            val daysLeft = 30 - maxCurrentStreak
            title = "🔥 Habit Breaker in Sight"
            body = "Incredible streak of $maxCurrentStreak days! Only $daysLeft more ${if (daysLeft == 1) "day" else "days"} to unlock the legendary 'Habit Breaker' (30-day streak) for '$streakHabitName'!"
        } else {
            val nextTarget = 90
            val daysLeft = nextTarget - maxCurrentStreak
            title = "👑 Legendary Ascended Warrior"
            body = "Wow! You are 30+ days clean ($maxCurrentStreak days)! Stay aligned, only $daysLeft more days of pure control to conquer the 90-day milestone!"
        }

        return Pair(title, body)
    }

    private fun calculateCurrentStreak(entries: List<HabitEntry>): Int {
        if (entries.isEmpty()) return 0
        val sortedNewest = entries.sortedByDescending { it.date }
        var currentStreak = 0
        for (entry in sortedNewest) {
            if (entry.status == "Relapsed") {
                break
            } else {
                currentStreak++
            }
        }
        return currentStreak
    }

    fun showNotification(context: Context, title: String, content: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }

    fun scheduleDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. Morning Reminder (9 AM)
        scheduleTimeReminder(context, alarmManager, 9, 0, 1001, "morning_alarm")

        // 2. Evening Reminder (9 PM)
        scheduleTimeReminder(context, alarmManager, 21, 0, 1002, "evening_alarm")
    }

    private fun scheduleTimeReminder(
        context: Context,
        alarmManager: AlarmManager,
        hour: Int,
        minute: Int,
        requestCode: Int,
        action: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
