package com.example.myapplication

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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

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
    val TabAktif1   = Color(0xFF6C63FF)
    val TabAktif2   = Color(0xFF8B83FF)
    val ObpGrad1    = Color(0x1A6C63FF)
    val ObpKenari   = Color(0x406C63FF)
}

// ─── Gradyanlar ───────────────────────────────────────────────────────────────
val VurguGradyan = Brush.linearGradient(
    colors = listOf(YksRenkler.Vurgu, YksRenkler.TabAktif2),
    start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)
val YesilGradyan = Brush.linearGradient(
    colors = listOf(Color(0xFF00C87A), YksRenkler.Yesil)
)
val BaslikGradyan = Brush.linearGradient(
    colors = listOf(YksRenkler.Vurgu, Color(0xFFA78BFA), YksRenkler.Yesil)
)
val KartUstCizgi = Brush.horizontalGradient(
    colors = listOf(Color.Transparent, Color(0x666C63FF), Color.Transparent)
)

// ─── Sekme Modeli ─────────────────────────────────────────────────────────────
data class YksSekmesi(val id: String, val emoji: String, val etiket: String)

val SEKMELER = listOf(
    YksSekmesi("TYT",   "📝", "TYT"),
    YksSekmesi("SAY",   "🔢", "Sayısal"),
    YksSekmesi("EA",    "📐", "EA"),
    YksSekmesi("SOZ",   "📚", "Sözel"),
    YksSekmesi("CALIS", "⏱", "Çalışma"),
    YksSekmesi("AI",    "🤖", "AI Koç"),
)

val DERSLER = listOf("Matematik", "Türkçe", "Fizik", "Kimya", "Biyoloji", "Edebiyat")

// ─── Activity ─────────────────────────────────────────────────────────────────
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

// ─── Ana Ekran ─────────────────────────────────────────────────────────────────
@Composable
fun YksAsistanUI() {
    val context = LocalContext.current

    // Sekme
    var aktifSekme by remember { mutableStateOf("TYT") }

    // Yükleniyor / Sonuç
    var yukleniyor by remember { mutableStateOf(false) }
    var sonuc     by remember { mutableStateOf<String?>(null) }

    // Net değerleri
    var obp   by remember { mutableStateOf("85.0") }
    var tTur  by remember { mutableStateOf("30") }
    var tMat  by remember { mutableStateOf("25") }
    var tSos  by remember { mutableStateOf("15") }
    var tFen  by remember { mutableStateOf("10") }
    var aMat  by remember { mutableStateOf("20") }
    var aFiz  by remember { mutableStateOf("10") }
    var aKim  by remember { mutableStateOf("10") }
    var aBio  by remember { mutableStateOf("10") }
    var aEdb  by remember { mutableStateOf("18") }
    var aTar1 by remember { mutableStateOf("6") }
    var aCog1 by remember { mutableStateOf("4") }
    var aTar2 by remember { mutableStateOf("8") }
    var aCog2 by remember { mutableStateOf("8") }
    var aFel  by remember { mutableStateOf("10") }
    var aDin  by remember { mutableStateOf("5") }

    // Çalışma / Kronometre
    var seciliDers   by remember { mutableStateOf("Matematik") }
    var saniye       by remember { mutableIntStateOf(0) }
    var calisiyorMu  by remember { mutableStateOf(false) }

    LaunchedEffect(calisiyorMu) {
        while (calisiyorMu) { delay(1000L); saniye++ }
    }

    val formatliSure = remember(saniye) {
        "%02d:%02d:%02d".format(saniye / 3600, (saniye % 3600) / 60, saniye % 60)
    }

    // AI Koç
    var aiSoru    by remember { mutableStateOf("") }
    var aiCevap   by remember { mutableStateOf("") }
    var aiYukleniyor by remember { mutableStateOf(false) }

    // Arka plan orb animasyonu
    val orbAlfa by rememberInfiniteTransition(label = "orb").animateFloat(
        initialValue = 0.06f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orbAlfa"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YksRenkler.Arka)
    ) {
        // Arka plan dekoratif orb'lar
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = 150.dp, y = (-80).dp)
                .blur(80.dp)
                .background(
                    YksRenkler.Vurgu.copy(alpha = orbAlfa),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = 500.dp)
                .blur(80.dp)
                .background(
                    YksRenkler.Yesil.copy(alpha = orbAlfa * 0.5f),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Başlık ──
            BaslikBolumu()

            // ── Sekme Çubuğu ──
            SekmeCubugu(aktifSekme = aktifSekme, onSekme = { aktifSekme = it; sonuc = null })

            // ── İçerik ──
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // OBP Kartı (hesaplama sekmelerinde göster)
                AnimatedVisibility(aktifSekme in listOf("TYT", "SAY", "EA", "SOZ")) {
                    ObpKarti(obp = obp, onObp = { obp = it })
                }

                // TYT
                AnimatedVisibility(aktifSekme == "TYT", enter = fadeIn() + slideInVertically()) {
                    NetKarti(baslik = "TYT Netleri") {
                        NetSatiri("Türkçe", tTur, { tTur = it }, "Matematik", tMat, { tMat = it })
                        NetSatiri("Sosyal Bilimler", tSos, { tSos = it }, "Fen Bilimleri", tFen, { tFen = it })
                    }
                }

                // SAY
                AnimatedVisibility(aktifSekme == "SAY", enter = fadeIn() + slideInVertically()) {
                    NetKarti(baslik = "AYT Sayısal Netleri") {
                        NetSatiri("TYT Türkçe", tTur, { tTur = it }, "TYT Matematik", tMat, { tMat = it })
                        Divider(color = YksRenkler.Kenar, modifier = Modifier.padding(vertical = 10.dp))
                        NetSatiri("AYT Matematik", aMat, { aMat = it }, "Fizik", aFiz, { aFiz = it })
                        NetSatiri("Kimya", aKim, { aKim = it }, "Biyoloji", aBio, { aBio = it })
                    }
                }

                // EA
                AnimatedVisibility(aktifSekme == "EA", enter = fadeIn() + slideInVertically()) {
                    NetKarti(baslik = "AYT Eşit Ağırlık Netleri") {
                        NetSatiri("TYT Türkçe", tTur, { tTur = it }, "TYT Matematik", tMat, { tMat = it })
                        Divider(color = YksRenkler.Kenar, modifier = Modifier.padding(vertical = 10.dp))
                        NetSatiri("AYT Matematik", aMat, { aMat = it }, "Edebiyat", aEdb, { aEdb = it })
                        NetSatiri("Tarih-1", aTar1, { aTar1 = it }, "Coğrafya-1", aCog1, { aCog1 = it })
                    }
                }

                // SÖZ
                AnimatedVisibility(aktifSekme == "SOZ", enter = fadeIn() + slideInVertically()) {
                    NetKarti(baslik = "AYT Sözel Netleri") {
                        NetSatiri("Edebiyat", aEdb, { aEdb = it }, "Tarih-1", aTar1, { aTar1 = it })
                        NetSatiri("Coğrafya-1", aCog1, { aCog1 = it }, "Tarih-2", aTar2, { aTar2 = it })
                        NetSatiri("Coğrafya-2", aCog2, { aCog2 = it }, "Felsefe", aFel, { aFel = it })
                        NetAlanTekli("Din Kültürü", aDin, { aDin = it })
                    }
                }

                // Hesapla Butonu + Sonuç
                AnimatedVisibility(aktifSekme in listOf("TYT", "SAY", "EA", "SOZ")) {
                    Column {
                        Spacer(Modifier.height(4.dp))
                        GradyanButon(
                            metin = if (yukleniyor) "Hesaplanıyor..." else "⚡  Hesapla ve Kaydet",
                            gradyan = VurguGradyan,
                            yukleniyor = yukleniyor,
                            onClick = {
                                yukleniyor = true
                                // API çağrısı burada yapılır; demo için simüle ediyoruz
                            }
                        )
                        AnimatedVisibility(sonuc != null) {
                            sonuc?.let { SonucKarti(metin = it) }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── Çalışma Sekmesi ──
                AnimatedVisibility(aktifSekme == "CALIS", enter = fadeIn() + slideInVertically()) {
                    Column {
                        KronometreKarti(
                            sure = formatliSure,
                            calisiyorMu = calisiyorMu,
                            ders = seciliDers
                        )
                        DersSecimKarti(
                            dersler = DERSLER,
                            secili = seciliDers,
                            onSec = { seciliDers = it }
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!calisiyorMu) {
                                OutlinedButton(
                                    onClick = { calisiyorMu = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFA8A4FF)
                                    ),
                                    border = BorderStroke(1.dp, Color(0x406C63FF))
                                ) {
                                    Text(
                                        if (saniye == 0) "▶  Başlat" else "▶  Devam",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { calisiyorMu = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = YksRenkler.Kirmizi.copy(alpha = 0.15f),
                                        contentColor = YksRenkler.Kirmizi
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text("⏸  Durdur", fontWeight = FontWeight.SemiBold)
                                }
                            }
                            OutlinedButton(
                                onClick = { calisiyorMu = false; saniye = 0 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = YksRenkler.YaziSecond
                                ),
                                border = BorderStroke(1.dp, YksRenkler.Kenar)
                            ) {
                                Text("↺  Sıfırla")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        GradyanButon(
                            metin = "✓  Çalışma Bitti — Kaydet (${saniye / 60} dk)",
                            gradyan = YesilGradyan,
                            metinRengi = Color(0xFF001A0F),
                            aktif = saniye > 0,
                            onClick = {
                                calisiyorMu = false
                                val dk = saniye / 60
                                Toast.makeText(context, "$dk dakika $seciliDers kaydedildi! 🎉", Toast.LENGTH_SHORT).show()
                                saniye = 0
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── AI Koç Sekmesi ──
                AnimatedVisibility(aktifSekme == "AI", enter = fadeIn() + slideInVertically()) {
                    Column {
                        AiKocBubble(cevap = aiCevap, yukleniyor = aiYukleniyor)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = aiSoru,
                            onValueChange = { aiSoru = it },
                            placeholder = {
                                Text(
                                    "Örn: TYT matematik için en etkili strateji nedir?",
                                    color = YksRenkler.YaziMuted,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YksRenkler.Vurgu,
                                unfocusedBorderColor = YksRenkler.Kenar,
                                focusedContainerColor = YksRenkler.YuzeyAlt,
                                unfocusedContainerColor = YksRenkler.YuzeyAlt,
                                focusedTextColor = YksRenkler.YaziPrimary,
                                unfocusedTextColor = YksRenkler.YaziPrimary,
                                cursorColor = YksRenkler.Vurgu
                            ),
                            textStyle = TextStyle(fontSize = 14.sp),
                            minLines = 3,
                            maxLines = 5
                        )
                        Spacer(Modifier.height(12.dp))
                        GradyanButon(
                            metin = if (aiYukleniyor) "Yanıt geliyor..." else "🚀  AI Koç'a Gönder",
                            gradyan = VurguGradyan,
                            yukleniyor = aiYukleniyor,
                            aktif = aiSoru.isNotBlank(),
                            onClick = { /* API çağrısı */ }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ─── Başlık Bölümü ─────────────────────────────────────────────────────────────
@Composable
fun BaslikBolumu() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 20.dp)) {
        // Rozet
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(YksRenkler.VurguSoft)
                .border(1.dp, Color(0x4D6C63FF), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                "🎯  2025 SINAV SEZONU",
                color = Color(0xFFA8A4FF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Ana başlık
        Row {
            Text(
                "YKS ",
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = YksRenkler.YaziPrimary,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                "Asistan",
                style = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush = BaslikGradyan,
                    letterSpacing = (-0.5).sp
                )
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            "Puan hesapla · Çalışmalarını takip et · AI ile öğren",
            color = YksRenkler.YaziSecond,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light
        )
    }
}

// ─── Sekme Çubuğu ──────────────────────────────────────────────────────────────
@Composable
fun SekmeCubugu(aktifSekme: String, onSekme: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(YksRenkler.Yuzey)
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(SEKMELER) { _, sekme ->
                val aktif = sekme.id == aktifSekme
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (aktif) Modifier.background(VurguGradyan)
                            else Modifier.background(Color.Transparent)
                        )
                        .clickable { onSekme(sekme.id) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(sekme.emoji, fontSize = 16.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            sekme.etiket,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (aktif) Color.White else YksRenkler.YaziMuted,
                            letterSpacing = 0.04.sp
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
}

// ─── OBP Kartı ─────────────────────────────────────────────────────────────────
@Composable
fun ObpKarti(obp: String, onObp: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0x1A6C63FF), Color(0x0D00E5A0))
                )
            )
            .border(1.dp, Color(0x406C63FF), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                "📊  ORTAÖĞRETİM BAŞARI PUANI (OBP)",
                color = Color(0xFFA8A4FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.08.sp
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = obp,
                onValueChange = onObp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YksRenkler.Vurgu,
                    unfocusedBorderColor = Color(0x406C63FF),
                    focusedContainerColor = Color(0x1A6C63FF),
                    unfocusedContainerColor = Color(0x0D6C63FF),
                    focusedTextColor = YksRenkler.YaziPrimary,
                    unfocusedTextColor = YksRenkler.YaziPrimary,
                    cursorColor = YksRenkler.Vurgu
                ),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
    Spacer(Modifier.height(14.dp))
}

// ─── Net Kartı Wrapper ─────────────────────────────────────────────────────────
@Composable
fun NetKarti(baslik: String, icerik: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(YksRenkler.Yuzey)
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp))
    ) {
        // Üst gradient çizgi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KartUstCizgi)
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(listOf(YksRenkler.Vurgu, YksRenkler.Yesil))
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    baslik.uppercase(),
                    color = YksRenkler.YaziSecond,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp
                )
            }
            icerik()
        }
    }
    Spacer(Modifier.height(14.dp))
}

// ─── Net Satırı ────────────────────────────────────────────────────────────────
@Composable
fun NetSatiri(
    etiket1: String, deger1: String, onChange1: (String) -> Unit,
    etiket2: String, deger2: String, onChange2: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        NetAlan(etiket1, deger1, onChange1, Modifier.weight(1f))
        NetAlan(etiket2, deger2, onChange2, Modifier.weight(1f))
    }
}

@Composable
fun NetAlan(etiket: String, deger: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            etiket.uppercase(),
            color = YksRenkler.YaziSecond,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.06.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        OutlinedTextField(
            value = deger,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YksRenkler.Vurgu,
                unfocusedBorderColor = YksRenkler.Kenar,
                focusedContainerColor = YksRenkler.YuzeyAlt,
                unfocusedContainerColor = YksRenkler.YuzeyAlt,
                focusedTextColor = YksRenkler.YaziPrimary,
                unfocusedTextColor = YksRenkler.YaziPrimary,
                cursorColor = YksRenkler.Vurgu
            ),
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            placeholder = { Text("0", color = YksRenkler.YaziMuted, fontSize = 15.sp) }
        )
    }
}

@Composable
fun NetAlanTekli(etiket: String, deger: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            etiket.uppercase(),
            color = YksRenkler.YaziSecond,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.06.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        OutlinedTextField(
            value = deger,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YksRenkler.Vurgu,
                unfocusedBorderColor = YksRenkler.Kenar,
                focusedContainerColor = YksRenkler.YuzeyAlt,
                unfocusedContainerColor = YksRenkler.YuzeyAlt,
                focusedTextColor = YksRenkler.YaziPrimary,
                unfocusedTextColor = YksRenkler.YaziPrimary,
                cursorColor = YksRenkler.Vurgu
            ),
            textStyle = TextStyle(fontSize = 15.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}

// ─── Gradient Buton ────────────────────────────────────────────────────────────
@Composable
fun GradyanButon(
    metin: String,
    gradyan: Brush,
    metinRengi: Color = Color.White,
    yukleniyor: Boolean = false,
    aktif: Boolean = true,
    onClick: () -> Unit
) {
    val alfa by animateFloatAsState(if (aktif) 1f else 0.35f, label = "btn_alfa")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(if (aktif) Modifier.background(gradyan) else Modifier.background(YksRenkler.Yuzey))
            .then(if (aktif) Modifier.clickable(onClick = onClick) else Modifier)
            .graphicsLayer { this.alpha = alfa }
    ) {
        if (yukleniyor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = metinRengi,
                    strokeWidth = 2.dp
                )
                Text(metin, color = metinRengi, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                metin,
                color = if (aktif) metinRengi else YksRenkler.YaziMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.04.sp
            )
        }
    }
}

// ─── Sonuç Kartı ───────────────────────────────────────────────────────────────
@Composable
fun SonucKarti(metin: String) {
    Spacer(Modifier.height(14.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(Color(0x1A6C63FF), Color(0x0D00E5A0)))
            )
            .border(1.dp, Color(0x336C63FF), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(YksRenkler.VurguSoft)
                .border(1.dp, Color(0x4D6C63FF), RoundedCornerShape(14.dp))
        ) {
            Text("🏆", fontSize = 24.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                "TAHMİNİ SONUÇ",
                color = YksRenkler.YaziSecond,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.08.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                metin,
                color = YksRenkler.YaziPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Kronometre Kartı ──────────────────────────────────────────────────────────
@Composable
fun KronometreKarti(sure: String, calisiyorMu: Boolean, ders: String) {
    val pulsAlfa by rememberInfiniteTransition(label = "puls").animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulsAlfa"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0x1A6C63FF), Color(0x1400E5A0))
                )
            )
            .border(1.dp, Color(0x336C63FF), RoundedCornerShape(24.dp))
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                sure,
                style = TextStyle(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush = if (calisiyorMu)
                        Brush.linearGradient(listOf(YksRenkler.Yesil, Color(0xFF00C87A)))
                    else
                        Brush.linearGradient(listOf(YksRenkler.YaziPrimary, Color(0xFFA8A4FF))),
                    letterSpacing = (-1).sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            if (calisiyorMu) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(YksRenkler.Yesil)
                            .graphicsLayer { alpha = pulsAlfa }
                    )
                    Text(
                        "Çalışıyor — $ders",
                        color = YksRenkler.YaziSecond,
                        fontSize = 12.sp,
                        letterSpacing = 0.1.sp
                    )
                }
            } else {
                Text(
                    if (sure == "00:00:00") "Başlamaya hazır" else "Duraklatıldı",
                    color = YksRenkler.YaziSecond,
                    fontSize = 12.sp,
                    letterSpacing = 0.08.sp
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

// ─── Ders Seçim Kartı ──────────────────────────────────────────────────────────
@Composable
fun DersSecimKarti(dersler: List<String>, secili: String, onSec: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(YksRenkler.Yuzey)
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Text(
                "DERS SEÇ",
                color = YksRenkler.YaziSecond,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.08.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            val satirlar = dersler.chunked(3)
            satirlar.forEach { satir ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    satir.forEach { ders ->
                        val aktif = ders == secili
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (aktif) Modifier.background(YksRenkler.VurguSoft)
                                    else Modifier.background(YksRenkler.YuzeyAlt)
                                )
                                .border(
                                    1.dp,
                                    if (aktif) Color(0x666C63FF) else YksRenkler.Kenar,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSec(ders) }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                ders,
                                color = if (aktif) Color(0xFFA8A4FF) else YksRenkler.YaziSecond,
                                fontSize = 12.sp,
                                fontWeight = if (aktif) FontWeight.SemiBold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Boş hücreleri doldur
                    repeat(3 - satir.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

// ─── AI Koç Balonu ─────────────────────────────────────────────────────────────
@Composable
fun AiKocBubble(cevap: String, yukleniyor: Boolean) {
    Column {
        // Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VurguGradyan)
        ) {
            Text("🤖", fontSize = 20.sp)
        }
        Spacer(Modifier.height(10.dp))
        // Konuşma balonu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(YksRenkler.YuzeyAlt)
                .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .padding(16.dp)
        ) {
            if (yukleniyor) {
                val noktaAlfa by rememberInfiniteTransition(label = "nokta").animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "n"
                )
                Text(
                    "Düşünüyor...",
                    color = YksRenkler.YaziSecond.copy(alpha = noktaAlfa),
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                Text(
                    text = cevap.ifEmpty { "Merhaba! YKS hakkında ne sormak istiyorsun? Sana en iyi şekilde yardımcı olmaya hazırım." },
                    color = if (cevap.isEmpty()) YksRenkler.YaziSecond else YksRenkler.YaziPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}