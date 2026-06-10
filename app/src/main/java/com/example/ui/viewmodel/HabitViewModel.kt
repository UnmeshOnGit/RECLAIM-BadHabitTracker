package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.getGeminiCoachInsight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = BadHabitRepository(database)

    // Data streams
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entries: StateFlow<List<HabitEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<List<UserPreference>> = repository.allPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    private val _selectedHabitId = MutableStateFlow<Int?>(null)
    val selectedHabitId = _selectedHabitId.asStateFlow()

    private val _aiInsight = MutableStateFlow<String>("Tap 'Coach' on Dashboard to load AI psychological assessment...")
    val aiInsight = _aiInsight.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    // Urge delay countdown challenge timer states
    private val _urgeTimerSecondsLeft = MutableStateFlow(0)
    val urgeTimerSecondsLeft = _urgeTimerSecondsLeft.asStateFlow()
    
    private val _isUrgeTimerRunning = MutableStateFlow(false)
    val isUrgeTimerRunning = _isUrgeTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedDatabase()
            repository.checkAndUnlockAchievements()
        }
    }

    fun selectHabit(id: Int?) {
        _selectedHabitId.value = id
    }

    // Habits actions
    fun addHabit(
        name: String,
        category: String,
        severity: String,
        recoveryGoal: String,
        triggerTags: String,
        dailyTarget: Int,
        iconName: String
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                category = category,
                severity = severity,
                startDate = System.currentTimeMillis(),
                recoveryGoal = recoveryGoal,
                triggerTags = triggerTags,
                dailyTarget = dailyTarget,
                customIcon = iconName
            )
            repository.insertHabit(habit)
        }
    }

    fun editHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun archiveHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isArchived = true))
        }
    }

    fun togglePauseHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isPaused = !habit.isPaused))
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            repository.deleteHabitById(habitId)
        }
    }

    // Logging daily tracker actions
    fun logEntry(
        habitId: Int,
        status: String, // "Clean", "Relapsed", "UrgeControlled", "FeltUrge", "Neutral"
        count: Int,
        timeOfDay: String,
        trigger: String,
        moodBefore: String,
        moodAfter: String,
        notes: String
    ) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entry = HabitEntry(
                habitId = habitId,
                date = todayDate,
                status = status,
                count = count,
                timeOfDay = timeOfDay,
                trigger = trigger,
                moodBefore = moodBefore,
                moodAfter = moodAfter,
                notes = notes
            )
            repository.logHabitEntry(entry)
        }
    }

    // Helper to log entry on explicit past dates
    fun logEntryForDate(
        habitId: Int,
        dateStr: String, // "YYYY-MM-DD"
        status: String,
        count: Int,
        timeOfDay: String,
        trigger: String,
        moodBefore: String,
        moodAfter: String,
        notes: String
    ) {
        viewModelScope.launch {
            val entry = HabitEntry(
                habitId = habitId,
                date = dateStr,
                status = status,
                count = count,
                timeOfDay = timeOfDay,
                trigger = trigger,
                moodBefore = moodBefore,
                moodAfter = moodAfter,
                notes = notes
            )
            repository.logHabitEntry(entry)
        }
    }

    // Add Reflection note - Calendar linked
    fun addJournalNote(text: String, mood: String, habitId: Int = 0) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val note = Note(
                date = todayDate,
                text = text,
                mood = mood,
                habitId = habitId
            )
            repository.insertNote(note)
        }
    }

    fun deleteJournalNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Save Preference info (e.g. username, quote)
    fun updateProfile(username: String, quote: String, goals: String, avatar: String) {
        viewModelScope.launch {
            repository.savePreference("username", username)
            repository.savePreference("motivation_quote", quote)
            repository.savePreference("recovery_goals", goals)
            repository.savePreference("avatar", avatar)
        }
    }

    fun setThemePreference(theme: String) {
        viewModelScope.launch {
            repository.savePreference("theme_preference", theme)
        }
    }

    // Urge recovery flow countdown control
    fun startUrgeCountdown(seconds: Int) {
        timerJob?.cancel()
        _urgeTimerSecondsLeft.value = seconds
        _isUrgeTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_urgeTimerSecondsLeft.value > 0) {
                delay(1000)
                _urgeTimerSecondsLeft.value -= 1
            }
            _isUrgeTimerRunning.value = false
        }
    }

    fun stopUrgeCountdown() {
        timerJob?.cancel()
        _urgeTimerSecondsLeft.value = 0
        _isUrgeTimerRunning.value = false
    }

    // Trigger Gemini Coach Insight Engine
    fun generateCoachReport() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiInsight.value = "AI Sovereign Coach is analyzing your behavioral patterns & trigger history..."
            
            val habitList = habits.value
            val entryList = entries.value
            
            if (habitList.isEmpty()) {
                _aiInsight.value = "Sober Coach: Welcome! Add at least one bad habit to begin receiving behavioral therapy insights."
                _isAiLoading.value = false
                return@launch
            }

            // Construct structured psychological context prompt representing user data
            val promptBuilder = StringBuilder()
            promptBuilder.append("You are 'Sober Coach', a clinical advisor specialized in cognitive behavioral therapy (CBT) and addiction recovery. Avoid flowery language, be extremely concise, brief, and highly impactful.\n\n")
            promptBuilder.append("Here is the user's current bad habits and logged triggers database:\n")
            
            for (habit in habitList) {
                val habitEntries = entryList.filter { it.habitId == habit.id }
                val streaks = repository.calculateStreaksForHabit(habitEntries)
                val relapses = habitEntries.count { it.status == "Relapsed" }
                val urgesResisted = habitEntries.count { it.status == "UrgeControlled" }
                
                promptBuilder.append("- Habit: ${habit.name} (${habit.category}), Severity: ${habit.severity}.\n")
                promptBuilder.append("  Current Streak: ${streaks.currentStreak} days, Longest Streak: ${streaks.longestStreak} days.\n")
                promptBuilder.append("  Relapses logged: $relapses, Urges controlled successfully: $urgesResisted.\n")
                
                val commonTriggers = habitEntries.map { it.trigger }
                    .filter { it.isNotEmpty() }
                    .groupBy { it }
                    .maxByOrNull { it.value.size }?.key ?: "Unknown"
                
                promptBuilder.append("  Primary trigger logged: $commonTriggers.\n")
            }
            
            val totalScore = preferences.value.firstOrNull { it.key == "recovery_score" }?.value ?: "50"
            promptBuilder.append("\nUser general recovery discipline score: $totalScore/100.\n")
            promptBuilder.append("\nProvide 3 highly actionable CBT-focused actions (max 2 sentences per action) covering trigger substitution, environmental triggers control, and emotional self-regulation to help the user sustain their streak and stay in control.")

            val result = getGeminiCoachInsight(promptBuilder.toString())
            _aiInsight.value = result
            _isAiLoading.value = false
        }
    }

    fun seedSampleAnalyticsData() {
        viewModelScope.launch {
            var currentHabits = habits.value
            if (currentHabits.isEmpty()) {
                val demoHabit1 = Habit(
                    name = "Smoking",
                    category = "Smoking",
                    severity = "High",
                    startDate = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
                    recoveryGoal = "Completely sober lifestyle.",
                    triggerTags = "stress,fatigue,social",
                    dailyTarget = 0,
                    customIcon = "smoking"
                )
                val demoHabit2 = Habit(
                    name = "Doomscrolling",
                    category = "ScreenTime",
                    severity = "Medium",
                    startDate = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
                    recoveryGoal = "Limit to 30 mins a day.",
                    triggerTags = "boredom,night,loneliness",
                    dailyTarget = 30,
                    customIcon = "phone"
                )
                repository.insertHabit(demoHabit1)
                repository.insertHabit(demoHabit2)
                delay(300)
                currentHabits = repository.allHabits.firstOrNull() ?: emptyList()
            }

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            val times = listOf("Morning", "Afternoon", "Evening", "Night")
            
            for (habit in currentHabits) {
                val triggers = when (habit.category) {
                    "Smoking" -> listOf("Stress", "Social circle", "After meals", "Fatigue")
                    "ScreenTime" -> listOf("Boredom", "Anxiety", "Loneliness", "Procrastination")
                    else -> listOf("Boredom", "Stress", "Fatigue", "Anxiety")
                }
                
                for (i in 0..30) {
                    cal.time = java.util.Date()
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
                    val dateString = sdf.format(cal.time)

                    val rand = (1..100).random()
                    val status = when {
                        rand <= 75 -> "Clean"
                        rand <= 90 -> "UrgeControlled"
                        else -> "Relapsed"
                    }

                    val count = if (status == "Relapsed") (1..3).random() else 0
                    val timeOfDay = if (status != "Clean") {
                        if (status == "Relapsed") {
                            val selectedTimesOfDays = (1..count).map {
                                if ((1..10).random() > 3) {
                                    listOf("Evening", "Night").random()
                                } else {
                                    times.random()
                                }
                            }.distinct()
                            selectedTimesOfDays.joinToString(", ")
                        } else {
                            times.random()
                        }
                    } else ""

                    val trigger = if (status != "Clean") {
                        if (status == "Relapsed" && (1..10).random() > 4) {
                            triggers.take(2).random()
                        } else {
                            triggers.random()
                        }
                    } else ""

                    val moodB = if (status != "Clean") listOf("Stressed", "Anxious", "Bored").random() else "Calm"
                    val moodA = if (status == "Relapsed") "Guilty" else if (status == "UrgeControlled") "Empowered" else "Calm"

                    val entry = HabitEntry(
                        habitId = habit.id,
                        date = dateString,
                        status = status,
                        count = count,
                        timeOfDay = timeOfDay,
                        trigger = trigger,
                        moodBefore = moodB,
                        moodAfter = moodA,
                        notes = "Auto-seeded recovery tracker demo entry."
                    )
                    repository.logHabitEntry(entry)
                }
            }
        }
    }
}
