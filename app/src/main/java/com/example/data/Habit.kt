package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val severity: String, // "High", "Medium", "Low"
    val startDate: Long, // timestamp millis
    val recoveryGoal: String,
    val triggerTags: String, // comma-separated e.g. "stress,boredom"
    val dailyTarget: Int = 1,
    val isPaused: Boolean = false,
    val isArchived: Boolean = false,
    val customIcon: String = "smoking" // icon name identifier
)
