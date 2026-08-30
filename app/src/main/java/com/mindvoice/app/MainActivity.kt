package com.mindvoice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindvoice.app.data.local.TokenManager
import com.mindvoice.app.ui.screens.ChatScreen
import com.mindvoice.app.ui.screens.HomeScreen
import com.mindvoice.app.ui.screens.LeaderboardScreen
import com.mindvoice.app.ui.screens.LessonDetailScreen
import com.mindvoice.app.ui.screens.LessonsScreen
import com.mindvoice.app.ui.screens.LoginScreen
import com.mindvoice.app.ui.screens.ProgressScreen
import com.mindvoice.app.ui.screens.RegisterScreen
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val token = TokenManager.getToken(this@MainActivity).first()
                    startDestination = if (token != null) "home" else "login"
                }

                if (startDestination == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = startDestination!!) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onNavigateToChat = {
                                    navController.navigate("chat")
                                },
                                onNavigateToProgress = {
                                    navController.navigate("progress")
                                },
                                onNavigateToLessons = {
                                    navController.navigate("lessons")
                                },
                                onNavigateToLeaderboard = {
                                    navController.navigate("leaderboard")
                                }
                            )
                        }
                        composable("chat") {
                            ChatScreen()
                        }
                        composable("progress") {
                            ProgressScreen()
                        }
                        composable("lessons") {
                            LessonsScreen(
                                onLessonClick = { lessonId ->
                                    navController.navigate("lessonDetail/$lessonId")
                                }
                            )
                        }
                        composable(
                            "lessonDetail/{lessonId}",
                            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                            LessonDetailScreen(
                                lessonId = lessonId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("leaderboard") {
                            LeaderboardScreen()
                        }
                    }
                }
            }
        }
    }
}