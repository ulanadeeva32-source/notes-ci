package com.example.notes.ui.viewmodel

import com.example.notes.data.local.entity.Note

/** Всё, что видит пользователь на экране списка заметок. */
data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val errorMessage: String? = null
)
