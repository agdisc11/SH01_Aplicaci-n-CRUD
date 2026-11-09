package com.upchiapas.mitiendita.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.di.appContainer
import com.upchiapas.mitiendita.core.error.AppException
import com.upchiapas.mitiendita.core.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    sessionExpired: Boolean = false
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(errorMessage = if (sessionExpired) AppException.SessionExpired().message else null)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !Validators.isValidEmail(state.email) -> "El formato del correo no es válido"
            else -> null
        }
        val passwordError = if (state.password.isBlank()) "Ingresa tu contraseña" else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.login(state.email, state.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo iniciar sesión"
                        )
                    }
                }
        }
    }

    companion object {
        /** Clave del argumento de navegación que indica que la sesión expiró. */
        private const val ARG_SESSION_EXPIRED = "sessionExpired"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val sessionExpired = createSavedStateHandle().get<Boolean>(ARG_SESSION_EXPIRED) ?: false
                LoginViewModel(
                    authRepository = appContainer.authRepository,
                    sessionExpired = sessionExpired
                )
            }
        }
    }
}
