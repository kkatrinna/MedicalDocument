package com.example.medicaldocuments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medicaldocuments.ui.add.AddDocumentScreen
import com.example.medicaldocuments.ui.detail.DetailScreen
import com.example.medicaldocuments.ui.main.MainScreen
import com.example.medicaldocuments.ui.search.SearchScreen
import com.example.medicaldocuments.ui.theme.AppTheme
import com.example.medicaldocuments.ui.theme.ThemeManager
import com.example.medicaldocuments.ui.viewer.DocumentViewerScreen
import dagger.hilt.android.AndroidEntryPoint
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        ThemeManager.initialize(this)

        setContent {
            val currentTheme by ThemeManager.currentTheme.collectAsState()

            val isDark = when (currentTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            key(isDark) {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = if (isDark)
                            androidx.compose.ui.graphics.Color(0xFF121212)
                        else
                            androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                        darkIcons = !isDark
                    )
                }

                MaterialTheme(
                    colorScheme = if (isDark)
                        androidx.compose.material3.darkColorScheme()
                    else
                        androidx.compose.material3.lightColorScheme()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MedDocApp()
                    }
                }
            }
        }
    }
}

@Composable
fun MedDocApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController = navController)
        }

        composable(
            "add/{category}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "Анализы"
            AddDocumentScreen(
                navController = navController,
                initialCategory = category
            )
        }

        composable("add") {
            AddDocumentScreen(
                navController = navController,
                initialCategory = "Анализы"
            )
        }

        composable(
            "detail/{documentId}",
            arguments = listOf(
                navArgument("documentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: 0L
            DetailScreen(
                navController = navController,
                documentId = documentId
            )
        }

        composable(
            "viewer/{documentId}",
            arguments = listOf(
                navArgument("documentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: 0L
            DocumentViewerScreen(
                navController = navController,
                documentId = documentId
            )
        }

        composable("search") {
            SearchScreen(navController = navController)
        }
    }
}