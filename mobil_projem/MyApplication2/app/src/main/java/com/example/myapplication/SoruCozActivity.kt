package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.network.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SoruCozActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(YksApiService::class.java) }

    // Shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmerAlpha"
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
                    IconButton(onClick = onGeriDon, modifier = Modifier.padding(start = 8.dp).size(40.dp).background(YksRenkler.YuzeyAlt, CircleShape)) {
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
            
            // Yükleme Alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (imageUri != null) YksRenkler.VurguSoft else YksRenkler.Yuzey)
                    .border(
                        width = 1.dp,
                        brush = if (imageUri != null) VurguGradyan else androidx.compose.ui.graphics.SolidColor(YksRenkler.Kenar),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (imageUri != null) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(YksRenkler.Vurgu.copy(alpha=0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(36.dp))
                        }
                        Text("Fotoğraf Hazır", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = YksRenkler.Vurgu)
                        Text("Değiştirmek için dokun", fontSize = 12.sp, color = YksRenkler.YaziMuted)
                    } else {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(YksRenkler.YuzeyAlt),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(32.dp))
                        }
                        Text("Soru fotoğrafını buraya yükle", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                        Text("JPG, PNG formatları desteklenir", fontSize = 13.sp, color = YksRenkler.YaziSecond)
                    }
                }
            }

            // Analiz Et Butonu
            GradyanButon(
                metin = if (isYukleniyor) "Analiz Ediliyor..." else "✨ Soruyu Çöz",
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
                                if (response.isSuccessful) {
                                    aiCevabi = response.body()?.cozum ?: "Cevap alınamadı."
                                } else {
                                    aiCevabi = "Bağlantı hatası: ${response.code()}"
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

            // Çözüm Kartı
            AnimatedVisibility(
                visible = aiCevabi.isNotEmpty() || isYukleniyor,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = YksRenkler.YuzeyAlt),
                    border = BorderStroke(1.dp, YksRenkler.Kenar)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = YksRenkler.Vurgu, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Yapay Zeka Çözümü", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Divider(color = YksRenkler.Kenar)
                        Spacer(Modifier.height(16.dp))

                        if (isYukleniyor) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(5) { index ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (index == 4) 0.5f else 1f)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(YksRenkler.Yuzey.copy(alpha = shimmerAlpha))
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = aiCevabi,
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = YksRenkler.YaziPrimary
                            )
                        }
                    }
                }
            }

            // Placeholder (Boş Durum)
            if (aiCevabi.isEmpty() && !isYukleniyor) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(48.dp))
                        Text(
                            "Soru fotoğrafını yükle,\nAI adım adım çözüm üretsin.",
                            color = YksRenkler.YaziSecond,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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