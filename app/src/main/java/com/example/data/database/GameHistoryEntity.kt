package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val packName: String,
    val playerNamesList: String, // Comma-separated names
    val imposterName: String,
    val winnerRole: String, // "IMPOSTER" or "CIVILIANS"
    val durationSeconds: Int,
    val secretWord: String
)
