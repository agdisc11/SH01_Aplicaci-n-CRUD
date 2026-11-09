package com.upchiapas.mitiendita.auth.data.remote

import com.upchiapas.mitiendita.auth.data.remote.dto.AuthResponseDto
import com.upchiapas.mitiendita.auth.data.remote.dto.LoginRequestDto
import com.upchiapas.mitiendita.auth.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Endpoints de autenticación (json-server-auth). */
interface AuthApi {

    @POST("register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto
}
