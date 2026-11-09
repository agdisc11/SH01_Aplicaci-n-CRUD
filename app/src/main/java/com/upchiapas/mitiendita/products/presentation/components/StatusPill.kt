package com.upchiapas.mitiendita.products.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.upchiapas.mitiendita.products.domain.model.Product

/** Etiqueta pequeña de estado (stock, caducidad). */
@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StockPill(product: Product, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    when {
        product.isOutOfStock ->
            StatusPill("Agotado", colors.errorContainer, colors.onErrorContainer, modifier)
        product.isLowStock ->
            StatusPill("Stock bajo: ${product.stock}", colors.secondaryContainer, colors.onSecondaryContainer, modifier)
        else ->
            StatusPill("${product.stock} en existencia", colors.primaryContainer, colors.onPrimaryContainer, modifier)
    }
}

@Composable
fun ExpiredPill(modifier: Modifier = Modifier) {
    StatusPill(
        text = "Caducado",
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        modifier = modifier
    )
}
