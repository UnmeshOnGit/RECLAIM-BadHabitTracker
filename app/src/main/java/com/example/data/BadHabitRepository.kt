package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int
)

class BadHabitRepository(private val db: AppDatabase) {

    val allHabits: Flow<List<Habit>> = db.habitDao().getAllHabits()
    val allEntries: Flow<List<HabitEntry>> = db.habitEntryDao().getAllEntries()
    val allNotes: Flow<List<Note>> = db.noteDao().getAllNotes()
    val allPreferences: Flow<List<UserPreference>> = db.userPreferenceDao().getAllPreferencesFlow()
    val allAchievements: Flow<List<Achievement>> = db.achievementDao().getAllAchievements()

    // Habit Operations
    suspend fun getHabitById(id: Int): Habit? = db.habitDao().getHabitById(id)
    
    suspend fun insertHabit(habit: Habit): Long {
        val habitId = db.habitDao().insertHabit(habit)
        checkAndUnlockAchievements()
        return habitId
    }

    suspend fun updateHabit(habit: Habit) {
        db.habitDao().updateHabit(habit)
        checkAndUnlockAchievements()
    }

    suspend fun deleteHabit(habit: Habit) {
        // Also delete entries and notes associated with the habit
        db.habitEntryDao().deleteEntriesForHabit(habit.id)
        db.habitDao().deleteHabit(habit)
    }

    suspend fun deleteHabitById(id: Int) {
        db.habitEntryDao().deleteEntriesForHabit(id)
        db.habitDao().deleteHabitById(id)
    }

    // Entry Operations
    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>> = 
        db.habitEntryDao().getEntriesForHabit(habitId)

    suspend fun logHabitEntry(entry: HabitEntry): Long {
        val existing = db.habitEntryDao().getEntryForHabitAndDate(entry.habitId, entry.date)
        val id = if (existing != null) {
            val updated = entry.copy(id = existing.id)
            db.habitEntryDao().updateEntry(updated)
            existing.id.toLong()
        } else {
            db.habitEntryDao().insertEntry(entry)
        }
        
        // Re-evaluate achievements whenever entries are logged
        checkAndUnlockAchievements()
        // Re-calculate recovery score
        updateCalculatedRecoveryScore()
        
        return id
    }

    // Note Operations
    fun getNotesForHabit(habitId: Int): Flow<List<Note>> = db.noteDao().getNotesForHabit(habitId)
    
    suspend fun insertNote(note: Note): Long {
        val noteId = db.noteDao().insertNote(note)
        checkAndUnlockAchievements()
        return noteId
    }

    suspend fun deleteNote(note: Note) = db.noteDao().deleteNote(note)

    // User Preferences / Profile Operations
    suspend fun getPreference(key: String, defaultValue: String): String {
        return db.userPreferenceDao().getPreferenceValue(key) ?: defaultValue
    }

    suspend fun savePreference(key: String, value: String) {
        db.userPreferenceDao().insertPreference(UserPreference(key, value))
        if (key == "username" || key == "recovery_goals") {
            updateCalculatedRecoveryScore()
        }
    }

    // Dynamic computations
    fun calculateStreaksForHabit(entries: List<HabitEntry>): StreakData {
        if (entries.isEmpty()) return StreakData(0, 0)
        
        // Sort from oldest to newest to calculate max streak
        val sortedOldest = entries.sortedBy { it.date }
        var maxStreak = 0
        var tempStreak = 0
        
        for (entry in sortedOldest) {
            // "UrgeButControlled", "FeltUrge", "Neutral", "Clean" are non-relapses. Only "Relapsed" resets streak.
            if (entry.status != "Relapsed") {
                tempStreak++
                if (tempStreak > maxStreak) {
                    maxStreak = tempStreak
                }
            } else {
                tempStreak = 0
            }
        }
        
        // Sort from newest to oldest to calculate current streak
        val sortedNewest = entries.sortedByDescending { it.date }
        var currentStreak = 0
        for (entry in sortedNewest) {
            if (entry.status == "Relapsed") {
                break
            } else {
                currentStreak++
            }
        }
        
        return StreakData(currentStreak, maxStreak)
    }

    // Seeding default achievements and setup meta
    suspend fun seedDatabase() {
        val defaultAchievements = listOf(
            Achievement("3_days_clean", "First Clean Steps", "Maintain a 3-day clean streak on any habit"),
            Achievement("1_week_streak", "Iron Will", "Maintain a 7-day clean streak on any habit"),
            Achievement("30_days_clean", "Habit Breaker", "Maintain a 30-day clean streak on any habit"),
            Achievement("10_urges_defeated", "Mind Control", "Successfully resist and control 10 strong urges"),
            Achievement("30_urges_defeated", "Unstoppable", "Successfully resist and control 30 strong urges"),
            Achievement("first_journal", "Scribe of Recovery", "Write your first daily reflection journal note"),
            Achievement("architect_goal", "Architect of Fate", "Tackle 3 or more bad habits simultaneously")
        )
        db.achievementDao().insertAchievements(defaultAchievements)

        // Seed some starter goals if preferences are empty
        if (db.userPreferenceDao().getPreferenceValue("username") == null) {
            db.userPreferenceDao().insertPreference(UserPreference("username", "Guest Warrior"))
            db.userPreferenceDao().insertPreference(UserPreference("motivation_quote", "The secret of change is to focus all of your energy, not on fighting the old, but on building the new."))
            db.userPreferenceDao().insertPreference("recovery_goals", "Stay clean and reclaim control over my life.")
            db.userPreferenceDao().insertPreference("recovery_score", "50")
            db.userPreferenceDao().insertPreference("avatar", "shield")
        }
    }

    private suspend fun dbInsertPreferenceHelper(key: String, value: String) {
        db.userPreferenceDao().insertPreference(UserPreference(key, value))
    }

    suspend fun checkAndUnlockAchievements() {
        val now = System.currentTimeMillis()
        val habits = db.habitDao().getAllHabits().firstOrNull() ?: emptyList()
        val entries = db.habitEntryDao().getAllEntries().firstOrNull() ?: emptyList()
        val notes = db.noteDao().getAllNotes().firstOrNull() ?: emptyList()

        // 1. Architect achievement: 3 or more habits
        if (habits.size >= 3) {
            db.achievementDao().unlockAchievement("architect_goal", now)
        }

        // 2. Journal achievement: at least 1 note
        if (notes.isNotEmpty()) {
            db.achievementDao().unlockAchievement("first_journal", now)
        }

        // Calculate streaks and controlled urges per habit
        var maxStreakAcrossAll = 0
        var totalUrgesControlled = 0

        for (habit in habits) {
            val habitEntries = entries.filter { it.habitId == habit.id }
            val streak = calculateStreaksForHabit(habitEntries)
            if (streak.longestStreak > maxStreakAcrossAll) {
                maxStreakAcrossAll = streak.longestStreak
            }
            totalUrgesControlled += habitEntries.count { it.status == "UrgeControlled" }
        }

        // 3. 3 days clean
        if (maxStreakAcrossAll >= 3) {
            db.achievementDao().unlockAchievement("3_days_clean", now)
        }

        // 4. 1 week clean
        if (maxStreakAcrossAll >= 7) {
            db.achievementDao().unlockAchievement("1_week_streak", now)
        }

        // 5. 30 days clean
        if (maxStreakAcrossAll >= 30) {
            db.achievementDao().unlockAchievement("30_days_clean", now)
        }

        // 6. 10 Urges controlled
        if (totalUrgesControlled >= 10) {
            db.achievementDao().unlockAchievement("10_urges_defeated", now)
        }

        // 7. 30 Urges controlled
        if (totalUrgesControlled >= 30) {
            db.achievementDao().unlockAchievement("30_urges_defeated", now)
        }
    }

    private suspend fun updateCalculatedRecoveryScore() {
        val habits = db.habitDao().getAllHabits().firstOrNull() ?: emptyList()
        val entries = db.habitEntryDao().getAllEntries().firstOrNull() ?: emptyList()

        if (habits.isEmpty()) {
            db.userPreferenceDao().insertPreference(UserPreference("recovery_score", "50"))
            return
        }

        var totalMaxStreak = 0
        var totalCurrentStreak = 0
        var urgesControlled = 0
        var relapses = 0

        for (habit in habits) {
            val habitEntries = entries.filter { it.habitId == habit.id }
            val streaks = calculateStreaksForHabit(habitEntries)
            totalCurrentStreak += streaks.currentStreak
            totalMaxStreak += streaks.longestStreak
            urgesControlled += habitEntries.count { it.status == "UrgeControlled" }
            relapses += habitEntries.count { it.status == "Relapsed" }
        }

        // Algorithm score:
        // Base = 50
        // Current streaks add 4 per day
        // Urges controlled add 3 per event
        // Relapses subtract 8 per event
        // Bound to 0 - 100
        val computedScore = (50 + (totalCurrentStreak * 4) + (urgesControlled * 3) - (relapses * 8)).coerceIn(0, 100)
        db.userPreferenceDao().insertPreference(UserPreference("recovery_score", computedScore.toString()))
    }
    
    // Quick helper to insert preference
    private suspend fun UserPreferenceDao.insertPreference(key: String, value: String) {
        insertPreference(UserPreference(key, value))
    }
}
