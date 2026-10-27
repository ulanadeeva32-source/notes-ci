package com.example.notes.domain.usecase

import com.example.notes.domain.model.AuthException
import com.example.notes.domain.model.AuthUser
import com.example.notes.domain.repository.AuthRepository
import com.example.notes.domain.validation.EmailValidator
import javax.inject.Inject

/** Сценарий «войти». Ничего не знает про Android SDK — тестируется на JUnit без эмулятора. */
class AuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> {
        val cleanEmail = email.trim()
        if (!EmailValidator.isValid(cleanEmail)) return Result.failure(AuthException.InvalidEmail())
        if (password.isBlank()) return Result.failure(AuthException.EmptyPassword())
        return repository.login(cleanEmail, password)
    }

    /** Регистрация: те же проверки + минимальная длина пароля. */
    suspend fun register(email: String, password: String): Result<AuthUser> {
        val cleanEmail = email.trim()
        if (!EmailValidator.isValid(cleanEmail)) return Result.failure(AuthException.InvalidEmail())
        if (password.length < MIN_PASSWORD_LENGTH) return Result.failure(AuthException.WeakPassword())
        return repository.register(cleanEmail, password)
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
