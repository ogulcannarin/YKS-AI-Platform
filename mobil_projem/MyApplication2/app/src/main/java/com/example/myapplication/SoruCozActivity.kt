package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.network.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SoruCozActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Artık MyApplicationTheme YksRenkler kullanıyor — tutarlı görünüm
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = YksRenkler.Arka) {
                    SoruCozEkrani { finish() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoruCozEkrani(onGeriDon: () -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var aiCevabi by remember { mutableStateOf("") }
    var isYukleniyor by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val okHttpClient = remember {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)   // Görsel analiz uzun sürebilir
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(YksApiService::class.java) }

    // Shimmer animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Soru Çözücü",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onGeriDon,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(YksRenkler.YuzeyAlt, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YksRenkler.Arka),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = YksRenkler.Arka
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ─── Fotoğraf Yükleme Alanı ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (imageUri != null) 280.dp else 220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (imageUri != null) YksRenkler.YuzeyAlt else YksRenkler.Yuzey)
                    .border(
                        width = 1.5.dp,
                        brush = if (imageUri != null) VurguGradyan
                                else androidx.compose.ui.graphics.SolidColor(YksRenkler.Kenar),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // Gerçek fotoğraf önizlemesi (Coil)
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Seçilen Soru Fotoğrafı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    )
                    // Üste değiştir overlay'i
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Değiştirmek için dokun", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(YksRenkler.YuzeyAlt)
                                .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(34.dp))
                        }
                        Text("Soru fotoğrafını buraya yükle", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                        Text("JPG, PNG formatları desteklenir", fontSize = 13.sp, color = YksRenkler.YaziSecond)
                    }
                }
            }

            // ─── Analiz Butonu ────────────────────────────────────────────────
            GradyanButon(
                metin = if (isYukleniyor) "Analiz Ediliyor..." else "✨  Soruyu Çöz",
                gradyan = VurguGradyan,
                yukleniyor = isYukleniyor
            ) {
                val base64 = imageUri?.let { uriToBase64(context, it) }
                if (base64 != null) {
                    isYukleniyor = true
                    apiService.yksSoruCoz(SoruCozRequest(image_base64 = base64))
                        .enqueue(object : Callback<SoruCozResponse> {
                            override fun onResponse(call: Call<SoruCozResponse>, response: Response<SoruCozResponse>) {
                                isYukleniyor = false
                                aiCevabi = if (response.isSuccessful) {
                                    response.body()?.cozum ?: "Cevap alınamadı."
                                } else {
                                    "Bağlantı hatası: ${response.code()}"
                                }
                            }
                            override fun onFailure(call: Call<SoruCozResponse>, t: Throwable) {
                                isYukleniyor = false
                                aiCevabi = "Hata: ${t.message}"
                            }
                        })
                } else {
                    Toast.makeText(context, "Lütfen önce sorunun fotoğrafını yükleyin!", Toast.LENGTH_SHORT).show()
                }
            }

            // ─── Çözüm Kartı ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = aiCevabi.isNotEmpty() || isYukleniyor,
                enter = fadeIn(tween(300)) + expandVertically(tween(300))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = YksRenkler.YuzeyAlt),
                    border = BorderStroke(1.dp, YksRenkler.Kenar)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(YksRenkler.VurguSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Yapay Zeka Çözümü", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.White)
                                Text("Adım adım açıklama", color = YksRenkler.YaziMuted, fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = YksRenkler.Kenar)
                        Spacer(Modifier.height(16.dp))

                        if (isYukleniyor) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                repeat(5) { index ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (index == 4) 0.55f else 1f)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(YksRenkler.Yuzey.copy(alpha = shimmerAlpha))
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = aiCevabi,
                                fontSize = 15.sp,
                                lineHeight = 26.sp,
                                color = YksRenkler.YaziPrimary
                            )
                        }
                    }
                }
            }

            // ─── Boş Durum ────────────────────────────────────────────────────
            if (aiCevabi.isEmpty() && !isYukleniyor) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(YksRenkler.YuzeyAlt),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(30.dp))
                        }
                        Text(
                            "Soru fotoğrafını yükle,\nAI adım adım çözüm üretsin.",
                            color = YksRenkler.YaziSecond,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}