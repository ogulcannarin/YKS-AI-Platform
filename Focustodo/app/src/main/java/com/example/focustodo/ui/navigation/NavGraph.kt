package com.example.focustodo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.focustodo.repository.AuthRepository
import com.example.focustodo.ui.screens.auth.AuthViewModel
import com.example.focustodo.ui.screens.auth.LoginScreen
import com.example.focustodo.ui.screens.auth.RegisterScreen
import com.example.focustodo.ui.screens.home.HomeScreen

@Composable
fun NavGraph(authRepository: AuthRepository) {
    val navController = rememberNavController()

    // Scoped to activity/navgraph level, so it persists across screen destinations.
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(authRepository)
    )

    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) "login" else "home"
    ) {
        composable("login") {
            // Because startDestination determines initial load, we dynamically navigate
            // based on state if it changes outside Compose (e.g., successful login)
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            LaunchedEffect(currentUser) {
                if (currentUser != null) {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            LaunchedEffect(currentUser) {
                if (currentUser == null) {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            HomeScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
