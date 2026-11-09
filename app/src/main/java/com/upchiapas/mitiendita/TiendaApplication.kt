package com.upchiapas.mitiendita

import android.app.Application
import com.upchiapas.mitiendita.core.di.AppContainer
import com.upchiapas.mitiendita.core.di.DefaultAppContainer

/**
 * Clase Application: crea el contenedor de dependencias UNA sola vez
 * y lo comparte con toda la app (raíz de composición).
 */
class TiendaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
