package com.upchiapas.mitiendita.core.di

import android.content.Context
import com.upchiapas.mitiendita.BuildConfig
import com.upchiapas.mitiendita.auth.data.remote.AuthApi
import com.upchiapas.mitiendita.auth.data.repository.AuthRepositoryImpl
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.network.AuthInterceptor
import com.upchiapas.mitiendita.core.session.SessionStorage
import com.upchiapas.mitiendita.core.session.SharedPrefsSessionStorage
import com.upchiapas.mitiendita.products.data.remote.ProductApi
import com.upchiapas.mitiendita.products.data.repository.ProductRepositoryImpl
import com.upchiapas.mitiendita.products.domain.repository.ProductRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Contenedor de dependencias (inyección manual).
 * Expone solo interfaces; las implementaciones concretas quedan ocultas aquí.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val productRepository: ProductRepository
}

/**
 * Construye UNA sola instancia de cada dependencia (by lazy) y las conecta
 * por constructor. Es el único lugar de la app donde se usa "new" para estas clases.
 */
class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext: Context = context.applicationContext

    // --- Sesión ---
    private val sessionStorage: SessionStorage by lazy {
        SharedPrefsSessionStorage(appContext)
    }

    // --- Red ---
    private val json: Json by lazy {
        Json { ignoreUnknownKeys = true }
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(sessionStorage)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    private val productApi: ProductApi by lazy { retrofit.create(ProductApi::class.java) }

    // --- Repositorios (instancia única compartida) ---
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApi, sessionStorage)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(productApi)
    }
}
