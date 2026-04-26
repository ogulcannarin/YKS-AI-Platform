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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    val Purple600 = Color(0xFF6C4EE3)
    val Purple100 = Color(0xFFEEEBFD)
    val Purple50 = Color(0xFFF5F3FF)
    val Slate900 = Color(0xFF1A1A2E)
    val Slate700 = Color(0xFF374151)
    val Slate400 = Color(0xFF9CA3AF)
    val Slate100 = Color(0xFFF1F0F9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Soru Çözücü",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGeriDon) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Slate700)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                windowInsets = WindowInsets(0)
            )
        },
        containerColor = Slate100
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Hero upload card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Upload zone
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (imageUri != null) Purple50 else Slate100)
                            .border(
                                width = 1.5.dp,
                                brush = if (imageUri != null)
                                    Brush.horizontalGradient(listOf(Purple600, Color(0xFF9C6FE4)))
                                else
                                    Brush.horizontalGradient(listOf(Slate400, Slate400)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (imageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Purple100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = Purple600,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    "Fotoğraf hazır",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Purple600
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Slate100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    "Soru fotoğrafını buraya yükle",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Slate700
                                )
                                Text(
                                    "JPG, PNG desteklenir",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }

                    // Fotoğraf seç/değiştir butonu
                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Purple600
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Purple600, Color(0xFF9C6FE4)))
                        )
                    ) {
                        Text(
                            if (imageUri == null) "Fotoğraf Seç" else "Fotoğrafı Değiştir",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Analiz Et butonu
            Button(
                onClick = {
                    val base64 = imageUri?.let { uriToBase64(context, it) }
                    if (base64 != null) {
                        isYukleniyor = true
                        apiService.yksSoruCoz(SoruCozRequest(image_base64 = base64))
                            .enqueue(object : Callback<SoruCozResponse> {
                                override fun onResponse(
                                    call: Call<SoruCozResponse>,
                                    response: Response<SoruCozResponse>
                                ) {
                                    isYukleniyor = false
                                    if (response.isSuccessful) {
                                        aiCevabi = response.body()?.cozum ?: "Cevap alınamadı."
                                    }
                                }
                                override fun onFailure(call: Call<SoruCozResponse>, t: Throwable) {
                                    isYukleniyor = false
                                    aiCevabi = "Hata: ${t.message}"
                                }
                            })
                    } else {
                        Toast.makeText(context, "Önce bir fotoğraf seçmelisin!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isYukleniyor && imageUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple600,
                    contentColor = Color.White,
                    disabledContainerColor = Purple100,
                    disabledContentColor = Color(0xFFB39FEC)
                )
            ) {
                if (isYukleniyor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Analiz ediliyor...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Soruyu Çöz",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Cevap kartı — sadece cevap varsa veya yükleniyorsa göster
            AnimatedVisibility(
                visible = aiCevabi.isNotEmpty() || isYukleniyor,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Purple600, Color(0xFF9C6FE4))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                "AI Çözümü",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Slate900
                            )
                        }

                        Divider(color = Slate100, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isYukleniyor) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(4) { index ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (index == 3) 0.6f else 1f)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(Slate100.copy(alpha = shimmerAlpha))
                                    )
                                }
                            }
                        } else {
                            Text(
                                aiCevabi,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = Slate700
                            )
                        }
                    }
                }
            }

            // Boş durum placeholder — hiçbir şey yok ve yüklenmiyorsa
            if (aiCevabi.isEmpty() && !isYukleniyor) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Text(
                            "Sorunun fotoğrafını yükle",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Slate900,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "AI adım adım çözüm üretsin",
                            fontSize = 13.sp,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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