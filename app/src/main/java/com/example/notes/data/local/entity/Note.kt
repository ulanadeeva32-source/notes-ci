package com.example.notes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    /** Чья заметка (email пользователя). Заполняет репозиторий при сохранении. */
    val ownerEmail: String = ""
)
