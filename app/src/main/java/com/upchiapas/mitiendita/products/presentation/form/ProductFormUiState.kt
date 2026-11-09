package com.upchiapas.mitiendita.products.presentation.form

import com.upchiapas.mitiendita.products.presentation.toDisplayDate
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Estado del formulario reutilizable (crear y editar).
 * Los campos numéricos se guardan como texto tal como los escribe el usuario;
 * el ViewModel los valida y convierte al guardar.
 */
data class ProductFormUiState(
    val isEditMode: Boolean = false,
    val isLoadingProduct: Boolean = false,
    val loadError: String? = null,
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val stock: String = "",
    val expirationDate: LocalDate? = null,
    val nameError: String? = null,
    val categoryError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    val expirationDateError: String? = null,
    val showDatePicker: Boolean = false,
    val isSaving: Boolean = false,
    val userMessage: String? = null,
    val savedMessage: String? = null,
    val sessionExpired: Boolean = false
) {
    val title: String get() = if (isEditMode) "Editar producto" else "Nuevo producto"

    val saveButtonLabel: String get() = if (isEditMode) "Guardar cambios" else "Agregar producto"

    val expirationDateText: String get() = expirationDate?.toDisplayDate().orEmpty()

    /** Fecha en milisegundos UTC, formato que usa el DatePicker de Material 3. */
    val expirationDateMillis: Long?
        get() = expirationDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

    val hasErrors: Boolean
        get() = nameError != null || categoryError != null || priceError != null ||
            stockError != null || expirationDateError != null
}
