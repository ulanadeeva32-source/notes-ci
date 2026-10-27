package com.example.notes.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.R
import com.example.notes.domain.model.AuthException
import com.example.notes.domain.usecase.AuthUseCase
import com.example.notes.domain.validation.EmailValidator
import com.example.notes.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Кнопка «Войти» активна, только если email валиден, пароль введён и запрос не идёт. */
    val isLoginEnabled: StateFlow<Boolean> = _uiState
        .map { canLogin(it) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (!canLogin(current)) return

        viewModelScope.launch { // отменится сам при уничтожении ViewModel
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (current.isRegisterMode) {
                authUseCase.register(current.email, current.password)
            } else {
                authUseCase(current.email, current.password)
            }
            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toUiText()) }
                }
        }
    }

    /** Переключатель «Вход» / «Регистрация». */
    fun onToggleMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) }
    }

    /** Snackbar показан — ошибку из состояния убираем. */
    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }

    /** Переход на следующий экран выполнен. */
    fun onNavigated() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    private fun canLogin(state: AuthUiState): Boolean =
        EmailValidator.isValid(state.email) && state.password.isNotBlank() && !state.isLoading

    private fun Throwable.toUiText(): UiText = when (this) {
        is AuthException.InvalidEmail -> UiText.StringResource(R.string.auth_error_email)
        is AuthException.EmptyPassword -> UiText.StringResource(R.string.auth_error_password)
        is AuthException.InvalidCredentials -> UiText.StringResource(R.string.auth_error_credentials)
        is AuthException.EmailTaken -> UiText.StringResource(R.string.auth_error_taken)
        is AuthException.WeakPassword -> UiText.StringResource(R.string.auth_error_weak)
        is AuthException.Network -> UiText.StringResource(R.string.auth_error_network)
        else -> UiText.Dynamic(message ?: "Unknown error")
    }
}
