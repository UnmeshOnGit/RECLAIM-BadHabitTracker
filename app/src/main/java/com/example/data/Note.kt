package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val text: String,
    val mood: String, // "Happy", "Stressed", "Bored", "Angry", "Calm", "Anxious"
    val habitId: Int = 0 // optionally bound to a specific habit, 0 if general journal
)
