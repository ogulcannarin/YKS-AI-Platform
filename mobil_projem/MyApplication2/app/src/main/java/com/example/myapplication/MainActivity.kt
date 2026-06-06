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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.util.Calendar

// ─── Veri Tipleri ─────────────────────────────────────────────────────────────

data class YksSekmesi(val id: String, val emoji: String, val etiket: String)

val DERSLER = listOf("Matematik", "Türkçe", "Fizik", "Kimya", "Biyoloji", "Edebiyat")

enum class AltSekme(val baslik: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HESAPLA("Hesapla", Icons.Rounded.Calculate),
    AI("AI Asistan", Icons.Rounded.AutoAwesome),
    KRONOMETRE("Çalışma", Icons.Rounded.Timer),
    KONU("Konular", Icons.Rounded.List),
    PROFIL("Profil", Icons.Rounded.Person)
}

// ─── Activity ─────────────────────────────────────────────────────────────────

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

// ─── Ana UI ───────────────────────────────────────────────────────────────────

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

    // Net Değerleri
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

    // AI - Sohbet Hafızası
    var aiSoru by remember { mutableStateOf("") }
    var aiMesajlar by remember { mutableStateOf<List<Pair<String,String>>>(emptyList()) } // Pair(role, content)
    var aiYukleniyor by remember { mutableStateOf(false) }
    var aiSessionId by remember { mutableStateOf<String?>(null) }
    var aiOturumlar by remember { mutableStateOf<List<ChatOturum>>(emptyList()) }
    var aiOturumGoster by remember { mutableStateOf(false) }
    var aiOturumYukleniyor by remember { mutableStateOf(false) }

    // Oturumları yükle (sekme açılınca)
    LaunchedEffect(altSekme) {
        if (altSekme == AltSekme.AI && aiOturumlar.isEmpty()) {
            aiOturumYukleniyor = true
            apiService.sohbetOturumlariniGetir(userEmail).enqueue(object : Callback<SohbetOturumlariResponse> {
                override fun onResponse(call: Call<SohbetOturumlariResponse>, response: Response<SohbetOturumlariResponse>) {
                    aiOturumYukleniyor = false
                    aiOturumlar = response.body()?.oturumlar ?: emptyList()
                }
                override fun onFailure(call: Call<SohbetOturumlariResponse>, t: Throwable) { aiOturumYukleniyor = false }
            })
        }
    }

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
            BaslikBolumu(userEmail)

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                AnimatedContent(
                    targetState = altSekme,
                    transitionSpec = {
                        fadeIn(tween(200)) + slideInHorizontally(tween(220)) { it / 12 } togetherWith
                        fadeOut(tween(150))
                    },
                    label = "tab_anim"
                ) { sekme ->
                    Column {
                        when (sekme) {
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
                                            NetSatiri("Türkçe", tTur, { tTur = it }, "Matematik", tMat, { tMat = it })
                                            NetSatiri("Sosyal", tSos, { tSos = it }, "Fen", tFen, { tFen = it })
                                        }
                                        "SAY" -> {
                                            NetSatiri("AYT Mat", aMat, { aMat = it }, "Fizik", aFiz, { aFiz = it })
                                            NetSatiri("Kimya", aKim, { aKim = it }, "Biyoloji", aBio, { aBio = it })
                                        }
                                        "EA" -> {
                                            NetSatiri("AYT Mat", aMat, { aMat = it }, "Edebiyat", aEdb, { aEdb = it })
                                            NetSatiri("Tarih-1", aTar1, { aTar1 = it }, "Coğrafya-1", aCog1, { aCog1 = it })
                                        }
                                        "SOZ" -> {
                                            NetSatiri("Edebiyat", aEdb, { aEdb = it }, "Tarih-2", aTar2, { aTar2 = it })
                                            NetSatiri("Coğrafya-2", aCog2, { aCog2 = it }, "Fel/Din", aFel, { aFel = it })
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                GradyanButon("⚡  Hesapla", VurguGradyan, yukleniyor = yukleniyor) {
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
                                AnimatedVisibility(visible = sonuc != null, enter = fadeIn() + expandVertically()) {
                                    sonuc?.let { SonucKarti(it) }
                                }
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
                                Spacer(Modifier.height(12.dp))

                                if (aiSekme == "SOHBET") {
                                    // ── Geçmiş Oturumlar Paneli ──────────────────────
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(YksRenkler.YuzeyAlt)
                                            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp))
                                    ) {
                                        Column {
                                            // Başlık satırı
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { aiOturumGoster = !aiOturumGoster }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Rounded.History, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Geçmiş Sohbetler", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    if (aiOturumlar.isNotEmpty()) {
                                                        Spacer(Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(YksRenkler.Vurgu)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("${aiOturumlar.size}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                Icon(
                                                    if (aiOturumGoster) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                    contentDescription = null,
                                                    tint = YksRenkler.YaziMuted,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Oturum listesi
                                            AnimatedVisibility(visible = aiOturumGoster) {
                                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                                    HorizontalDivider(color = YksRenkler.Kenar)
                                                    if (aiOturumYukleniyor) {
                                                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                                            CircularProgressIndicator(color = YksRenkler.Vurgu, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                                        }
                                                    } else if (aiOturumlar.isEmpty()) {
                                                        Text(
                                                            "Henüz sohbet geçmişi yok",
                                                            color = YksRenkler.YaziMuted,
                                                            fontSize = 13.sp,
                                                            modifier = Modifier.padding(16.dp)
                                                        )
                                                    } else {
                                                        aiOturumlar.forEach { oturum ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        // Bu oturumu yükle
                                                                        aiSessionId = oturum.session_id
                                                                        aiYukleniyor = true
                                                                        aiOturumGoster = false
                                                                        apiService.sohbetGecmisiniGetir(userEmail, oturum.session_id)
                                                                            .enqueue(object : Callback<SohbetGecmisiResponse> {
                                                                                override fun onResponse(call: Call<SohbetGecmisiResponse>, response: Response<SohbetGecmisiResponse>) {
                                                                                    aiYukleniyor = false
                                                                                    aiMesajlar = response.body()?.mesajlar?.map {
                                                                                        Pair(it.role, it.content)
                                                                                    } ?: emptyList()
                                                                                }
                                                                                override fun onFailure(call: Call<SohbetGecmisiResponse>, t: Throwable) { aiYukleniyor = false }
                                                                            })
                                                                    }
                                                                    .background(
                                                                        if (aiSessionId == oturum.session_id) YksRenkler.VurguSoft else Color.Transparent
                                                                    )
                                                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(Icons.Rounded.Chat, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(14.dp))
                                                                Spacer(Modifier.width(10.dp))
                                                                Text(
                                                                    oturum.ilk_mesaj,
                                                                    color = if (aiSessionId == oturum.session_id) YksRenkler.Vurgu else YksRenkler.YaziSecond,
                                                                    fontSize = 13.sp,
                                                                    maxLines = 1,
                                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                            }
                                                            HorizontalDivider(color = YksRenkler.Kenar.copy(alpha = 0.4f))
                                                        }
                                                    }

                                                    // Yeni Sohbet Butonu
                                                    TextButton(
                                                        onClick = {
                                                            aiMesajlar = emptyList()
                                                            aiSessionId = null
                                                            aiOturumGoster = false
                                                        },
                                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Add, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("+ Yeni Sohbet", color = YksRenkler.Vurgu, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // ── Mesaj Baloncukları ───────────────────────────
                                    if (aiMesajlar.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(YksRenkler.YuzeyAlt)
                                                .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp))
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            aiMesajlar.forEach { (rol, icerik) ->
                                                val isUser = rol == "user"
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                                ) {
                                                    if (!isUser) {
                                                        Box(
                                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(VurguGradyan),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }
                                                        Spacer(Modifier.width(8.dp))
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f, fill = false)
                                                            .clip(RoundedCornerShape(
                                                                topStart = if (isUser) 14.dp else 4.dp,
                                                                topEnd = if (isUser) 4.dp else 14.dp,
                                                                bottomStart = 14.dp,
                                                                bottomEnd = 14.dp
                                                            ))
                                                            .background(if (isUser) YksRenkler.Vurgu else YksRenkler.Yuzey)
                                                            .border(
                                                                1.dp,
                                                                if (isUser) Color.Transparent else YksRenkler.Kenar,
                                                                RoundedCornerShape(topStart = if (isUser) 14.dp else 4.dp, topEnd = if (isUser) 4.dp else 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                                                            )
                                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                                    ) {
                                                        Text(
                                                            text = icerik,
                                                            color = Color.White,
                                                            fontSize = 14.sp,
                                                            lineHeight = 22.sp
                                                        )
                                                    }
                                                    if (isUser) {
                                                        Spacer(Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(YksRenkler.Yesil),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    } else {
                                        // İlk açılış - AI karşılama
                                        AiKocBubble("", aiYukleniyor)
                                    }

                                    if (aiYukleniyor && aiMesajlar.isNotEmpty()) {
                                        AiKocBubble("", true)
                                    }

                                    // ── Soru Giriş Alanı ─────────────────────────────
                                    OutlinedTextField(
                                        value = aiSoru,
                                        onValueChange = { aiSoru = it },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        placeholder = { Text("AI Koç'a bir soru sor...", color = YksRenkler.YaziMuted) },
                                        shape = RoundedCornerShape(16.dp),
                                        trailingIcon = {
                                            if (aiSoru.isNotBlank()) {
                                                IconButton(onClick = {
                                                    val soru = aiSoru
                                                    aiSoru = ""
                                                    aiYukleniyor = true
                                                    aiMesajlar = aiMesajlar + Pair("user", soru)
                                                    apiService.yksAiDanis(
                                                        AiDanismanRequest(
                                                            user_id = userEmail,
                                                            soru = soru,
                                                            session_id = aiSessionId
                                                        )
                                                    ).enqueue(object : Callback<AiResponse> {
                                                        override fun onResponse(call: Call<AiResponse>, response: Response<AiResponse>) {
                                                            aiYukleniyor = false
                                                            val body = response.body()
                                                            if (body != null) {
                                                                if (aiSessionId == null) {
                                                                    aiSessionId = body.session_id
                                                                    // Oturum listesini güncelle
                                                                    apiService.sohbetOturumlariniGetir(userEmail).enqueue(object : Callback<SohbetOturumlariResponse> {
                                                                        override fun onResponse(call: Call<SohbetOturumlariResponse>, r: Response<SohbetOturumlariResponse>) {
                                                                            aiOturumlar = r.body()?.oturumlar ?: emptyList()
                                                                        }
                                                                        override fun onFailure(call: Call<SohbetOturumlariResponse>, t: Throwable) {}
                                                                    })
                                                                }
                                                                aiMesajlar = aiMesajlar + Pair("ai", body.cevap)
                                                            }
                                                        }
                                                        override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                                                            aiYukleniyor = false
                                                            aiMesajlar = aiMesajlar + Pair("ai", "Bağlantı hatası. Backend çalışıyor mu?")
                                                        }
                                                    })
                                                }) {
                                                    Icon(Icons.Rounded.Send, contentDescription = "Gönder", tint = YksRenkler.Vurgu)
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = YksRenkler.Vurgu,
                                            unfocusedBorderColor = YksRenkler.Kenar,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = YksRenkler.Vurgu
                                        )
                                    )
                                    GradyanButon("🚀  Soruyu Gönder", VurguGradyan, yukleniyor = aiYukleniyor) {
                                        if (aiSoru.isBlank()) return@GradyanButon
                                        val soru = aiSoru
                                        aiSoru = ""
                                        aiYukleniyor = true
                                        aiMesajlar = aiMesajlar + Pair("user", soru)
                                        apiService.yksAiDanis(
                                            AiDanismanRequest(
                                                user_id = userEmail,
                                                soru = soru,
                                                session_id = aiSessionId
                                            )
                                        ).enqueue(object : Callback<AiResponse> {
                                            override fun onResponse(call: Call<AiResponse>, response: Response<AiResponse>) {
                                                aiYukleniyor = false
                                                val body = response.body()
                                                if (body != null) {
                                                    if (aiSessionId == null) {
                                                        aiSessionId = body.session_id
                                                        apiService.sohbetOturumlariniGetir(userEmail).enqueue(object : Callback<SohbetOturumlariResponse> {
                                                            override fun onResponse(call: Call<SohbetOturumlariResponse>, r: Response<SohbetOturumlariResponse>) {
                                                                aiOturumlar = r.body()?.oturumlar ?: emptyList()
                                                            }
                                                            override fun onFailure(call: Call<SohbetOturumlariResponse>, t: Throwable) {}
                                                        })
                                                    }
                                                    aiMesajlar = aiMesajlar + Pair("ai", body.cevap)
                                                }
                                            }
                                            override fun onFailure(call: Call<AiResponse>, t: Throwable) {
                                                aiYukleniyor = false
                                                aiMesajlar = aiMesajlar + Pair("ai", "Bağlantı hatası.")
                                            }
                                        })
                                    }

                                } else {
                                    // ── Soru Çöz Kartı ───────────────────────────────
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(YksRenkler.Yuzey)
                                            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp))
                                            .padding(32.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(VurguGradyan),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("📸", fontSize = 36.sp)
                                            }
                                            Spacer(Modifier.height(20.dp))
                                            Text("AI Soru Çözücü", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = "Yapamadığın sorunun fotoğrafını çek,\nyapay zeka adım adım senin için çözsün.",
                                                color = YksRenkler.YaziSecond,
                                                textAlign = TextAlign.Center,
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp
                                            )
                                            Spacer(Modifier.height(24.dp))
                                            GradyanButon("📷  Kamerayı Aç", VurguGradyan) {
                                                context.startActivity(Intent(context, SoruCozActivity::class.java))
                                            }
                                        }
                                    }
                                }
                            }

                            AltSekme.KRONOMETRE -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(YksRenkler.Yuzey)
                                        .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp))
                                        .padding(24.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        ArcKronometre(
                                            sure = "%02d:%02d:%02d".format(saniye / 3600, (saniye % 3600) / 60, saniye % 60),
                                            calisiyorMu = calisiyorMu,
                                            progress = (saniye % 3600).toFloat() / 3600f
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "Aktif Ders: $seciliDers",
                                            color = YksRenkler.YaziSecond,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        DersSecimKarti(DERSLER, seciliDers) { seciliDers = it }
                                        Spacer(Modifier.height(20.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Button(
                                                onClick = { calisiyorMu = !calisiyorMu },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (calisiyorMu) YksRenkler.Kirmizi else YksRenkler.Vurgu
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (calisiyorMu) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (calisiyorMu) "Durdur" else "Başlat",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { saniye = 0; calisiyorMu = false },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                border = BorderStroke(1.dp, YksRenkler.Kenar)
                                            ) {
                                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Sıfırla")
                                            }
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        GradyanButon("✓  Çalışmayı Kaydet", YesilGradyan) {
                                            val sharedPref = context.getSharedPreferences("profil_prefs", android.content.Context.MODE_PRIVATE)
                                            val currentTotalMinutes = sharedPref.getInt("total_study_minutes_$userEmail", 0)
                                            sharedPref.edit().putInt("total_study_minutes_$userEmail", currentTotalMinutes + (saniye / 60)).apply()
                                            apiService.calismaKaydet(StudyLogRequest(seciliDers, saniye / 60)).enqueue(object : Callback<SimpleResponse> {
                                                override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                                                    Toast.makeText(context, "✓ Kaydedildi!", Toast.LENGTH_SHORT).show()
                                                    saniye = 0
                                                }
                                                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {}
                                            })
                                        }
                                    }
                                }
                            }

                            AltSekme.KONU -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(YksRenkler.Yuzey)
                                        .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(24.dp))
                                        .padding(24.dp)
                                ) {
                                    KonuTakipEkrani(userEmail)
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

// ─── UI BİLEŞENLERİ ──────────────────────────────────────────────────────────

@Composable
fun ModernBottomNav(secili: AltSekme, onSec: (AltSekme) -> Unit) {
    NavigationBar(
        containerColor = YksRenkler.Arka,
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
            val iconSize by animateDpAsState(
                targetValue = if (isSelected) 27.dp else 24.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "icon_${sekme.name}"
            )
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSec(sekme) },
                icon = {
                    Icon(
                        imageVector = sekme.icon,
                        contentDescription = sekme.baslik,
                        modifier = Modifier.size(iconSize)
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
fun BaslikBolumu(userEmail: String = "") {
    val selamlama = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Günaydın ☀️"
            hour < 18 -> "İyi günler 👋"
            else -> "İyi akşamlar 🌙"
        }
    }
    val initial = userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 44.dp, end = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VurguGradyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.School, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(text = "YKS Asistan", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = selamlama, fontSize = 13.sp, color = YksRenkler.YaziSecond)
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(YksRenkler.VurguSoft)
                .border(1.5.dp, YksRenkler.Vurgu.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = YksRenkler.Vurgu,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SekmeCubugu(sekmeler: List<YksSekmesi>, aktifSekme: String, onSekme: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(YksRenkler.Yuzey)
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            itemsIndexed(sekmeler) { _, s ->
                val aktif = s.id == aktifSekme
                val bgColor by animateColorAsState(
                    targetValue = if (aktif) YksRenkler.Vurgu else Color.Transparent,
                    animationSpec = tween(220),
                    label = "tab_bg_${s.id}"
                )
                val txtColor by animateColorAsState(
                    targetValue = if (aktif) Color.White else YksRenkler.YaziMuted,
                    animationSpec = tween(220),
                    label = "tab_txt_${s.id}"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onSekme(s.id) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${s.emoji} ${s.etiket}", color = txtColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ObpKarti(obp: String, onObp: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            "OBP PUANI",
            color = YksRenkler.YaziSecond,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = obp,
            onValueChange = onObp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Icon(Icons.Rounded.School, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(20.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YksRenkler.Vurgu,
                unfocusedBorderColor = YksRenkler.Kenar,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = YksRenkler.Vurgu
            )
        )
    }
}

@Composable
fun NetKarti(baslik: String, icerik: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(YksRenkler.Yuzey, RoundedCornerShape(20.dp))
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(YksRenkler.VurguSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "$baslik NETLERİ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        icerik()
    }
}

@Composable
fun NetSatiri(l1: String, v1: String, on1: (String) -> Unit, l2: String, v2: String, on2: (String) -> Unit) {
    Row(modifier = Modifier.padding(top = 12.dp)) {
        OutlinedTextField(
            value = v1, onValueChange = on1,
            label = { Text(l1, fontSize = 11.sp, color = YksRenkler.YaziMuted) },
            modifier = Modifier.fillMaxWidth(0.48f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YksRenkler.Vurgu,
                unfocusedBorderColor = YksRenkler.Kenar,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = YksRenkler.Vurgu
            )
        )
        Spacer(Modifier.width(12.dp))
        OutlinedTextField(
            value = v2, onValueChange = on2,
            label = { Text(l2, fontSize = 11.sp, color = YksRenkler.YaziMuted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YksRenkler.Vurgu,
                unfocusedBorderColor = YksRenkler.Kenar,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = YksRenkler.Vurgu
            )
        )
    }
}

@Composable
fun GradyanButon(metin: String, gradyan: Brush, yukleniyor: Boolean = false, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(gradyan)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = true
                onClick()
                pressed = false
            },
        contentAlignment = Alignment.Center
    ) {
        if (yukleniyor) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
        } else {
            Text(metin, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SonucKarti(metin: String) {
    val siralama = metin.substringAfter("Sıralama: ").trim()
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, YksRenkler.Vurgu.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(YksRenkler.VurguSoft)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(YksRenkler.Altin.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = YksRenkler.Altin, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Tahmini Sıralamanız", color = YksRenkler.YaziSecond, fontSize = 12.sp)
                    Text(siralama, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun ArcKronometre(sure: String, calisiyorMu: Boolean, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseInOutSine),
        label = "arc_progress"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow_alpha"
    )

    Box(modifier = Modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // Arka plan ray'ı
            drawArc(
                color = YksRenkler.Kenar,
                startAngle = -210f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // İlerleme ark'ı
            if (animatedProgress > 0.002f) {
                val arcColor = if (calisiyorMu) YksRenkler.Yesil.copy(alpha = glowAlpha) else YksRenkler.Vurgu
                drawArc(
                    color = arcColor,
                    startAngle = -210f,
                    sweepAngle = 240f * animatedProgress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = sure, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (calisiyorMu) YksRenkler.Yesil else YksRenkler.YaziMuted)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = if (calisiyorMu) "Çalışıyor" else "Bekliyor",
                    color = if (calisiyorMu) YksRenkler.Yesil else YksRenkler.YaziMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DersSecimKarti(dersler: List<String>, secili: String, onSec: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(dersler) { _, d ->
            val selected = d == secili
            val bgColor by animateColorAsState(
                targetValue = if (selected) YksRenkler.Vurgu else YksRenkler.YuzeyAlt,
                animationSpec = tween(180),
                label = "chip_$d"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, if (selected) Color.Transparent else YksRenkler.Kenar, RoundedCornerShape(20.dp))
                    .clickable { onSec(d) }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(d, color = Color.White, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun AiKocBubble(cevap: String, yukleniyor: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp).copy(bottomStart = CornerSize(4.dp)),
        colors = CardDefaults.cardColors(containerColor = YksRenkler.YuzeyAlt),
        border = BorderStroke(1.dp, YksRenkler.Kenar)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(VurguGradyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("AI Koç", color = YksRenkler.Vurgu, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Text("YKS Uzmanı", color = YksRenkler.YaziMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = YksRenkler.Kenar)
            Spacer(Modifier.height(14.dp))
            if (yukleniyor) {
                TypingIndicator()
            } else {
                Text(
                    text = cevap.ifEmpty { "Merhaba! Hedeflerine ulaşman için sana nasıl yardımcı olabilirim?" },
                    color = YksRenkler.YaziPrimary,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Yanıt üretiliyor", color = YksRenkler.YaziSecond, fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(0, 180, 360).forEach { delayMs ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = delayMs),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$delayMs"
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(YksRenkler.Vurgu.copy(alpha = alpha))
                )
            }
        }
    }
}