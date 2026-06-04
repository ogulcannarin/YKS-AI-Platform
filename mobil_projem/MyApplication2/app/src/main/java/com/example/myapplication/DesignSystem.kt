package com.example.myapplication

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── YKS Asistan Design System ────────────────────────────────────────────────
// Tüm renk ve gradyan token'ları burada tanımlanır.
// MainActivity.kt'deki eski YksRenkler objesi kaldırıldı, buraya taşındı.

object YksRenkler {
    // Arka plan katmanları
    val Arka        = Color(0xFF07090F)
    val Yuzey       = Color(0xFF0F1120)
    val YuzeyAlt    = Color(0xFF151829)
    val Kenar       = Color(0xFF1C2038)

    // Vurgu renkleri
    val Vurgu       = Color(0xFF7B72FF)
    val VurguSoft   = Color(0x207B72FF)
    val VurguGlow   = Color(0x507B72FF)
    val TabAktif2   = Color(0xFFAA9FFF)

    // Durum renkleri
    val Yesil       = Color(0xFF00E5A0)
    val YesilSoft   = Color(0x2000E5A0)
    val Kirmizi     = Color(0xFFFF4D6D)
    val KirmiziSoft = Color(0x20FF4D6D)
    val Turuncu     = Color(0xFFFF8C42)
    val TuruncuSoft = Color(0x20FF8C42)
    val Altin       = Color(0xFFFFD700)

    // Yazı renkleri
    val YaziPrimary = Color(0xFFF0F0FF)
    val YaziSecond  = Color(0xFF8890AA)
    val YaziMuted   = Color(0xFF4A5066)
}

// ─── Gradyanlar ───────────────────────────────────────────────────────────────

val VurguGradyan = Brush.linearGradient(
    colors = listOf(YksRenkler.Vurgu, YksRenkler.TabAktif2)
)

val YesilGradyan = Brush.linearGradient(
    colors = listOf(Color(0xFF00C87A), YksRenkler.Yesil)
)

val BaslikGradyan = Brush.linearGradient(
    colors = listOf(YksRenkler.Vurgu, Color(0xFFA78BFA), YksRenkler.Yesil)
)

val KirmiziGradyan = Brush.linearGradient(
    colors = listOf(YksRenkler.Kirmizi, Color(0xFFFF6B35))
)

val ArkaGradyan = Brush.verticalGradient(
    colors = listOf(Color(0xFF0D0F1E), YksRenkler.Arka)
)

val ProfilBannerGradyan = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1440), YksRenkler.Yuzey)
)
