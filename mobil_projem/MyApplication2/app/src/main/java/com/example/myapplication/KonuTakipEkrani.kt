package com.example.myapplication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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

// Varsayılan konular
val DERS_KONULARI = mapOf(
    "Matematik" to listOf("Temel Kavramlar", "Üslü Sayılar", "Köklü Sayılar", "Problemler", "Fonksiyonlar"),
    "Türkçe" to listOf("Sözcükte Anlam", "Cümlede Anlam", "Paragraf", "Dil Bilgisi"),
    "Fizik" to listOf("Fizik Bilimine Giriş", "Madde ve Özellikleri", "Hareket ve Kuvvet", "Enerji"),
    "Kimya" to listOf("Kimya Bilimi", "Atom ve Periyodik Sistem", "Maddenin Halleri"),
    "Biyoloji" to listOf("Canlıların Ortak Özellikleri", "Hücre", "Canlıların Sınıflandırılması"),
    "Edebiyat" to listOf("Şiir Bilgisi", "İslamiyet Öncesi Edebiyat", "Halk Edebiyatı")
)

@Composable
fun KonuTakipEkrani(userEmail: String) {
    val context = LocalContext.current
    var seciliDers by remember { mutableStateOf("Matematik") }
    var yukleniyor by remember { mutableStateOf(true) }
    var konuDurumlari by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // konu_adi -> durum

    fun fetchKonular() {
        yukleniyor = true
        RetrofitClient.apiService.getKonuTakip(
            apiKey = SupabaseConfig.SUPABASE_KEY,
            authHeader = "Bearer ${SupabaseConfig.SUPABASE_KEY}", // Sadece anon key okuması için (Eğer RLS yoksa)
            emailEq = "eq.$userEmail"
        ).enqueue(object : Callback<List<KonuTakipResponse>> {
            override fun onResponse(call: Call<List<KonuTakipResponse>>, response: Response<List<KonuTakipResponse>>) {
                yukleniyor = false
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val map = mutableMapOf<String, String>()
                    body.filter { it.ders_adi == seciliDers }.forEach {
                        map[it.konu_adi] = it.durum
                    }
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

    LaunchedEffect(seciliDers) {
        fetchKonular()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        DersSecimKarti(DERSLER, seciliDers) { seciliDers = it }
        Spacer(Modifier.height(16.dp))

        if (yukleniyor) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YksRenkler.Vurgu)
            }
        } else {
            val konular = DERS_KONULARI[seciliDers] ?: emptyList()
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
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
fun KonuSatiri(konu: String, durum: String, onDurumDegistir: (String) -> Unit) {
    val (icon, color) = when (durum) {
        "bitti" -> Pair(Icons.Rounded.CheckCircle, YksRenkler.Yesil)
        "calisiliyor" -> Pair(Icons.Rounded.Schedule, YksRenkler.Vurgu)
        else -> Pair(Icons.Rounded.RadioButtonUnchecked, YksRenkler.YaziMuted)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(YksRenkler.YuzeyAlt)
            .clickable {
                val nextDurum = when (durum) {
                    "calisilacak" -> "calisiliyor"
                    "calisiliyor" -> "bitti"
                    else -> "calisilacak"
                }
                onDurumDegistir(nextDurum)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = konu, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
    }
}
