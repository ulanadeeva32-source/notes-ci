package com.example.notes.ui.auth

import com.example.notes.ui.util.UiText

/** Всё, что видит пользователь на экране входа. Единственный источник правды для UI. */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isLoggedIn: Boolean = false,
    val isRegisterMode: Boolean = false
)
