package com.upchiapas.mitiendita.products.presentation.form

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mitiendita.core.ui.components.ButtonProgress
import com.upchiapas.mitiendita.core.ui.components.ErrorView
import com.upchiapas.mitiendita.core.ui.components.LoadingView
import com.upchiapas.mitiendita.products.domain.model.ProductCategories
import com.upchiapas.mitiendita.ui.theme.MiTienditaTheme

/** Formulario reutilizable: crear (sin id) y editar (con id, precargado). */
@Composable
fun ProductFormScreen(
    onSaved: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: ProductFormViewModel = viewModel(factory = ProductFormViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let(onSaved)
    }
    LaunchedEffect(uiState.sessionExpired) {
        if (uiState.sessionExpired) onSessionExpired()
    }

    ProductFormContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetryLoad = viewModel::retryLoad,
        onNameChange = viewModel::onNameChange,
        onCategorySelected = viewModel::onCategorySelected,
        onPriceChange = viewModel::onPriceChange,
        onStockChange = viewModel::onStockChange,
        onOpenDatePicker = viewModel::onOpenDatePicker,
        onDismissDatePicker = viewModel::onDismissDatePicker,
        onDateSelected = viewModel::onDateSelected,
        onSave = viewModel::save,
        onUserMessageShown = viewModel::onUserMessageShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormContent(
    uiState: ProductFormUiState,
    onNavigateBack: () -> Unit,
    onRetryLoad: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onDismissDatePicker: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onSave: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onUserMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoadingProduct -> LoadingView(
                modifier = Modifier.padding(padding),
                message = "Cargando producto…"
            )

            uiState.loadError != null -> ErrorView(
                message = uiState.loadError,
                onRetry = onRetryLoad,
                modifier = Modifier.padding(padding)
            )

            else -> ProductFormFields(
                uiState = uiState,
                onNameChange = onNameChange,
                onCategorySelected = onCategorySelected,
                onPriceChange = onPriceChange,
                onStockChange = onStockChange,
                onOpenDatePicker = onOpenDatePicker,
                onSave = onSave,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.expirationDateMillis
        )
        DatePickerDialog(
            onDismissRequest = onDismissDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) onDateSelected(millis) else onDismissDatePicker()
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDatePicker) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductFormFields(
    uiState: ProductFormUiState,
    onNameChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Al tocar el campo de fecha (solo lectura) se abre el calendario.
    val dateInteractionSource = remember { MutableInteractionSource() }
    val currentOnOpenDatePicker by rememberUpdatedState(onOpenDatePicker)
    LaunchedEffect(dateInteractionSource) {
        dateInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) currentOnOpenDatePicker()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Todos los campos son obligatorios.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // Nombre (texto)
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Nombre del producto") },
            placeholder = { Text("Ej. Leche entera 1 L") },
            isError = uiState.nameError != null,
            supportingText = { uiState.nameError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Categoría (texto, selección)
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.labelLarge,
            color = if (uiState.categoryError != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductCategories.all.forEach { category ->
                val selected = category == uiState.category
                FilterChip(
                    selected = selected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
        if (uiState.categoryError != null) {
            Text(
                text = uiState.categoryError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        // Precio y existencias (numéricos)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.price,
                onValueChange = onPriceChange,
                label = { Text("Precio") },
                prefix = { Text("$") },
                isError = uiState.priceError != null,
                supportingText = { uiState.priceError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.stock,
                onValueChange = onStockChange,
                label = { Text("Existencias") },
                suffix = { Text("pzas") },
                isError = uiState.stockError != null,
                supportingText = { uiState.stockError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Fecha de caducidad (fecha)
        OutlinedTextField(
            value = uiState.expirationDateText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de caducidad") },
            placeholder = { Text("Toca para elegir una fecha") },
            trailingIcon = {
                IconButton(onClick = onOpenDatePicker) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Elegir fecha")
                }
            },
            isError = uiState.expirationDateError != null,
            supportingText = { uiState.expirationDateError?.let { Text(it) } },
            singleLine = true,
            interactionSource = dateInteractionSource,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSave,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (uiState.isSaving) ButtonProgress() else Text(uiState.saveButtonLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductFormContentPreview() {
    MiTienditaTheme {
        ProductFormContent(
            uiState = ProductFormUiState(
                name = "Leche entera 1 L",
                category = "Lácteos",
                price = "28.5",
                stockError = "Las existencias son obligatorias"
            ),
            onNavigateBack = {},
            onRetryLoad = {},
            onNameChange = {},
            onCategorySelected = {},
            onPriceChange = {},
            onStockChange = {},
            onOpenDatePicker = {},
            onDismissDatePicker = {},
            onDateSelected = {},
            onSave = {},
            onUserMessageShown = {}
        )
    }
}
