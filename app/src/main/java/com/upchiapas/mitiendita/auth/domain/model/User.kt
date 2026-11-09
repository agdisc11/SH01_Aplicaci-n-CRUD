package com.upchiapas.mitiendita.auth.domain.model

/** Modelo de dominio del usuario autenticado. */
data class User(
    val id: Int,
    val name: String,
    val email: String
)
