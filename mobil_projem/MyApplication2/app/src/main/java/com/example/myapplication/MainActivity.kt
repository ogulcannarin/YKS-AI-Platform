package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.myapplication.network.*
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ─── Renk Paleti ──────────────────────────────────────────────────────────────
object YksRenkler {
    val Arka        = Color(0xFF0A0B0F)
    val Yuzey       = Color(0xFF12141A)
    val YuzeyAlt    = Color(0xFF1A1D26)
    val Kenar       = Color(0xFF22263A)
    val Vurgu       = Color(0xFF6C63FF)
    val VurguSoft   = Color(0x1F6C63FF)
    val Yesil       = Color(0xFF00E5A0)
    val Kirmizi     = Color(0xFFFF4D6D)
    val YaziPrimary = Color(0xFFF0F0FF)
    val YaziSecond  = Color(0xFF8890AA)
    val YaziMuted   = Color(0xFF4A5066)
    val TabAktif2   = Color(0xFF8B83FF)
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

// ─── Modeller ─────────────────────────────────────────────────────────────────
data class YksSekmesi(val id: String, val emoji: String, val etiket: String)

val SEKMELER = listOf(
    YksSekmesi("TYT",   "📝", "TYT"),
    YksSekmesi("SAY",   "🔢", "Sayısal"),
    YksSekmesi("EA",    "📐", "EA"),
    YksSekmesi("SOZ",   "📚", "Sözel"),
    YksSekmesi("CALIS", "⏱", "Çalışma"),
    YksSekmesi("AI",    "🤖", "AI Koç"),
    YksSekmesi("SORU",  "📸", "Soru Çöz")
)

val DERSLER = listOf("Matematik", "Türkçe", "Fizik", "Kimya", "Biyoloji", "Edebiyat")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = YksRenkler.Arka)) {
                YksAsistanUI()
            }
        }
    }
}

@Composable
fun YksAsistanUI() {
    val context = LocalContext.current
    var aktifSekme by remember { mutableStateOf("TYT") }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(YksApiService::class.java) }

    // Net State'leri
    var obp by remember { mutableStateOf("85.0") }
    var tTur by remember { mutableStateOf("30") }; var tMat by remember { mutableStateOf("25") }
    var tSos by remember { mutableStateOf("15") }; var tFen by remember { mutableStateOf("10") }
    var aMat by remember { mutableStateOf("20") }; var aFiz by remember { mutableStateOf("10") }
    var aKim by remember { mutableStateOf("10") }; var aBio by remember { mutableStateOf("10") }
    var aEdb by remember { mutableStateOf("18") }; var aTar1 by remember { mutableStateOf("6") }
    var aCog1 by remember { mutableStateOf("4") }; var aTar2 by remember { mutableStateOf("8") }
    var aCog2 by remember { mutableStateOf("8") }; var aFel by remember { mutableStateOf("10") }
    var aDin by remember { mutableStateOf("5") }

    var yukleniyor by remember { mutableStateOf(false) }
    var sonuc by remember { mutableStateOf<String?>(null) }

    // Kronometre
    var saniye by remember { mutableIntStateOf(0) }
    var calisiyorMu by remember { mutableStateOf(false) }
    var seciliDers by remember { mutableStateOf("Matematik") }

    LaunchedEffect(calisiyorMu) {
        while (calisiyorMu) { delay(1000L); saniye++ }
    }

    // AI
    var aiSoru by remember { mutableStateOf("") }
    var aiCevap by remember { mutableStateOf("") }
    var aiYukleniyor by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(YksRenkler.Arka)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding()) {
            BaslikBolumu()
            SekmeCubugu(aktifSekme = aktifSekme, onSekme = { aktifSekme = it; sonuc = null })

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // 1. HESAPLAMA SEKMELERİ
                if (aktifSekme in listOf("TYT", "SAY", "EA", "SOZ")) {
                    ObpKarti(obp) { obp = it }
                    NetKarti(aktifSekme) {
                        when (aktifSekme) {
                            "TYT" -> {
                                NetSatiri("Türkçe", tTur, {tTur=it}, "Matematik", tMat, {tMat=it})
                                NetSatiri("Sosyal", tSos, {tSos=it}, "Fen", tFen, {tFen=it})
                            }
                            "SAY" -> {
                                NetSatiri("AYT Mat", aMat, {aMat=it}, "Fizik", aFiz, {aFiz=it})
                                NetSatiri("Kimya", aKim, {aKim=it}, "Biyoloji", aBio, {aBio=it})
                            }
                            "EA" -> {
                                NetSatiri("AYT Mat", aMat, {aMat=it}, "Edebiyat", aEdb, {aEdb=it})
                                NetSatiri("Tarih-1", aTar1, {aTar1=it}, "Coğrafya-1", aCog1, {aCog1=it})
                            }
                            "SOZ" -> {
                                NetSatiri("Edebiyat", aEdb, {aEdb=it}, "Tarih-2", aTar2, {aTar2=it})
                                NetSatiri("Coğrafya-2", aCog2, {aCog2=it}, "Fel/Din", aFel, {aFel=it})
                            }
                        }
                    }
                    GradyanButon("⚡ Hesapla", VurguGradyan, yukleniyor = yukleniyor) {
                        yukleniyor = true
                        val req = HesaplaRequest(
                            obp.toDoubleOrNull() ?: 0.0,
                            TytNetleri(tTur.toDoubleOrNull() ?: 0.0, tMat.toDoubleOrNull() ?: 0.0, tSos.toDoubleOrNull() ?: 0.0, tFen.toDoubleOrNull() ?: 0.0),
                            AytSayisalNetleri(aMat.toDoubleOrNull() ?: 0.0, aFiz.toDoubleOrNull() ?: 0.0, aKim.toDoubleOrNull() ?: 0.0, aBio.toDoubleOrNull() ?: 0.0),
                            AytEaNetleri(aMat.toDoubleOrNull() ?: 0.0, aEdb.toDoubleOrNull() ?: 0.0, aTar1.toDoubleOrNull() ?: 0.0, aCog1.toDoubleOrNull() ?: 0.0),
                            AytSozelNetleri(aEdb.toDoubleOrNull() ?: 0.0, aTar1.toDoubleOrNull() ?: 0.0, aCog1.toDoubleOrNull() ?: 0.0, aTar2.toDoubleOrNull() ?: 0.0, aCog2.toDoubleOrNull() ?: 0.0, aFel.toDoubleOrNull() ?: 0.0, aDin.toDoubleOrNull() ?: 0.0)
                        )
                        apiService.puanHesapla(req).enqueue(object : Callback<HesaplaResponse> {
                            override fun onResponse(call: Call<HesaplaResponse>, response: Response<HesaplaResponse>) {
                                yukleniyor = false
                                if (response.isSuccessful) {
                                    sonuc = "Tahmini Sıralama: ${response.body()?.sonuclar?.get(aktifSekme)?.siralama ?: "N/A"}"
                                }
                            }
                            override fun onFailure(call: Call<HesaplaResponse>, t: Throwable) { yukleniyor = false }
                        })
                    }
                    sonuc?.let { SonucKarti(it) }
                }

                // 2. ÇALIŞMA SEKME
                if (aktifSekme == "CALIS") {
                    KronometreKarti("%02d:%02d:%02d".format(saniye/3600, (saniye%3600)/60, saniye%60), calisiyorMu, seciliDers)
                    DersSecimKarti(DERSLER, seciliDers) { seciliDers = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { calisiyorMu = !calisiyorMu }, modifier = Modifier.weight(1f)) { Text(if(calisiyorMu) "Durdur" else "Başlat") }
                        OutlinedButton(onClick = { saniye = 0; calisiyorMu = false }, modifier = Modifier.weight(1f)) { Text("Sıfırla") }
                    }
                    Spacer(Modifier.height(10.dp))
                    GradyanButon("✓ Çalışmayı Kaydet", YesilGradyan) {
                        apiService.calismaKaydet(StudyLogRequest(seciliDers, saniye/60)).enqueue(object : Callback<SimpleResponse> {
                            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                                Toast.makeText(context, "Kaydedildi!", Toast.LENGTH_SHORT).show()
                                saniye = 0
                            }
                            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {}
                        })
                    }
                }

                // 3. AI KOÇ SEKME
                if (aktifSekme == "AI") {
                    AiKocBubble(aiCevap, aiYukleniyor)
                    OutlinedTextField(
                        value = aiSoru,
                        onValueChange = { aiSoru = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        placeholder = { Text("AI Koç'a bir soru sor...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    GradyanButon("🚀 Soruyu Gönder", VurguGradyan, yukleniyor = aiYukleniyor) {
                        aiYukleniyor = true
                        apiService.yksAiDanis(AiDanismanRequest(soru = aiSoru)).enqueue(object : Callback<AiResponse> {
                            override fun onResponse(call: Call<AiResponse>, response: Response<AiResponse>) {
                                aiYukleniyor = false
                                aiCevap = response.body()?.cevap ?: "Yanıt alınamadı."
                            }
                            override fun onFailure(call: Call<AiResponse>, t: Throwable) { aiYukleniyor = false }
                        })
                    }
                }

                // 4. SORU ÇÖZ SEKME
                if (aktifSekme == "SORU") {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YksRenkler.Yuzey).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp)).padding(24.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📸", fontSize = 48.sp)
                            Text("AI Soru Çözücü", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Sorunun fotoğrafını çek ve AI anında çözsün.", color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            GradyanButon("Sayfayı Aç", VurguGradyan) {
                                context.startActivity(Intent(context, SoruCozActivity::class.java))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── UI BİLEŞENLERİ ──────────────────────────────────────────────────────────

@Composable
fun BaslikBolumu() {
    Column(modifier = Modifier.padding(start = 20.dp, top = 40.dp, end = 20.dp, bottom = 10.dp)) {
        Row {
            Text(
                text = "YKS ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Asistan", // Burada 'text =' ekledik
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(brush = BaslikGradyan) // Brush'ı style içine aldık, en güvenli yol budur
            )
        }
    }
}

@Composable
fun SekmeCubugu(aktifSekme: String, onSekme: (String) -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp)).background(YksRenkler.Yuzey).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp)).padding(4.dp)) {
        LazyRow {
            itemsIndexed(SEKMELER) { _, s ->
                val aktif = s.id == aktifSekme
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        // HATA BURADAYDI: Parametreleri isimlendirerek çakışmayı önledik
                        .then(
                            if (aktif) Modifier.background(brush = VurguGradyan)
                            else Modifier.background(color = Color.Transparent)
                        )
                        .clickable { onSekme(s.id) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${s.emoji} ${s.etiket}",
                        color = if (aktif) Color.White else YksRenkler.YaziMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ObpKarti(obp: String, onObp: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text("OBP PUANI", color = YksRenkler.YaziSecond, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = obp, onValueChange = onObp, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
fun NetKarti(baslik: String, icerik: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(YksRenkler.Yuzey, RoundedCornerShape(16.dp)).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Text(baslik, color = YksRenkler.Vurgu, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        icerik()
    }
}

@Composable
fun NetSatiri(l1: String, v1: String, on1: (String)->Unit, l2: String, v2: String, on2: (String)->Unit) {
    Row(modifier = Modifier.padding(top = 8.dp)) {
        OutlinedTextField(v1, on1, label = {Text(l1, fontSize = 10.sp)}, modifier = Modifier.weight(1f), singleLine = true)
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(v2, on2, label = {Text(l2, fontSize = 10.sp)}, modifier = Modifier.weight(1f), singleLine = true)
    }
}

@Composable
fun GradyanButon(metin: String, gradyan: Brush, yukleniyor: Boolean = false, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)).background(gradyan).clickable { onClick() }, contentAlignment = Alignment.Center) {
        if(yukleniyor) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        else Text(metin, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SonucKarti(metin: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = YksRenkler.VurguSoft)) {
        Text(metin, modifier = Modifier.padding(16.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun KronometreKarti(sure: String, calisiyorMu: Boolean, ders: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
        Text(text = sure, fontSize = 54.sp, fontWeight = FontWeight.Bold, color = if (calisiyorMu) YksRenkler.Yesil else Color.White)
        Text("Ders: $ders", color = YksRenkler.YaziSecond, fontSize = 14.sp)
    }
}

@Composable
fun DersSecimKarti(dersler: List<String>, secili: String, onSec: (String) -> Unit) {
    LazyRow(modifier = Modifier.padding(vertical = 12.dp)) {
        itemsIndexed(dersler) { _, d ->
            Box(modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(10.dp)).background(if(d == secili) YksRenkler.VurguSoft else YksRenkler.YuzeyAlt).clickable { onSec(d) }.padding(12.dp, 8.dp)) {
                Text(d, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AiKocBubble(cevap: String, yukleniyor: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = YksRenkler.YuzeyAlt)) {
        Text(text = if (yukleniyor) "Düşünüyor..." else cevap.ifEmpty { "Merhaba! Sana nasıl yardımcı olabilirim?" }, modifier = Modifier.padding(16.dp), color = Color.White)
    }
}