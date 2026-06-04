package com.example.myapplication

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.network.KonuTakipRequest
import com.example.myapplication.network.KonuTakipResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ─── Konu Listesi ─────────────────────────────────────────────────────────────

val DERS_KONULARI = mapOf(
    "Matematik" to listOf(
        "Temel Kavramlar", "Üslü Sayılar", "Köklü Sayılar", "Problemler",
        "Fonksiyonlar", "Trigonometri", "Logaritma", "Diziler"
    ),
    "Türkçe" to listOf(
        "Sözcükte Anlam", "Cümlede Anlam", "Paragraf", "Dil Bilgisi",
        "Yazım Kuralları", "Noktalama", "Edebi Türler"
    ),
    "Fizik" to listOf(
        "Fizik Bilimine Giriş", "Madde ve Özellikleri", "Hareket ve Kuvvet",
        "Enerji", "Basınç", "Isı ve Sıcaklık", "Elektrik"
    ),
    "Kimya" to listOf(
        "Kimya Bilimi", "Atom ve Periyodik Sistem", "Maddenin Halleri",
        "Kimyasal Bağlar", "Asitler ve Bazlar", "Çözeltiler"
    ),
    "Biyoloji" to listOf(
        "Canlıların Ortak Özellikleri", "Hücre", "Canlıların Sınıflandırılması",
        "Ekosistem", "Genetik", "İnsan Fizyolojisi"
    ),
    "Edebiyat" to listOf(
        "Şiir Bilgisi", "İslamiyet Öncesi Edebiyat", "Halk Edebiyatı",
        "Divan Edebiyatı", "Tanzimat Edebiyatı", "Modern Edebiyat"
    )
)

// ─── KonuTakip Ekranı ─────────────────────────────────────────────────────────

@Composable
fun KonuTakipEkrani(userEmail: String) {
    val context = LocalContext.current
    var seciliDers by remember { mutableStateOf("Matematik") }
    var yukleniyor by remember { mutableStateOf(true) }
    var konuDurumlari by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    fun fetchKonular() {
        yukleniyor = true
        RetrofitClient.apiService.getKonuTakip(
            apiKey = SupabaseConfig.SUPABASE_KEY,
            authHeader = "Bearer ${SupabaseConfig.SUPABASE_KEY}",
            emailEq = "eq.$userEmail"
        ).enqueue(object : Callback<List<KonuTakipResponse>> {
            override fun onResponse(call: Call<List<KonuTakipResponse>>, response: Response<List<KonuTakipResponse>>) {
                yukleniyor = false
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val map = mutableMapOf<String, String>()
                    body.filter { it.ders_adi == seciliDers }.forEach { map[it.konu_adi] = it.durum }
                    konuDurumlari = map
                }
            }
            override fun onFailure(call: Call<List<KonuTakipResponse>>, t: Throwable) {
                yukleniyor = false
                Toast.makeText(context, "Bağlantı hatası", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateKonu(konuAdi: String, durum: String) {
        val request = KonuTakipRequest(userEmail, seciliDers, konuAdi, durum)
        RetrofitClient.apiService.upsertKonuTakip(
            apiKey = SupabaseConfig.SUPABASE_KEY,
            authHeader = "Bearer ${SupabaseConfig.SUPABASE_KEY}",
            request = request
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val newMap = konuDurumlari.toMutableMap()
                    newMap[konuAdi] = durum
                    konuDurumlari = newMap
                } else {
                    Toast.makeText(context, "Güncellenemedi!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Hata oluştu!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    LaunchedEffect(seciliDers) { fetchKonular() }

    val konular = DERS_KONULARI[seciliDers] ?: emptyList()
    val tamamlanan = konular.count { konuDurumlari[it] == "bitti" }
    val devamEden = konular.count { konuDurumlari[it] == "calisiliyor" }
    val ilerleme = if (konular.isNotEmpty()) tamamlanan.toFloat() / konular.size.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = ilerleme,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Ders Seçimi
        DersSecimKarti(DERSLER, seciliDers) { seciliDers = it }

        Spacer(Modifier.height(16.dp))

        // İlerleme Kartı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(YksRenkler.YuzeyAlt)
                .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "İlerleme",
                        color = YksRenkler.YaziSecond,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusBadge("$tamamlanan Bitti", YksRenkler.Yesil)
                        StatusBadge("$devamEden Devam", YksRenkler.Vurgu)
                    }
                }
                Spacer(Modifier.height(10.dp))
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(YksRenkler.Kenar)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(YesilGradyan)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "$tamamlanan / ${konular.size} konu tamamlandı",
                    color = YksRenkler.YaziMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Konu Listesi
        if (yukleniyor) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YksRenkler.Vurgu, strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(konular) { konu ->
                    val durum = konuDurumlari[konu] ?: "calisilacak"
                    KonuSatiri(konu = konu, durum = durum) { yeniDurum ->
                        updateKonu(konu, yeniDurum)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(metin: String, renk: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(renk.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(metin, color = renk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KonuSatiri(konu: String, durum: String, onDurumDegistir: (String) -> Unit) {
    val (icon, accentColor, bgAlpha, statusLabel) = when (durum) {
        "bitti"        -> listOf(Icons.Rounded.CheckCircle, YksRenkler.Yesil, 0.08f, "Bitti")
        "calisiliyor"  -> listOf(Icons.Rounded.Schedule, YksRenkler.Vurgu, 0.06f, "Çalışıyor")
        else           -> listOf(Icons.Rounded.RadioButtonUnchecked, YksRenkler.YaziMuted, 0f, "Bekliyor")
    }

    @Suppress("UNCHECKED_CAST")
    val iconVec = icon as androidx.compose.ui.graphics.vector.ImageVector
    val color = accentColor as Color
    val alpha = bgAlpha as Float
    val label = statusLabel as String

    val bgColor by animateColorAsState(
        targetValue = color.copy(alpha = alpha),
        animationSpec = tween(200),
        label = "row_bg_$konu"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (durum == "calisilacak") YksRenkler.Kenar else color.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .clickable {
                val nextDurum = when (durum) {
                    "calisilacak" -> "calisiliyor"
                    "calisiliyor" -> "bitti"
                    else -> "calisilacak"
                }
                onDurumDegistir(nextDurum)
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Sol renkli çizgi
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Text(text = konu, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        // Durum badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(targetState = label, label = "status_$konu") { lbl ->
                Text(
                    text = lbl,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(iconVec, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}
