package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Int): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Int)
}

@Dao
interface HabitEntryDao {
    @Query("SELECT * FROM habit_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries WHERE date = :date")
    suspend fun getEntriesForDate(date: String): List<HabitEntry>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getEntryForHabitAndDate(habitId: Int, date: String): HabitEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntry): Long

    @Update
    suspend fun updateEntry(entry: HabitEntry)

    @Delete
    suspend fun deleteEntry(entry: HabitEntry)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId")
    suspend fun deleteEntriesForHabit(habitId: Int)
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferencesFlow(): Flow<List<UserPreference>>

    @Query("SELECT value FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreferenceValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: UserPreference)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE habitId = :habitId ORDER BY date DESC")
    fun getNotesForHabit(habitId: Int): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("UPDATE achievements SET unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockAchievement(id: String, timestamp: Long)
}
