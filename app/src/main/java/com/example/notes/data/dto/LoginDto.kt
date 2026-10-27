package com.example.notes.data.dto

/** Сетевые модели (JSON). От доменных моделей отделены намеренно. */
data class LoginRequestDto(val email: String, val password: String)

data class LoginResponseDto(val email: String, val token: String)
