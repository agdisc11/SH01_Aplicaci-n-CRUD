package com.upchiapas.mitiendita.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Indicador de carga pequeño para usar dentro de un botón. */
@Composable
fun ButtonProgress(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier.size(22.dp),
        strokeWidth = 2.dp,
        color = LocalContentColor.current
    )
}
