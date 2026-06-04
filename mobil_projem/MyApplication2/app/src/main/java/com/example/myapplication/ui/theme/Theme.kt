package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.myapplication.YksRenkler

// YKS Asistan tema sistemi — DesignSystem.kt'deki renkler kullanılıyor.
// Tüm Activity'ler bu tema veya doğrudan YksRenkler kullanır (artık tutarlı).

private val YksDarkColorScheme = darkColorScheme(
    primary = YksRenkler.Vurgu,
    secondary = YksRenkler.TabAktif2,
    tertiary = YksRenkler.Yesil,
    background = YksRenkler.Arka,
    surface = YksRenkler.Yuzey,
    surfaceVariant = YksRenkler.YuzeyAlt,
    error = YksRenkler.Kirmizi,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = YksRenkler.YaziPrimary,
    onSurface = YksRenkler.YaziPrimary,
    onError = Color.White,
    outline = YksRenkler.Kenar,
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YksDarkColorScheme,
        typography = Typography,
        content = content
    )
}