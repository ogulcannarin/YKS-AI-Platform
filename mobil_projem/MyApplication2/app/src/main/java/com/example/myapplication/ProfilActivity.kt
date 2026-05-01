package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
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
        Pair("$studyMins", "Dk")
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
                    if (response.isSuccessful) {
                        profilKullaniciAdi = response.body()?.firstOrNull()?.kullanici_adi ?: "Bilinmiyor"
                    } else {
                        profilKullaniciAdi = "Bulunamadı"
                    }
                }
                override fun onFailure(call: Call<List<KullaniciKayitRequest>>, t: Throwable) {
                    profilKullaniciAdi = "Hata"
                }
            })
        } else {
            profilKullaniciAdi = "Misafir"
        }
    }

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
            // Başlık Alanı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onGeriDon, modifier = Modifier.size(40.dp).background(YksRenkler.YuzeyAlt, CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Text("Profilim", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            // Profil Foto ve Bilgi
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(YksRenkler.YuzeyAlt)
                        .border(3.dp, YksRenkler.Vurgu, CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profilFotoUri != null) {
                        AsyncImage(
                            model = profilFotoUri,
                            contentDescription = "Profil Fotoğrafı",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👤", fontSize = 40.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Fotoğraf Ekle", fontSize = 10.sp, color = YksRenkler.YaziSecond, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(profilKullaniciAdi, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(if (userEmail.isNotEmpty()) userEmail else "misafir@uygulama.com", color = YksRenkler.YaziMuted, fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))

            // İstatistikler Kartı
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("İSTATİSTİKLER (Özet)", color = YksRenkler.YaziSecond, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
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
                        baslik = "Çözülen",
                        deger = "150",
                        birim = "Soru",
                        icon = Icons.Rounded.TaskAlt,
                        renk = YksRenkler.Yesil,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Menü Öğeleri
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ProfilAyarOgesi(icon = Icons.Rounded.Settings, baslik = "Hesap Ayarları")
                ProfilAyarOgesi(icon = Icons.Rounded.Notifications, baslik = "Bildirim Tercihleri")
                ProfilAyarOgesi(icon = Icons.Rounded.Star, baslik = "Premium'a Geç", vurgulu = true)
                ProfilAyarOgesi(icon = Icons.Rounded.Help, baslik = "Yardım ve Destek")
            }

            Spacer(Modifier.height(32.dp))
            
            // Çıkış Yap Butonu
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
                    border = BorderStroke(1.dp, YksRenkler.Kirmizi.copy(alpha = 0.5f))
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
fun ProfilStatKarti(baslik: String, deger: String, birim: String, icon: androidx.compose.ui.graphics.vector.ImageVector, renk: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(YksRenkler.Yuzey)
            .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = renk, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(deger, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(4.dp))
                Text(birim, color = YksRenkler.YaziMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(baslik, color = YksRenkler.YaziSecond, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfilAyarOgesi(icon: androidx.compose.ui.graphics.vector.ImageVector, baslik: String, vurgulu: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (vurgulu) YksRenkler.VurguSoft else YksRenkler.Yuzey)
            .border(1.dp, if (vurgulu) YksRenkler.Vurgu.copy(alpha=0.3f) else YksRenkler.Kenar, RoundedCornerShape(16.dp))
            .clickable { /* Tıklama eylemi */ }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (vurgulu) YksRenkler.Vurgu.copy(alpha=0.2f) else YksRenkler.YuzeyAlt),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (vurgulu) YksRenkler.Vurgu else YksRenkler.YaziPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(baslik, color = if (vurgulu) YksRenkler.Vurgu else Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = YksRenkler.YaziMuted)
    }
}
