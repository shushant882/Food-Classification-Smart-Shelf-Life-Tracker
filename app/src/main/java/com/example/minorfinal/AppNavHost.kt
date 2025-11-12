package com.example.minorfinal


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


/**
 * The main navigation graph for the application.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.OnBoarding.route // Start with onboarding
    ) {
        // Onboarding Screen Route
        composable(route = Screen.OnBoarding.route) {
            OnBoardingScreen(navController = navController)
        }

        // Home Screen Route
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Classifier Screen Route
        composable(route = Screen.Classifier.route) {
            ClassifierScreen(navController = navController)
        }
    }
}