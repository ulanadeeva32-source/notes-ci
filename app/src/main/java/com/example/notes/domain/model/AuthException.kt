package com.example.notes.domain.model

/** Доменные ошибки авторизации. UI превращает их в текст, а не показывает «сырой» exception. */
sealed class AuthException(message: String) : Exception(message) {
    class InvalidEmail : AuthException("Invalid email")
    class EmptyPassword : AuthException("Password is empty")
    class InvalidCredentials : AuthException("Invalid credentials")
    class EmailTaken : AuthException("Email already registered")
    class WeakPassword : AuthException("Password too short")
    class Network : AuthException("Network error")
}
