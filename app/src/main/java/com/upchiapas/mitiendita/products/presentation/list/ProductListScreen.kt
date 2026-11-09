package com.upchiapas.mitiendita.products.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mitiendita.core.ui.components.ConfirmDialog
import com.upchiapas.mitiendita.core.ui.components.EmptyView
import com.upchiapas.mitiendita.core.ui.components.ErrorBanner
import com.upchiapas.mitiendita.core.ui.components.ErrorView
import com.upchiapas.mitiendita.core.ui.components.LoadingView
import com.upchiapas.mitiendita.products.domain.model.Product
import com.upchiapas.mitiendita.products.presentation.components.ProductCard
import com.upchiapas.mitiendita.ui.theme.MiTienditaTheme
import java.time.LocalDate

/**
 * Home = CRUD. Pantalla stateful: observa el ViewModel y reenvía eventos.
 *
 * @param resultMessage mensaje que dejó otra pantalla al regresar (ej. "Producto agregado").
 */
@Composable
fun ProductListScreen(
    resultMessage: String?,
    onResultMessageConsumed: () -> Unit,
    onProductClick: (Int) -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onLoggedOut: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: ProductListViewModel = viewModel(factory = ProductListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(resultMessage) {
        if (resultMessage != null) {
            viewModel.showMessage(resultMessage)
            onResultMessageConsumed()
        }
    }
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLoggedOut()
    }
    LaunchedEffect(uiState.sessionExpired) {
        if (uiState.sessionExpired) onSessionExpired()
    }

    ProductListContent(
        uiState = uiState,
        onRefresh = viewModel::loadProducts,
        onProductClick = onProductClick,
        onAddProduct = onAddProduct,
        onEditProduct = onEditProduct,
        onDeleteRequest = viewModel::onDeleteRequest,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onLogoutRequest = viewModel::onLogoutRequest,
        onLogoutConfirm = viewModel::onLogoutConfirm,
        onLogoutDismiss = viewModel::onLogoutDismiss,
        onUserMessageShown = viewModel::onUserMessageShown
    )
}

/** Stateless: solo dibuja según el UiState. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListContent(
    uiState: ProductListUiState,
    onRefresh: () -> Unit,
    onProductClick: (Int) -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onDeleteRequest: (Product) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onLogoutRequest: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
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
                title = {
                    Column {
                        Text("Inventario")
                        if (uiState.userName.isNotBlank()) {
                            Text(
                                text = "Hola, ${uiState.userName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = onLogoutRequest) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.showFullScreenLoading -> LoadingView(message = "Cargando productos…")

                uiState.showFullScreenError -> ErrorView(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = onRefresh
                )

                uiState.showEmpty -> EmptyView(
                    title = "Aún no hay productos",
                    message = "Agrega el primer producto de tu tienda para empezar.",
                    actionLabel = "Agregar producto",
                    onAction = onAddProduct
                )

                else -> ProductList(
                    uiState = uiState,
                    onRetry = onRefresh,
                    onProductClick = onProductClick,
                    onEditProduct = onEditProduct,
                    onDeleteRequest = onDeleteRequest
                )
            }
        }
    }

    uiState.productToDelete?.let { product ->
        ConfirmDialog(
            title = "Eliminar producto",
            message = "¿Seguro que quieres eliminar \"${product.name}\"? Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
            isDestructive = true
        )
    }

    if (uiState.showLogoutDialog) {
        ConfirmDialog(
            title = "Cerrar sesión",
            message = "¿Quieres salir de tu cuenta?",
            confirmLabel = "Cerrar sesión",
            onConfirm = onLogoutConfirm,
            onDismiss = onLogoutDismiss
        )
    }
}

@Composable
private fun ProductList(
    uiState: ProductListUiState,
    onRetry: () -> Unit,
    onProductClick: (Int) -> Unit,
    onEditProduct: (Int) -> Unit,
    onDeleteRequest: (Product) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                ErrorBanner(message = uiState.errorMessage)
                TextButton(onClick = onRetry) { Text("Reintentar") }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = uiState.summary,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(uiState.products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    onEdit = { onEditProduct(product.id) },
                    onDelete = { onDeleteRequest(product) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductListContentPreview() {
    MiTienditaTheme {
        ProductListContent(
            uiState = ProductListUiState(
                userName = "Ana",
                hasLoaded = true,
                products = listOf(
                    Product(1, "Leche entera 1 L", "Lácteos", 28.5, 24, LocalDate.now().plusDays(20)),
                    Product(2, "Pan de caja", "Panadería", 54.9, 3, LocalDate.now().minusDays(1))
                )
            ),
            onRefresh = {},
            onProductClick = {},
            onAddProduct = {},
            onEditProduct = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onLogoutRequest = {},
            onLogoutConfirm = {},
            onLogoutDismiss = {},
            onUserMessageShown = {}
        )
    }
}
