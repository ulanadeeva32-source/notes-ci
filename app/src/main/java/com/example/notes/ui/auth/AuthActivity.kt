package com.example.notes.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.notes.data.local.SessionStorage
import com.example.notes.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Стартовый экран. Если пользователь уже входил и не выходил — сразу открывает заметки. */
@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    @Inject lateinit var session: SessionStorage

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (session.isLoggedIn) {
            openNotes()
            return
        }
        setContent {
            MaterialTheme {
                AuthScreen(viewModel = viewModel, onLoggedIn = ::openNotes)
            }
        }
    }

    private fun openNotes() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
