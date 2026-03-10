package com.example.focustodo.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focustodo.ui.screens.auth.AuthViewModel

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var studyDuration by remember { mutableStateOf("") }
    var solvedQuestions by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Hoşgeldin, ${currentUser?.fullName ?: "Öğrenci"}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hedef Sıralama: ${currentUser?.targetRank ?: "Belirtilmedi"}",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Günlük Çalışma Süresi Girişi",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = studyDuration,
                    onValueChange = { studyDuration = it },
                    label = { Text("Çalışma Süresi (Dakika)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = solvedQuestions,
                    onValueChange = { solvedQuestions = it },
                    label = { Text("Çözülen Soru Sayısı") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // Dummy click listener to showcase entering data
                        studyDuration = ""
                        solvedQuestions = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kaydet")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çıkış Yap")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
