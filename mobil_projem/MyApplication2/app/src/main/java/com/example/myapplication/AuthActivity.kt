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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.network.AuthRequest
import com.example.myapplication.network.AuthResponse
import com.example.myapplication.network.IdTokenRequest
import com.example.myapplication.network.SupabaseApiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = YksRenkler.Arka)) {
                AuthScreen { 
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var isLoginMode by remember { mutableStateOf(true) }
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.SUPABASE_URL + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(SupabaseApiService::class.java) }

    // Google Sign-In Ayarları
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SupabaseConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Google Sign-In Sonuç Dinleyici
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
                    // Supabase'e ID Token'ı gönder
                    apiService.loginWithIdToken(
                        apiKey = SupabaseConfig.SUPABASE_KEY,
                        request = IdTokenRequest(id_token = idToken)
                    ).enqueue(object : Callback<AuthResponse> {
                        override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                            isLoading = false
                            if (response.isSuccessful && response.body()?.access_token != null) {
                                Toast.makeText(context, "Google ile Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Supabase Google yetkilendirmesi başarısız", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Google Token alınamadı", Toast.LENGTH_SHORT).show()
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
            apiService.login(SupabaseConfig.SUPABASE_KEY, request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    isLoading = false
                    if (response.isSuccessful && response.body()?.access_token != null) {
                        Toast.makeText(context, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
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
            apiService.signup(SupabaseConfig.SUPABASE_KEY, request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.access_token
                        
                        if (token.isNullOrEmpty()) {
                            isLoading = false
                            Toast.makeText(context, "Kayıt Başarılı! Lütfen e-postanızı onaylayın.", Toast.LENGTH_LONG).show()
                            isLoginMode = true
                            return
                        }

                        val profilRequest = com.example.myapplication.network.KullaniciKayitRequest(email, username)
                        apiService.kayitEkle(
                            apiKey = SupabaseConfig.SUPABASE_KEY,
                            authHeader = "Bearer $token",
                            request = profilRequest
                        ).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                isLoading = false
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Kayıt Başarılı! Lütfen giriş yapın.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Kullanıcı tablosuna kayıt başarısız: ${response.code()}", Toast.LENGTH_LONG).show()
                                }
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
                        Toast.makeText(context, "Kayıt Hatası (${response.code()}): Email kullanılıyor veya şifre yetersiz olabilir.", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YksRenkler.Arka)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Text(
                text = "YKS Asistan",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(brush = BaslikGradyan)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isLoginMode) "Hesabınıza giriş yapın" else "Yeni bir hesap oluşturun",
                color = YksRenkler.YaziSecond,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(YksRenkler.YuzeyAlt)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLoginMode) YksRenkler.Kenar else Color.Transparent)
                        .clickable { isLoginMode = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Giriş Yap", color = if (isLoginMode) Color.White else YksRenkler.YaziMuted, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isLoginMode) YksRenkler.Kenar else Color.Transparent)
                        .clickable { isLoginMode = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Kayıt Ol", color = if (!isLoginMode) Color.White else YksRenkler.YaziMuted, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = !isLoginMode) {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        label = { Text("Kullanıcı Adı", color = YksRenkler.YaziSecond) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YksRenkler.Vurgu,
                            unfocusedBorderColor = YksRenkler.Kenar,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                label = { Text("Email", color = YksRenkler.YaziSecond) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YksRenkler.Vurgu,
                    unfocusedBorderColor = YksRenkler.Kenar,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                label = { Text("Şifre", color = YksRenkler.YaziSecond) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YksRenkler.Vurgu,
                    unfocusedBorderColor = YksRenkler.Kenar,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            GradyanButon(
                metin = if (isLoginMode) "Giriş Yap" else "Kayıt Ol",
                gradyan = VurguGradyan,
                yukleniyor = isLoading
            ) {
                handleAuth()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = YksRenkler.Kenar)
                Text("veya", modifier = Modifier.padding(horizontal = 16.dp), color = YksRenkler.YaziMuted)
                Divider(modifier = Modifier.weight(1f), color = YksRenkler.Kenar)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    googleAuthLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = YksRenkler.YuzeyAlt),
                border = BorderStroke(1.dp, YksRenkler.Kenar)
            ) {
                Text("Google ile Devam Et", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
