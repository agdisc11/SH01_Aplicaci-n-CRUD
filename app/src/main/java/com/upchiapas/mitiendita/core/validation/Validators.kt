package com.upchiapas.mitiendita.core.validation

/** Reglas de validación reutilizadas por los ViewModels de autenticación. */
object Validators {
    const val MIN_PASSWORD_LENGTH = 8

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
}
