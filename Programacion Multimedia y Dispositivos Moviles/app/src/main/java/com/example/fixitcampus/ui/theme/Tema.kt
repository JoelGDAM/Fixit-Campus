package com.example.fixitcampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta oscura de FixItCampus
private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    background = Color(0xFF121318),
    onBackground = Color.White,
    surface = Color(0xFF1E2028),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2D38),
    onSurfaceVariant = Color(0xFFB8B8C2),
    secondary = Color(0xFF9E7BFF),
    error = Color(0xFFFF6B6B)
)

// Tema global de la app
@Composable
fun FixItTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        typography = Typography(),
        content = content
    )
}
