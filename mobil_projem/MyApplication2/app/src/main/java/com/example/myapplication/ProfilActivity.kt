package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.network.KullaniciKayitRequest
import com.example.myapplication.network.SupabaseApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = YksRenkler.Arka)) {
                Surface(modifier = Modifier.fillMaxSize(), color = YksRenkler.Arka) {
                    ProfilSayfasi(userEmail, onGeriDon = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilSayfasi(userEmail: String, onGeriDon: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("profil_prefs", android.content.Context.MODE_PRIVATE)

    var profilKullaniciAdi by remember { mutableStateOf("Yükleniyor...") }
    var profilFotoUri by remember { mutableStateOf(sharedPref.getString("profil_foto_$userEmail", null)) }

    val totalStudyMinutes = sharedPref.getInt("total_study_minutes_$userEmail", 0)
    val studyHours = totalStudyMinutes / 60
    val studyMins = totalStudyMinutes % 60
    val (calismaDeger, calismaBirim) = if (studyHours > 0) {
        Pair("$studyHours", "Saat $studyMins Dk")
    } else {
        Pair("$studyMins", "Dakika")
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profilFotoUri = uri.toString()
            sharedPref.edit().putString("profil_foto_$userEmail", uri.toString()).apply()
        }
    }

    val supabaseService = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApiService::class.java)
    }

    LaunchedEffect(userEmail) {
        if (userEmail.isNotEmpty()) {
            supabaseService.getKullanici(SupabaseConfig.SUPABASE_KEY, "eq.$userEmail").enqueue(object : Callback<List<KullaniciKayitRequest>> {
                override fun onResponse(call: Call<List<KullaniciKayitRequest>>, response: Response<List<KullaniciKayitRequest>>) {
                    profilKullaniciAdi = if (response.isSuccessful) {
                        response.body()?.firstOrNull()?.kullanici_adi ?: "Bilinmiyor"
                    } else "Bulunamadı"
                }
                override fun onFailure(call: Call<List<KullaniciKayitRequest>>, t: Throwable) {
                    profilKullaniciAdi = "Hata"
                }
            })
        } else {
            profilKullaniciAdi = "Misafir"
        }
    }

    // Dönen gradient ring animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "ring_rotate"
    )

    Scaffold(
        bottomBar = {
            ModernBottomNav(
                secili = AltSekme.PROFIL,
                onSec = { sekme ->
                    if (sekme != AltSekme.PROFIL) {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("USER_EMAIL", userEmail)
                        intent.putExtra("TARGET_TAB", sekme.name)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        (context as? android.app.Activity)?.finish()
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
            // ─── Gradient Banner ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Banner arka planı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(ProfilBannerGradyan)
                ) {
                    // Dekoratif desen
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val circleColor = YksRenkler.Vurgu.copy(alpha = 0.08f)
                        drawCircle(color = circleColor, radius = size.width * 0.4f, center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, -size.height * 0.2f))
                        drawCircle(color = circleColor, radius = size.width * 0.3f, center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.8f))
                    }
                    // Geri butonu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 52.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onGeriDon,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Text("Profilim", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.width(40.dp))
                    }
                }

                // Profil Fotoğrafı (banner üzerine taşıyor)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dönen gradient ring
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .rotate(ringRotation)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        YksRenkler.Vurgu,
                                        YksRenkler.Yesil,
                                        YksRenkler.TabAktif2,
                                        YksRenkler.Vurgu
                                    )
                                )
                            )
                    )
                    // İç beyaz boşluk
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(YksRenkler.Arka)
                    )
                    // Fotoğraf alanı
                    Box(
                        modifier = Modifier
                            .size(98.dp)
                            .clip(CircleShape)
                            .background(YksRenkler.YuzeyAlt)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilFotoUri != null) {
                            AsyncImage(
                                model = profilFotoUri,
                                contentDescription = "Profil Fotoğrafı",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.height(2.dp))
                                Text("Ekle", fontSize = 9.sp, color = YksRenkler.YaziSecond, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // İsim ve Email
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Text(profilKullaniciAdi, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (userEmail.isNotEmpty()) userEmail else "misafir@uygulama.com",
                    color = YksRenkler.YaziMuted,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // ─── İstatistikler ────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "İSTATİSTİKLER",
                    color = YksRenkler.YaziSecond,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfilStatKarti(
                        baslik = "Çalışma",
                        deger = calismaDeger,
                        birim = calismaBirim,
                        icon = Icons.Rounded.Timer,
                        renk = YksRenkler.Vurgu,
                        modifier = Modifier.weight(1f)
                    )
                    ProfilStatKarti(
                        baslik = "Başarı",
                        deger = "🎯",
                        birim = "Devam Et",
                        icon = Icons.Rounded.TrendingUp,
                        renk = YksRenkler.Yesil,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── Menü Öğeleri ─────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "AYARLAR",
                    color = YksRenkler.YaziSecond,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
                ProfilAyarOgesi(icon = Icons.Rounded.Settings, baslik = "Hesap Ayarları")
                Spacer(Modifier.height(8.dp))
                ProfilAyarOgesi(icon = Icons.Rounded.Notifications, baslik = "Bildirim Tercihleri")
                Spacer(Modifier.height(8.dp))
                ProfilAyarOgesi(icon = Icons.Rounded.Star, baslik = "Premium'a Geç", vurgulu = true)
                Spacer(Modifier.height(8.dp))
                ProfilAyarOgesi(icon = Icons.Rounded.Help, baslik = "Yardım ve Destek")
            }

            Spacer(Modifier.height(24.dp))

            // ─── Çıkış Yap ────────────────────────────────────────────────────
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YksRenkler.Kirmizi),
                    border = BorderStroke(1.dp, YksRenkler.Kirmizi.copy(alpha = 0.4f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Çıkış Yap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfilStatKarti(
    baslik: String,
    deger: String,
    birim: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    renk: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(YksRenkler.Yuzey)
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(renk.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = renk, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(deger, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                if (birim.length < 6) {
                    Spacer(Modifier.width(4.dp))
                    Text(birim, color = YksRenkler.YaziMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            if (birim.length >= 6) {
                Text(birim, color = YksRenkler.YaziMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(baslik, color = YksRenkler.YaziSecond, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfilAyarOgesi(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    baslik: String,
    vurgulu: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (vurgulu) YksRenkler.VurguSoft else YksRenkler.Yuzey)
            .border(
                1.dp,
                if (vurgulu) YksRenkler.Vurgu.copy(alpha = 0.35f) else YksRenkler.Kenar,
                RoundedCornerShape(16.dp)
            )
            .clickable { /* Tıklama eylemi */ }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (vurgulu) YksRenkler.VurguGlow else YksRenkler.YuzeyAlt),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (vurgulu) YksRenkler.Vurgu else YksRenkler.YaziPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                baslik,
                color = if (vurgulu) YksRenkler.Vurgu else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = YksRenkler.YaziMuted)
    }
}
