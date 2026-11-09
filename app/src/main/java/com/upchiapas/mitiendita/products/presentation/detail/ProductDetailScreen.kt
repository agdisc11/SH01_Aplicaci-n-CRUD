package com.upchiapas.mitiendita.products.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mitiendita.core.ui.components.ButtonProgress
import com.upchiapas.mitiendita.core.ui.components.ConfirmDialog
import com.upchiapas.mitiendita.core.ui.components.ErrorView
import com.upchiapas.mitiendita.core.ui.components.LoadingView
import com.upchiapas.mitiendita.products.domain.model.Product
import com.upchiapas.mitiendita.products.presentation.components.ExpiredPill
import com.upchiapas.mitiendita.products.presentation.components.StatusPill
import com.upchiapas.mitiendita.products.presentation.components.StockPill
import com.upchiapas.mitiendita.products.presentation.toCurrency
import com.upchiapas.mitiendita.products.presentation.toDisplayDate
import com.upchiapas.mitiendita.ui.theme.MiTienditaTheme
import java.time.LocalDate

@Composable
fun ProductDetailScreen(
    resultMessage: String?,
    onResultMessageConsumed: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onDeleted: (String) -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(resultMessage) {
        if (resultMessage != null) {
            viewModel.showMessage(resultMessage)
            onResultMessageConsumed()
        }
    }
    LaunchedEffect(uiState.deletedMessage) {
        uiState.deletedMessage?.let(onDeleted)
    }
    LaunchedEffect(uiState.sessionExpired) {
        if (uiState.sessionExpired) onSessionExpired()
    }

    ProductDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::loadProduct,
        onEditProduct = onEditProduct,
        onDeleteRequest = viewModel::onDeleteRequest,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onUserMessageShown = viewModel::onUserMessageShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    uiState: ProductDetailUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onDeleteRequest: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
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
                title = { Text("Detalle del producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val product = uiState.product
            when {
                product != null -> ProductDetailBody(
                    product = product,
                    isBusy = uiState.isLoading || uiState.isDeleting,
                    isDeleting = uiState.isDeleting,
                    onEdit = { onEditProduct(product.id) },
                    onDelete = onDeleteRequest
                )

                uiState.isLoading -> LoadingView(message = "Cargando producto…")

                else -> ErrorView(
                    message = uiState.errorMessage ?: "No se encontró el producto",
                    onRetry = onRetry
                )
            }
        }
    }

    if (uiState.showDeleteDialog && uiState.product != null) {
        ConfirmDialog(
            title = "Eliminar producto",
            message = "¿Seguro que quieres eliminar \"${uiState.product.name}\"? Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
            isDestructive = true
        )
    }
}

@Composable
private fun ProductDetailBody(
    product: Product,
    isBusy: Boolean,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Encabezado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(product.category, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = product.price.toCurrency(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StockPill(product)
                        if (product.isExpired()) {
                            ExpiredPill()
                        } else {
                            StatusPill(
                                text = "Vigente",
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Todos los atributos
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    DetailRow(Icons.Filled.Tag, "Identificador", "#${product.id}")
                    HorizontalDivider()
                    DetailRow(Icons.Filled.Inventory2, "Nombre", product.name)
                    HorizontalDivider()
                    DetailRow(Icons.Filled.Category, "Categoría", product.category)
                    HorizontalDivider()
                    DetailRow(Icons.Filled.AttachMoney, "Precio", product.price.toCurrency())
                    HorizontalDivider()
                    DetailRow(Icons.Filled.Inventory2, "Existencias", "${product.stock} piezas")
                    HorizontalDivider()
                    DetailRow(Icons.Filled.Event, "Fecha de caducidad", product.expirationDate.toDisplayDate())
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    if (isDeleting) {
                        ButtonProgress()
                    } else {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
                Button(
                    onClick = onEdit,
                    enabled = !isDeleting,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editar")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailContentPreview() {
    MiTienditaTheme {
        ProductDetailContent(
            uiState = ProductDetailUiState(
                product = Product(1, "Leche entera 1 L", "Lácteos", 28.5, 24, LocalDate.now().plusDays(20))
            ),
            onNavigateBack = {},
            onRetry = {},
            onEditProduct = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onUserMessageShown = {}
        )
    }
}
