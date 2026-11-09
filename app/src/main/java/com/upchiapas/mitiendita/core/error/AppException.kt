package com.upchiapas.mitiendita.core.error

/**
 * Errores de la app expresados en lenguaje del dominio.
 * Los repositorios convierten los errores técnicos (HTTP, red, JSON) en estos tipos,
 * así los ViewModels y la UI nunca dependen de Retrofit ni de OkHttp.
 */
sealed class AppException(message: String) : Exception(message) {
    class Network : AppException("No se pudo conectar con el servidor. Verifica que la API esté encendida.")
    class SessionExpired : AppException("Tu sesión expiró. Inicia sesión de nuevo.")
    class InvalidCredentials : AppException("Correo o contraseña incorrectos.")
    class EmailAlreadyRegistered : AppException("Ya existe una cuenta con ese correo.")
    class NotFound : AppException("El producto ya no existe.")
    class Unknown(detail: String? = null) :
        AppException(detail ?: "Ocurrió un error inesperado. Intenta de nuevo.")
}
