package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_entries")
data class HabitEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String, // "YYYY-MM-DD" style
    val status: String, // "Clean" (Did not do), "Relapsed" (Did do), "UrgeControlled" (Almost did but controlled), "Urge" (Felt urge/attracted), "Neutral"
    val count: Int = 0,
    val timeOfDay: String = "", // e.g. "Morning", "Afternoon", "Evening", "Night"
    val trigger: String = "",
    val moodBefore: String = "",
    val moodAfter: String = "",
    val notes: String = ""
)
