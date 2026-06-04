package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.network.AuthRequest
import com.example.myapplication.network.AuthResponse
import com.example.myapplication.network.IdTokenRequest
import com.example.myapplication.network.KullaniciKayitRequest
import com.example.myapplication.network.ResetPasswordRequest
import com.example.myapplication.network.SupabaseApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService: SupabaseApiService by lazy { retrofit.create(SupabaseApiService::class.java) }
}

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = YksRenkler.Arka)) {
                AuthScreen { email ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Composable
fun AuthScreen(onLoginSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SupabaseConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(context, gso) { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    isLoading = true
                    RetrofitClient.apiService.loginWithIdToken(
                        apiKey = SupabaseConfig.SUPABASE_KEY,
                        request = IdTokenRequest(id_token = idToken)
                    ).enqueue(object : Callback<AuthResponse> {
                        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                            isLoading = false
                            if (response.isSuccessful && response.body()?.access_token != null) {
                                Toast.makeText(context, "Google ile Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                                val googleEmail = account?.email ?: ""
                                val googleName = account?.displayName ?: googleEmail.substringBefore("@")
                                val token = response.body()?.access_token
                                RetrofitClient.apiService.kayitEkle(
                                    apiKey = SupabaseConfig.SUPABASE_KEY,
                                    authHeader = "Bearer $token",
                                    request = KullaniciKayitRequest(googleEmail, googleName)
                                ).enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, res: Response<Void>) { onLoginSuccess(googleEmail) }
                                    override fun onFailure(call: Call<Void>, t: Throwable) { onLoginSuccess(googleEmail) }
                                })
                            } else {
                                Toast.makeText(context, "Supabase Google yetkilendirmesi başarısız", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Giriş Hatası: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleAuth() {
        if (email.isBlank() || password.isBlank() || (!isLoginMode && username.isBlank())) {
            Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }
        isLoading = true
        val request = AuthRequest(email, password)
        if (isLoginMode) {
            RetrofitClient.apiService.login(SupabaseConfig.SUPABASE_KEY, request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    isLoading = false
                    if (response.isSuccessful && response.body()?.access_token != null) {
                        Toast.makeText(context, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(email)
                    } else {
                        Toast.makeText(context, "Hata: Bilgileri kontrol edin (${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            RetrofitClient.apiService.signup(SupabaseConfig.SUPABASE_KEY, request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.access_token
                        if (token.isNullOrEmpty()) {
                            isLoading = false
                            Toast.makeText(context, "Kayıt Başarılı! Lütfen e-postanızı onaylayın.", Toast.LENGTH_LONG).show()
                            isLoginMode = true
                            return
                        }
                        RetrofitClient.apiService.kayitEkle(
                            apiKey = SupabaseConfig.SUPABASE_KEY,
                            authHeader = "Bearer $token",
                            request = KullaniciKayitRequest(email, username)
                        ).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                isLoading = false
                                Toast.makeText(context, "Kayıt Başarılı! Lütfen giriş yapın.", Toast.LENGTH_SHORT).show()
                                isLoginMode = true
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                isLoading = false
                                Toast.makeText(context, "Profil oluşturulamadı: ${t.message}", Toast.LENGTH_LONG).show()
                                isLoginMode = true
                            }
                        })
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Kayıt Hatası (${response.code()}): Email kullanılıyor veya şifre yetersiz.", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Arka plan blob animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val blob1Scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "blob1"
    )
    val blob2Scale by infiniteTransition.animateFloat(
        initialValue = 1.05f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(7000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "blob2"
    )

    // Logo animasyonu
    var logoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { logoVisible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YksRenkler.Arka)
    ) {
        // Animasyonlu arka plan blob'ları
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Mor blob - sağ üst
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(YksRenkler.Vurgu.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.6f * blob1Scale
                ),
                center = Offset(size.width * 0.85f, size.height * 0.15f),
                radius = size.width * 0.6f * blob1Scale
            )
            // Yeşil blob - sol alt
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(YksRenkler.Yesil.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.8f),
                    radius = size.width * 0.5f * blob2Scale
                ),
                center = Offset(size.width * 0.15f, size.height * 0.8f),
                radius = size.width * 0.5f * blob2Scale
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo & Başlık
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(VurguGradyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.School,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "YKS Asistan",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = androidx.compose.ui.text.TextStyle(brush = BaslikGradyan)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Yapay Zeka Destekli Çalışma Koçun",
                        color = YksRenkler.YaziSecond,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Giriş/Kayıt Tab Switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(YksRenkler.Yuzey)
                    .border(1.dp, YksRenkler.Kenar, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf(true to "Giriş Yap", false to "Kayıt Ol").forEach { (mode, label) ->
                        val aktif = isLoginMode == mode
                        val bgColor by animateColorAsState(
                            targetValue = if (aktif) YksRenkler.Vurgu else Color.Transparent,
                            animationSpec = tween(220),
                            label = "tab_$label"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .clickable { isLoginMode = mode }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (aktif) Color.White else YksRenkler.YaziMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Form Alanları
            AnimatedVisibility(visible = !isLoginMode, enter = fadeIn() + expandVertically()) {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        label = { Text("Kullanıcı Adı", color = YksRenkler.YaziSecond) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YksRenkler.Vurgu,
                            unfocusedBorderColor = YksRenkler.Kenar,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = YksRenkler.Vurgu
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                label = { Text("E-posta", color = YksRenkler.YaziSecond) },
                leadingIcon = {
                    Icon(Icons.Rounded.Email, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(20.dp))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YksRenkler.Vurgu,
                    unfocusedBorderColor = YksRenkler.Kenar,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YksRenkler.Vurgu
                )
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                label = { Text("Şifre", color = YksRenkler.YaziSecond) },
                leadingIcon = {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = YksRenkler.YaziMuted, modifier = Modifier.size(20.dp))
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YksRenkler.Vurgu,
                    unfocusedBorderColor = YksRenkler.Kenar,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = YksRenkler.Vurgu
                )
            )

            AnimatedVisibility(visible = isLoginMode) {
                Text(
                    text = "Şifremi Unuttum",
                    color = YksRenkler.Vurgu,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Lütfen önce e-posta adresinizi girin.", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                RetrofitClient.apiService.resetPassword(
                                    SupabaseConfig.SUPABASE_KEY,
                                    ResetPasswordRequest(email)
                                ).enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        isLoading = false
                                        if (response.isSuccessful) Toast.makeText(context, "Şifre sıfırlama bağlantısı gönderildi!", Toast.LENGTH_LONG).show()
                                        else Toast.makeText(context, "Hata: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                    override fun onFailure(call: Call<Void>, t: Throwable) { isLoading = false }
                                })
                            }
                        },
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(28.dp))

            GradyanButon(
                metin = if (isLoginMode) "Giriş Yap" else "Kayıt Ol",
                gradyan = VurguGradyan,
                yukleniyor = isLoading
            ) { handleAuth() }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = YksRenkler.Kenar)
                Text("  veya  ", color = YksRenkler.YaziMuted, fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = YksRenkler.Kenar)
            }

            Spacer(Modifier.height(20.dp))

            // Google Butonu
            OutlinedButton(
                onClick = { googleAuthLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = YksRenkler.Yuzey),
                border = BorderStroke(1.dp, YksRenkler.Kenar)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", color = YksRenkler.Vurgu, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(10.dp))
                    Text("Google ile Devam Et", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
