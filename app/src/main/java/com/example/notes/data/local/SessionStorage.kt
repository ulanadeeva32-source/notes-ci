package com.example.notes.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Текущая сессия: кто вошёл. Хранится в SharedPreferences, поэтому переживает перезапуск приложения. */
@Singleton
class SessionStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    val email: String?
        get() = prefs.getString(KEY_EMAIL, null)

    val isLoggedIn: Boolean
        get() = !email.isNullOrEmpty()

    fun save(email: String, token: String) {
        prefs.edit().putString(KEY_EMAIL, email).putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_EMAIL = "email"
        const val KEY_TOKEN = "token"
    }
}
