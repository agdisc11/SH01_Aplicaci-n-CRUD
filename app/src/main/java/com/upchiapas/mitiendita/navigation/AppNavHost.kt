package com.upchiapas.mitiendita.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.upchiapas.mitiendita.auth.presentation.login.LoginScreen
import com.upchiapas.mitiendita.auth.presentation.register.RegisterScreen
import com.upchiapas.mitiendita.products.presentation.detail.ProductDetailScreen
import com.upchiapas.mitiendita.products.presentation.form.ProductFormScreen
import com.upchiapas.mitiendita.products.presentation.list.ProductListScreen

/** Clave para devolver un mensaje a la pantalla anterior (resultado de navegación). */
private const val RESULT_MESSAGE_KEY = "result_message"

@Composable
fun AppNavHost(
    startDestination: Any,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { navController.navigateClearingBackStack(ProductListRoute) },
                onNavigateToRegister = { navController.navigate(RegisterRoute) }
            )
        }

        composable<RegisterRoute> { entry ->
            RegisterScreen(
                onRegisterSuccess = { navController.navigateClearingBackStack(ProductListRoute) },
                onNavigateBack = { navController.popBackStackIfResumed(entry) }
            )
        }

        composable<ProductListRoute> { entry ->
            val resultMessage by entry.savedStateHandle
                .getStateFlow<String?>(RESULT_MESSAGE_KEY, null)
                .collectAsStateWithLifecycle()

            ProductListScreen(
                resultMessage = resultMessage,
                onResultMessageConsumed = { entry.savedStateHandle[RESULT_MESSAGE_KEY] = null },
                onProductClick = { id -> navController.navigateIfResumed(entry, ProductDetailRoute(id)) },
                onAddProduct = { navController.navigateIfResumed(entry, ProductCreateRoute) },
                onEditProduct = { id -> navController.navigateIfResumed(entry, ProductEditRoute(id)) },
                // Cerrar sesión: se limpia toda la pila, "Atrás" ya no regresa a Home.
                onLoggedOut = { navController.navigateClearingBackStack(LoginRoute()) },
                onSessionExpired = { navController.navigateClearingBackStack(LoginRoute(sessionExpired = true)) }
            )
        }

        composable<ProductDetailRoute> { entry ->
            val resultMessage by entry.savedStateHandle
                .getStateFlow<String?>(RESULT_MESSAGE_KEY, null)
                .collectAsStateWithLifecycle()

            ProductDetailScreen(
                resultMessage = resultMessage,
                onResultMessageConsumed = { entry.savedStateHandle[RESULT_MESSAGE_KEY] = null },
                onNavigateBack = { navController.popBackStackIfResumed(entry) },
                onEditProduct = { id -> navController.navigateIfResumed(entry, ProductEditRoute(id)) },
                onDeleted = { message -> navController.popBackStackWithMessage(message) },
                onSessionExpired = { navController.navigateClearingBackStack(LoginRoute(sessionExpired = true)) }
            )
        }

        composable<ProductCreateRoute> { entry ->
            ProductFormScreen(
                onSaved = { message -> navController.popBackStackWithMessage(message) },
                onNavigateBack = { navController.popBackStackIfResumed(entry) },
                onSessionExpired = { navController.navigateClearingBackStack(LoginRoute(sessionExpired = true)) }
            )
        }

        composable<ProductEditRoute> { entry ->
            ProductFormScreen(
                onSaved = { message -> navController.popBackStackWithMessage(message) },
                onNavigateBack = { navController.popBackStackIfResumed(entry) },
                onSessionExpired = { navController.navigateClearingBackStack(LoginRoute(sessionExpired = true)) }
            )
        }
    }
}

/** Navega a [route] y borra toda la pila anterior (login, registro, logout). */
private fun NavHostController.navigateClearingBackStack(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Evita navegaciones dobles si el usuario toca dos veces muy rápido. */
private fun NavHostController.navigateIfResumed(entry: NavBackStackEntry, route: Any) {
    if (entry.isResumed()) navigate(route)
}

private fun NavHostController.popBackStackIfResumed(entry: NavBackStackEntry) {
    if (entry.isResumed()) popBackStack()
}

/** Regresa a la pantalla anterior dejándole un mensaje para mostrar en su Snackbar. */
private fun NavHostController.popBackStackWithMessage(message: String) {
    previousBackStackEntry?.savedStateHandle?.set(RESULT_MESSAGE_KEY, message)
    popBackStack()
}

private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED
