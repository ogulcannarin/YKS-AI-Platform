package com.example.focustodo.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var targetRank by remember { mutableStateOf("") }

    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Yeni Hesap Oluştur", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; viewModel.clearError() },
            label = { Text("Ad Soyad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            label = { Text("E-posta") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = targetRank,
            onValueChange = { targetRank = it; viewModel.clearError() },
            label = { Text("Hedef Sıralama (Opsiyonel)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val rank = targetRank.toIntOrNull()
                viewModel.register(email, fullName, password, rank)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Kayıt Ol")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Zaten hesabın var mı? Giriş Yap")
        }
    }
}
