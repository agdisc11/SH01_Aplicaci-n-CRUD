package com.upchiapas.mitiendita.products.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.di.appContainer
import com.upchiapas.mitiendita.core.error.AppException
import com.upchiapas.mitiendita.products.domain.model.Product
import com.upchiapas.mitiendita.products.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProductListUiState(userName = authRepository.getCurrentUser()?.name.orEmpty())
    )
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        loadProducts()
    }

    /** La lista se actualiza sola cuando el repositorio cambia (crear / editar / eliminar). */
    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.products.collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }

    fun loadProducts() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            productRepository.refreshProducts()
                .onSuccess { products ->
                    _uiState.update { it.copy(isLoading = false, hasLoaded = true, products = products) }
                }
                .onFailure { error ->
                    if (error is AppException.SessionExpired) {
                        expireSession()
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar la lista")
                        }
                    }
                }
        }
    }

    // --- Eliminar (con confirmación) ---
    fun onDeleteRequest(product: Product) {
        _uiState.update { it.copy(productToDelete = product) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(productToDelete = null) }
    }

    fun onDeleteConfirm() {
        val product = _uiState.value.productToDelete ?: return
        _uiState.update { it.copy(productToDelete = null, isDeleting = true) }
        viewModelScope.launch {
            productRepository.deleteProduct(product.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(isDeleting = false, userMessage = "Se eliminó \"${product.name}\"")
                    }
                }
                .onFailure { error ->
                    if (error is AppException.SessionExpired) {
                        expireSession()
                    } else {
                        _uiState.update {
                            it.copy(isDeleting = false, userMessage = error.message ?: "No se pudo eliminar")
                        }
                    }
                }
        }
    }

    // --- Cerrar sesión ---
    fun onLogoutRequest() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun onLogoutDismiss() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun onLogoutConfirm() {
        authRepository.logout()
        _uiState.update { it.copy(showLogoutDialog = false, isLoggedOut = true) }
    }

    // --- Mensajes (Snackbar) ---
    fun showMessage(message: String) {
        _uiState.update { it.copy(userMessage = message) }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun expireSession() {
        authRepository.logout()
        _uiState.update { it.copy(isLoading = false, isDeleting = false, sessionExpired = true) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProductListViewModel(
                    productRepository = appContainer.productRepository,
                    authRepository = appContainer.authRepository
                )
            }
        }
    }
}
