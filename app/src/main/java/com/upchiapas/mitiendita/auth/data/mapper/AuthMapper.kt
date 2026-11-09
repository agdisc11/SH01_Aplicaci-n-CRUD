package com.upchiapas.mitiendita.auth.data.mapper

import com.upchiapas.mitiendita.auth.data.remote.dto.LoginRequestDto
import com.upchiapas.mitiendita.auth.data.remote.dto.RegisterRequestDto
import com.upchiapas.mitiendita.auth.data.remote.dto.UserDto
import com.upchiapas.mitiendita.auth.domain.model.User

/** Conversiones DTO <-> dominio de autenticación. Solo las usa el repositorio. */
fun UserDto.toDomain(): User = User(
    id = id,
    name = name.orEmpty(),
    email = email
)

fun toLoginRequestDto(email: String, password: String): LoginRequestDto =
    LoginRequestDto(email = email.trim(), password = password)

fun toRegisterRequestDto(name: String, email: String, password: String): RegisterRequestDto =
    RegisterRequestDto(name = name.trim(), email = email.trim(), password = password)
