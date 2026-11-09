package com.upchiapas.mitiendita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.di.appContainer
import com.upchiapas.mitiendita.navigation.LoginRoute
import com.upchiapas.mitiendita.navigation.ProductListRoute

/**
 * Decide la pantalla inicial (RF-03): si ya hay un token guardado se omite el login.
 */
class MainViewModel(authRepository: AuthRepository) : ViewModel() {

    val startDestination: Any =
        if (authRepository.isLoggedIn()) ProductListRoute else LoginRoute()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(authRepository = appContainer.authRepository)
            }
        }
    }
}
