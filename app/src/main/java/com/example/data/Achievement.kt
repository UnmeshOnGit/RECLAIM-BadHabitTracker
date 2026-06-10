package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String, // unique string ID
    val title: String,
    val description: String,
    val unlockedAt: Long? = null // Null means locked, long timestamp is the unlocked time
)
