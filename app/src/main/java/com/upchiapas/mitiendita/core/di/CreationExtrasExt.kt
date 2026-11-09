package com.upchiapas.mitiendita.core.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.upchiapas.mitiendita.TiendaApplication

/**
 * Acceso al [AppContainer] desde los `initializer { }` de las ViewModelProvider.Factory.
 */
val CreationExtras.appContainer: AppContainer
    get() = (this[APPLICATION_KEY] as TiendaApplication).container
