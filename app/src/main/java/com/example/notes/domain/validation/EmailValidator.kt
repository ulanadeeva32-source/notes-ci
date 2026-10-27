package com.example.notes.domain.validation

/** Проверка email без android.util.Patterns — чтобы её можно было тестировать на обычном JUnit. */
object EmailValidator {
    private val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValid(email: String): Boolean = regex.matches(email.trim())
}
