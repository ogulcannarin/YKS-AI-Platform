package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

val VurguGradyan = Brush.linearGradient(colors = listOf(YksRenkler.Vurgu, YksRenkler.TabAktif2))
val YesilGradyan = Brush.linearGradient(colors = listOf(Color(0xFF00C87A), YksRenkler.Yesil))
val BaslikGradyan = Brush.linearGradient(colors = listOf(YksRenkler.Vurgu, Color(0xFFA78BFA), YksRenkler.Yesil))

data class YksSekmesi(val id: String, val emoji: String, val etiket: String)
val DERSLER = listOf("Matematik", "Türkçe", "Fizik", "Kimya", "Biyoloji", "Edebiyat")

enum class AltSekme(val baslik: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HESAPLA("Hesapla", Icons.Rounded.Calculate),
    AI("AI Asistan", Icons.Rounded.AutoAwesome),
    KRONOMETRE("Çalışma", Icons.Rounded.Timer),
    KONU("Konular", Icons.Rounded.List),
    PROFIL("Profil", Icons.Rounded.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
            MaterialTheme(colorScheme = darkColorScheme(background = YksRenkler.Arka)) {
                YksAsistanUI(userEmail)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }
}

@Composable
fun YksAsistanUI(userEmail: String = "") {
    val context = LocalContext.current
    var altSekme by remember { mutableStateOf(AltSekme.HESAPLA) }
    var hesaplaSekme by remember { mutableStateOf("TYT") }
    var aiSekme by remember { mutableStateOf("SOHBET") }

    LaunchedEffect(Unit) {
        val targetTab = (context as? ComponentActivity)?.intent?.getStringExtra("TARGET_TAB")
        if (targetTab != null) {
            try { altSekme = AltSekme.valueOf(targetTab) } catch (e: Exception) {}
        }
    }

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

    Scaffold(
        bottomBar = {
            ModernBottomNav(
                secili = altSekme,
                onSec = { sekme ->
                    if (sekme == AltSekme.PROFIL) {
                        val intent = Intent(context, ProfilActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        context.startActivity(intent)
                    } else {
                        altSekme = sekme
                    }
                }
            )
        },
        containerColor = YksRenkler.Arka
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            BaslikBolumu()

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                when (altSekme) {
                    AltSekme.HESAPLA -> {
                        SekmeCubugu(
                            sekmeler = listOf(
                                YksSekmesi("TYT", "📝", "TYT"),
                                YksSekmesi("SAY", "🔢", "Sayısal"),
                                YksSekmesi("EA", "📐", "EA"),
                                YksSekmesi("SOZ", "📚", "Sözel")
                            ),
                            aktifSekme = hesaplaSekme,
                            onSekme = { hesaplaSekme = it; sonuc = null }
                        )
                        Spacer(Modifier.height(16.dp))
                        ObpKarti(obp) { obp = it }
                        NetKarti(hesaplaSekme) {
                            when (hesaplaSekme) {
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
                        Spacer(Modifier.height(8.dp))
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
                                    if (response.isSuccessful) sonuc = "Tahmini Sıralama: ${response.body()?.sonuclar?.get(hesaplaSekme)?.siralama ?: "N/A"}"
                                }
                                override fun onFailure(call: Call<HesaplaResponse>, t: Throwable) { yukleniyor = false }
                            })
                        }
                        sonuc?.let { SonucKarti(it) }
                    }

                    AltSekme.AI -> {
                        SekmeCubugu(
                            sekmeler = listOf(
                                YksSekmesi("SOHBET", "💬", "AI Sohbet"),
                                YksSekmesi("SORU_COZ", "📸", "Soru Çöz")
                            ),
                            aktifSekme = aiSekme,
                            onSekme = { aiSekme = it }
                        )
                        Spacer(Modifier.height(16.dp))

                        if (aiSekme == "SOHBET") {
                            AiKocBubble(aiCevap, aiYukleniyor)
                            OutlinedTextField(
                                value = aiSoru,
                                onValueChange = { aiSoru = it },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                placeholder = { Text("AI Koç'a bir soru sor...", color = YksRenkler.YaziMuted) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = YksRenkler.Vurgu,
                                    unfocusedBorderColor = YksRenkler.Kenar,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
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
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YksRenkler.Yuzey).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp)).padding(32.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("📸", fontSize = 64.sp)
                                    Spacer(Modifier.height(16.dp))
                                    Text("AI Soru Çözücü", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Yapamadığın sorunun fotoğrafını çek, yapay zeka adım adım senin için çözsün.", color = YksRenkler.YaziSecond, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(24.dp))
                                    GradyanButon("Kamerayı Aç", VurguGradyan) {
                                        context.startActivity(Intent(context, SoruCozActivity::class.java))
                                    }
                                }
                            }
                        }
                    }

                    AltSekme.KRONOMETRE -> {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YksRenkler.Yuzey).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp)).padding(24.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                KronometreKarti("%02d:%02d:%02d".format(saniye/3600, (saniye%3600)/60, saniye%60), calisiyorMu, seciliDers)
                                DersSecimKarti(DERSLER, seciliDers) { seciliDers = it }
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { calisiyorMu = !calisiyorMu }, 
                                        modifier = Modifier.fillMaxWidth(0.48f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if(calisiyorMu) YksRenkler.Kirmizi else YksRenkler.Vurgu)
                                    ) { Text(if(calisiyorMu) "Durdur" else "Başlat", fontWeight = FontWeight.Bold) }
                                    
                                    OutlinedButton(
                                        onClick = { saniye = 0; calisiyorMu = false }, 
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, YksRenkler.Kenar)
                                    ) { Text("Sıfırla") }
                                }
                                Spacer(Modifier.height(16.dp))
                                GradyanButon("✓ Çalışmayı Kaydet", YesilGradyan) {
                                    val sharedPref = context.getSharedPreferences("profil_prefs", android.content.Context.MODE_PRIVATE)
                                    val currentTotalMinutes = sharedPref.getInt("total_study_minutes_$userEmail", 0)
                                    sharedPref.edit().putInt("total_study_minutes_$userEmail", currentTotalMinutes + (saniye / 60)).apply()

                                    apiService.calismaKaydet(StudyLogRequest(seciliDers, saniye/60)).enqueue(object : Callback<SimpleResponse> {
                                        override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                                            Toast.makeText(context, "Kaydedildi!", Toast.LENGTH_SHORT).show()
                                            saniye = 0
                                        }
                                        override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {}
                                    })
                                }
                            }
                        }
                    }
                    AltSekme.KONU -> {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(YksRenkler.Yuzey).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp)).padding(24.dp)) {
                            KonuTakipEkrani(userEmail)
                        }
                    }
                    else -> {}
                }
            }
            Spacer(Modifier.height(100.dp)) // Nav bar padding
        }
    }
}

// ─── UI BİLEŞENLERİ ──────────────────────────────────────────────────────────

@Composable
fun ModernBottomNav(secili: AltSekme, onSec: (AltSekme) -> Unit) {
    NavigationBar(
        containerColor = YksRenkler.Arka, // Arka plana uyumlu
        contentColor = YksRenkler.YaziMuted,
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
            drawLine(
                color = YksRenkler.Kenar,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        AltSekme.values().forEach { sekme ->
            val isSelected = secili == sekme
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSec(sekme) },
                icon = {
                    Icon(
                        imageVector = sekme.icon, 
                        contentDescription = sekme.baslik,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = { Text(sekme.baslik, fontWeight = FontWeight.SemiBold, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YksRenkler.Vurgu,
                    selectedTextColor = YksRenkler.Vurgu,
                    indicatorColor = YksRenkler.VurguSoft,
                    unselectedIconColor = YksRenkler.YaziMuted,
                    unselectedTextColor = YksRenkler.YaziMuted
                )
            )
        }
    }
}

@Composable
fun BaslikBolumu() {
    Column(modifier = Modifier.padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VurguGradyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.School, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = "YKS Asistan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = "Yapay Zeka Destekli Koçun", fontSize = 13.sp, color = YksRenkler.YaziSecond)
            }
        }
    }
}

@Composable
fun SekmeCubugu(sekmeler: List<YksSekmesi>, aktifSekme: String, onSekme: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(YksRenkler.Yuzey).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp)).padding(4.dp)) {
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            itemsIndexed(sekmeler) { _, s ->
                val aktif = s.id == aktifSekme
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(if (aktif) Modifier.background(brush = VurguGradyan) else Modifier.background(color = Color.Transparent))
                        .clickable { onSekme(s.id) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${s.emoji} ${s.etiket}", color = if (aktif) Color.White else YksRenkler.YaziMuted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ObpKarti(obp: String, onObp: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text("OBP PUANI", color = YksRenkler.YaziSecond, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        OutlinedTextField(
            value = obp, onValueChange = onObp, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YksRenkler.Vurgu,
                unfocusedBorderColor = YksRenkler.Kenar,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
fun NetKarti(baslik: String, icerik: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(YksRenkler.Yuzey, RoundedCornerShape(20.dp)).border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp)).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("$baslik NETLERİ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))
        icerik()
    }
}

@Composable
fun NetSatiri(l1: String, v1: String, on1: (String)->Unit, l2: String, v2: String, on2: (String)->Unit) {
    Row(modifier = Modifier.padding(top = 12.dp)) {
        OutlinedTextField(
            value = v1, onValueChange = on1, label = {Text(l1, fontSize = 11.sp, color = YksRenkler.YaziMuted)}, 
            modifier = Modifier.fillMaxWidth(0.48f), singleLine = true, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = YksRenkler.Vurgu, unfocusedBorderColor = YksRenkler.Kenar, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = v2, onValueChange = on2, label = {Text(l2, fontSize = 11.sp, color = YksRenkler.YaziMuted)}, 
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = YksRenkler.Vurgu, unfocusedBorderColor = YksRenkler.Kenar, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
    }
}

@Composable
fun GradyanButon(metin: String, gradyan: Brush, yukleniyor: Boolean = false, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(gradyan).clickable { onClick() }, contentAlignment = Alignment.Center) {
        if(yukleniyor) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
        else Text(metin, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
fun SonucKarti(metin: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp), 
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YksRenkler.VurguSoft),
        border = BorderStroke(1.dp, YksRenkler.Vurgu.copy(alpha=0.5f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Text(metin, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun KronometreKarti(sure: String, calisiyorMu: Boolean, ders: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Box(
            modifier = Modifier.size(160.dp).clip(CircleShape).border(4.dp, if (calisiyorMu) YksRenkler.Yesil else YksRenkler.Kenar, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = sure, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text("Aktif Ders: $ders", color = YksRenkler.YaziSecond, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DersSecimKarti(dersler: List<String>, secili: String, onSec: (String) -> Unit) {
    LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(dersler) { _, d ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if(d == secili) YksRenkler.Vurgu else YksRenkler.Arka)
                    .border(1.dp, if(d == secili) Color.Transparent else YksRenkler.Kenar, RoundedCornerShape(12.dp))
                    .clickable { onSec(d) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(d, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AiKocBubble(cevap: String, yukleniyor: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), 
        shape = RoundedCornerShape(16.dp).copy(bottomStart = CornerSize(4.dp)),
        colors = CardDefaults.cardColors(containerColor = YksRenkler.YuzeyAlt)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("AI Koç", color = YksRenkler.Vurgu, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            if (yukleniyor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = YksRenkler.YaziSecond, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Yanıt üretiliyor...", color = YksRenkler.YaziSecond, fontSize = 14.sp)
                }
            } else {
                Text(text = cevap.ifEmpty { "Merhaba! Hedeflerine ulaşman için sana nasıl yardımcı olabilirim?" }, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
    }
}