package com.example.notes.domain.repository

import com.example.notes.domain.model.AuthUser

/** Контракт репозитория. Domain знает только интерфейс, а не Retrofit или Room. */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>

    suspend fun register(email: String, password: String): Result<AuthUser>
}
