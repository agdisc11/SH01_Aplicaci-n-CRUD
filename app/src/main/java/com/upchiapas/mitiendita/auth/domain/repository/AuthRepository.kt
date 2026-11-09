package com.upchiapas.mitiendita.auth.domain.repository

import com.upchiapas.mitiendita.auth.domain.model.User

/** Contrato de autenticación. Expone solo modelos de dominio, nunca DTOs. */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    fun isLoggedIn(): Boolean
    fun getCurrentUser(): User?
    fun logout()
}
