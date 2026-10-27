package com.example.notes.domain.model

/** Бизнес-сущность: авторизованный пользователь (чистый Kotlin, без Android и Retrofit). */
data class AuthUser(val email: String, val token: String)
