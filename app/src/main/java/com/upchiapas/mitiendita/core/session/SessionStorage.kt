package com.upchiapas.mitiendita.core.session

import com.upchiapas.mitiendita.auth.domain.model.User

/**
 * Abstracción del almacenamiento de la sesión.
 * Nadie depende de SharedPreferences directamente, solo de esta interfaz.
 */
interface SessionStorage {
    fun saveSession(token: String, user: User)
    fun getToken(): String?
    fun getUser(): User?
    fun clear()
}
