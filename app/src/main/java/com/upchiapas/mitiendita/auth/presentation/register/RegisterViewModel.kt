package com.upchiapas.mitiendita.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.di.appContainer
import com.upchiapas.mitiendita.core.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun register() {
        val state = _uiState.value
        if (state.isLoading) return

        val validated = validate(state)
        _uiState.value = validated
        if (validated.hasErrors()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.register(state.name, state.email, state.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isRegistered = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo crear la cuenta"
                        )
                    }
                }
        }
    }

    /** Validaciones de RF-01: campos vacíos, formato de correo, longitud y coincidencia. */
    private fun validate(state: RegisterUiState): RegisterUiState = state.copy(
        nameError = when {
            state.name.isBlank() -> "Ingresa tu nombre"
            state.name.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        },
        emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !Validators.isValidEmail(state.email) -> "El formato del correo no es válido"
            else -> null
        },
        passwordError = when {
            state.password.isBlank() -> "Ingresa una contraseña"
            state.password.length < Validators.MIN_PASSWORD_LENGTH ->
                "Debe tener al menos ${Validators.MIN_PASSWORD_LENGTH} caracteres"
            else -> null
        },
        confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Confirma tu contraseña"
            state.confirmPassword != state.password -> "Las contraseñas no coinciden"
            else -> null
        }
    )

    private fun RegisterUiState.hasErrors(): Boolean =
        nameError != null || emailError != null || passwordError != null || confirmPasswordError != null

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RegisterViewModel(authRepository = appContainer.authRepository)
            }
        }
    }
}
