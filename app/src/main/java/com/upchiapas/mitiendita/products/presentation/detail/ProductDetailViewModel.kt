package com.upchiapas.mitiendita.products.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upchiapas.mitiendita.auth.domain.repository.AuthRepository
import com.upchiapas.mitiendita.core.di.appContainer
import com.upchiapas.mitiendita.core.error.AppException
import com.upchiapas.mitiendita.products.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        observeProduct()
        loadProduct()
    }

    /** Si el producto se edita, el repositorio lo actualiza y el detalle se refresca solo. */
    private fun observeProduct() {
        viewModelScope.launch {
            productRepository.products.collect { products ->
                products.firstOrNull { it.id == productId }?.let { product ->
                    _uiState.update { it.copy(product = product) }
                }
            }
        }
    }

    fun loadProduct() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            productRepository.getProduct(productId)
                .onSuccess { product ->
                    _uiState.update { it.copy(isLoading = false, product = product) }
                }
                .onFailure { error ->
                    if (error is AppException.SessionExpired) {
                        expireSession()
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar el producto")
                        }
                    }
                }
        }
    }

    fun onDeleteRequest() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteConfirm() {
        val product = _uiState.value.product ?: return
        _uiState.update { it.copy(showDeleteDialog = false, isDeleting = true) }
        viewModelScope.launch {
            productRepository.deleteProduct(product.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(isDeleting = false, deletedMessage = "Se eliminó \"${product.name}\"")
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
        /** Nombre del argumento en la ruta ProductDetailRoute(productId). */
        private const val ARG_PRODUCT_ID = "productId"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val productId = requireNotNull(createSavedStateHandle().get<Int>(ARG_PRODUCT_ID)) {
                    "Falta el argumento $ARG_PRODUCT_ID"
                }
                ProductDetailViewModel(
                    productId = productId,
                    productRepository = appContainer.productRepository,
                    authRepository = appContainer.authRepository
                )
            }
        }
    }
}
