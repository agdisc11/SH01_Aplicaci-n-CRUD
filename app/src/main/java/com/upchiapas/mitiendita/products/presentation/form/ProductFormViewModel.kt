package com.upchiapas.mitiendita.products.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
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
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * ViewModel del formulario reutilizable.
 * productId == null -> modo CREAR; productId != null -> modo EDITAR (se precarga).
 */
class ProductFormViewModel(
    private val productId: Int?,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductFormUiState(isEditMode = productId != null))
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        productId?.let(::loadProduct)
    }

    fun retryLoad() {
        productId?.let(::loadProduct)
    }

    private fun loadProduct(id: Int) {
        _uiState.update { it.copy(isLoadingProduct = true, loadError = null) }
        viewModelScope.launch {
            productRepository.getProduct(id)
                .onSuccess { product ->
                    _uiState.update {
                        it.copy(
                            isLoadingProduct = false,
                            name = product.name,
                            category = product.category,
                            price = BigDecimal.valueOf(product.price).stripTrailingZeros().toPlainString(),
                            stock = product.stock.toString(),
                            expirationDate = product.expirationDate
                        )
                    }
                }
                .onFailure { error ->
                    if (error is AppException.SessionExpired) {
                        expireSession()
                    } else {
                        _uiState.update {
                            it.copy(isLoadingProduct = false, loadError = error.message ?: "No se pudo cargar el producto")
                        }
                    }
                }
        }
    }

    // --- Eventos de los campos ---
    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value.take(MAX_NAME_LENGTH), nameError = null) }
    }

    fun onCategorySelected(value: String) {
        _uiState.update { it.copy(category = value, categoryError = null) }
    }

    fun onPriceChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }.take(10)
        _uiState.update { it.copy(price = filtered, priceError = null) }
    }

    fun onStockChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(stock = filtered, stockError = null) }
    }

    fun onOpenDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    /** El DatePicker entrega milisegundos UTC; aquí se convierten a LocalDate. */
    fun onDateSelected(millis: Long) {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
        _uiState.update { it.copy(expirationDate = date, expirationDateError = null, showDatePicker = false) }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // --- Guardar (crear o editar) ---
    fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val validated = validate(state)
        val price = state.price.replace(',', '.').toDoubleOrNull()
        val stock = state.stock.toIntOrNull()
        val date = state.expirationDate

        if (validated.hasErrors || price == null || stock == null || date == null) {
            _uiState.value = validated
            return
        }

        val product = Product(
            id = productId ?: Product.NEW_PRODUCT_ID,
            name = state.name.trim(),
            category = state.category,
            price = price,
            stock = stock,
            expirationDate = date
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (productId == null) {
                productRepository.createProduct(product)
            } else {
                productRepository.updateProduct(product)
            }
            result
                .onSuccess { saved ->
                    val message = if (productId == null) {
                        "Se agregó \"${saved.name}\""
                    } else {
                        "Se guardaron los cambios de \"${saved.name}\""
                    }
                    _uiState.update { it.copy(isSaving = false, savedMessage = message) }
                }
                .onFailure { error ->
                    if (error is AppException.SessionExpired) {
                        expireSession()
                    } else {
                        _uiState.update {
                            it.copy(isSaving = false, userMessage = error.message ?: "No se pudo guardar")
                        }
                    }
                }
        }
    }

    /** Reglas de validación del recurso Producto. */
    private fun validate(state: ProductFormUiState): ProductFormUiState {
        val name = state.name.trim()
        val priceText = state.price.replace(',', '.')
        val price = priceText.toDoubleOrNull()
        val stock = state.stock.toIntOrNull()
        val date = state.expirationDate

        return state.copy(
            nameError = when {
                name.isEmpty() -> "El nombre es obligatorio"
                name.length < MIN_NAME_LENGTH -> "Debe tener al menos $MIN_NAME_LENGTH caracteres"
                else -> null
            },
            categoryError = if (state.category.isBlank()) "Selecciona una categoría" else null,
            priceError = when {
                priceText.isEmpty() -> "El precio es obligatorio"
                !PRICE_REGEX.matches(priceText) || price == null -> "Usa un número con máximo 2 decimales (ej. 25.50)"
                price <= 0.0 -> "El precio debe ser mayor a 0"
                price > MAX_PRICE -> "El precio no puede ser mayor a $MAX_PRICE_LABEL"
                else -> null
            },
            stockError = when {
                state.stock.isEmpty() -> "Las existencias son obligatorias"
                stock == null -> "Ingresa un número entero"
                stock > MAX_STOCK -> "No puede haber más de $MAX_STOCK piezas"
                else -> null
            },
            expirationDateError = when {
                date == null -> "Selecciona la fecha de caducidad"
                !state.isEditMode && date.isBefore(LocalDate.now()) ->
                    "Un producto nuevo no puede estar caducado"
                else -> null
            }
        )
    }

    private fun expireSession() {
        authRepository.logout()
        _uiState.update { it.copy(isLoadingProduct = false, isSaving = false, sessionExpired = true) }
    }

    companion object {
        private const val ARG_PRODUCT_ID = "productId"
        private const val MIN_NAME_LENGTH = 3
        private const val MAX_NAME_LENGTH = 60
        private const val MAX_PRICE = 100_000.0
        private const val MAX_PRICE_LABEL = "$100,000"
        private const val MAX_STOCK = 99_999
        private val PRICE_REGEX = Regex("^\\d+(\\.\\d{1,2})?$")

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // En ProductEditRoute(productId) viene el id; en ProductCreateRoute no hay argumento.
                val productId = createSavedStateHandle().get<Int>(ARG_PRODUCT_ID)
                ProductFormViewModel(
                    productId = productId,
                    productRepository = appContainer.productRepository,
                    authRepository = appContainer.authRepository
                )
            }
        }
    }
}
