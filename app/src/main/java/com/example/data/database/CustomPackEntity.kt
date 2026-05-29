package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_packs")
data class CustomPackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val creatorName: String,
    val description: String,
    val words: String, // Comma-separated list of words
    val iconName: String = "stars",
    val isBuiltIn: Boolean = false
)
