package com.example.donasiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.donasiapp.ui.DonasiScreen
import com.example.donasiapp.ui.HistoryScreen
import com.example.donasiapp.ui.theme.DonasiAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DonasiAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            Dashboard(navController)
        }
        composable(
            "donasi/{kategori}",
            arguments = listOf(navArgument("kategori") { type = NavType.StringType })
        ) { backStackEntry ->
            val kategori = backStackEntry.arguments?.getString("kategori") ?: "Umum"
            DonasiScreen(navController = navController, kategori = kategori)
        }
        composable("history") {
            HistoryScreen(navController = navController)
        }
    }
}

