package com.upchiapas.mitiendita.auth.data.repository

import com.upchiapas.mitiendita.auth.data.mapper.toDomain
import com.upchiapas.mitiendita.auth.data.mapper.toLoginRequestDto
import com.upchiapas.mitiendita.auth.data.mapper.toRegisterRequestDto
import com.upchiapas.mitiendita.auth.data.remote.AuthApi
import com.upchiapas.mitiendita.auth.data.remote.dto.AuthResponseDto
import com.upchiapas.mitiendita.auth.domain.model.User
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.error.AppException
import com.upchiapas.mitiendita.core.network.safeApiCall
import com.upchiapas.mitiendita.core.session.SessionStorage

/**
 * Implementación del repositorio de autenticación.
 * Recibe sus dependencias por constructor y tipadas como interfaces.
 */
class AuthRepositoryImpl(
    private val api: AuthApi,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        safeApiCall(
            onHttpError = { code, _ ->
                if (code == 400) AppException.InvalidCredentials() else null
            }
        ) {
            val response = api.login(toLoginRequestDto(email, password))
            saveSession(response)
        }

    override suspend fun register(name: String, email: String, password: String): Result<User> =
        safeApiCall(
            onHttpError = { code, body ->
                when {
                    code == 400 && body.contains("already exists", ignoreCase = true) ->
                        AppException.EmailAlreadyRegistered()
                    code == 400 ->
                        AppException.Unknown("No se pudo crear la cuenta: ${body.trim('"')}")
                    else -> null
                }
            }
        ) {
            val response = api.register(toRegisterRequestDto(name, email, password))
            saveSession(response)
        }

    override fun isLoggedIn(): Boolean = !sessionStorage.getToken().isNullOrBlank()

    override fun getCurrentUser(): User? = sessionStorage.getUser()

    override fun logout() {
        sessionStorage.clear()
    }

    private fun saveSession(response: AuthResponseDto): User {
        val user = response.user.toDomain()
        sessionStorage.saveSession(token = response.accessToken, user = user)
        return user
    }
}
