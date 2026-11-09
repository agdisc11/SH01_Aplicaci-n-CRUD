package com.upchiapas.mitiendita.core.network

import com.upchiapas.mitiendita.core.session.SessionStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp: agrega "Authorization: Bearer <token>" a TODAS las peticiones
 * cuando hay una sesión guardada. Así ningún endpoint tiene que recibir el token a mano.
 */
class AuthInterceptor(
    private val sessionStorage: SessionStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionStorage.getToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
    }
}
