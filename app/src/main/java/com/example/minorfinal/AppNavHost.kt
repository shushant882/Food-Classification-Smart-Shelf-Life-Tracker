package com.example.minorfinal


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minorfinal.ui.screens.ClassifierScreen

//import com.example.minorfinal.ui.screens.ClassifierScreen


/**
 * The main navigation graph for the application.
 * This has been updated to your requested flow.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.OnBoarding.route // 1. Start with Onboarding
    ) {
        // Onboarding Screen Route
        composable(route = Screen.OnBoarding.route) {
            // Your existing OnBoardingScreen
            OnBoardingScreen(navController = navController)
        }

        // Welcome Screen Route
        composable(route = Screen.Welcome.route) {
            // Our new WelcomeScreen
            WelcomeScreen(
                navController = navController,
                onLoginClick = {
                    // 2. Welcome -> Login
                    navController.navigate(Screen.Login.route)
                },
                onSignupClick = {
                    // 3. Welcome -> Signup
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        // Login Screen Route
        composable(route = Screen.Login.route) {
            // Our new LoginScreen
            LoginScreen(
                navController = navController,
                onLoginClick = {
                    // 4. Login -> Home (and clear the whole back stack)
                    navController.navigate(Screen.Home.route) {
                        // This removes Onboarding, Welcome, and Login from the back stack
                        popUpTo(Screen.OnBoarding.route) {
                            inclusive = true
                        }
                    }
                },
                onSignupClick = {
                    // Go from Login -> Signup
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        // Signup Screen Route
        composable(route = Screen.Signup.route) {
            // Our new SignupScreen
            SignupScreen(
                navController = navController,
                onSignupClick = {
                    // 5. Signup -> Home (and clear the whole back stack)
                    navController.navigate(Screen.Home.route) {
                        // This removes Onboarding, Welcome, and Signup from the back stack
                        popUpTo(Screen.OnBoarding.route) {
                            inclusive = true
                        }
                    }
                },
                onLoginClick = {
                    // Go from Signup -> Login
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Home Screen Route
        composable(route = Screen.Home.route) {
            // 6. Your existing Home Screen
            HomeScreen(navController = navController)
        }

        // Classifier Screen Route
        composable(route = Screen.Classifier.route) {
            // 7. Your existing Classifier Screen (navigated to from Home)
            ClassifierScreen(navController = navController)
        }

        composable(route = Screen.Support.route) {
            // 6. Your existing Home Screen
            AboutUsScreen(navController = navController)
        }
    }
}