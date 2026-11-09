package com.upchiapas.mitiendita.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.upchiapas.mitiendita.auth.domain.model.User

/** Implementación de [SessionStorage] con SharedPreferences (modo privado). */
class SharedPrefsSessionStorage(context: Context) : SessionStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveSession(token: String, user: User) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
        }
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun getUser(): User? {
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        return User(
            id = prefs.getInt(KEY_USER_ID, 0),
            name = prefs.getString(KEY_USER_NAME, null).orEmpty(),
            email = email
        )
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "mi_tiendita_session"
        const val KEY_TOKEN = "access_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
    }
}
