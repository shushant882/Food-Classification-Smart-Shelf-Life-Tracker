package com.example.minorfinal


/**
 * Defines all the routes in the app.
 * This sealed class is used to ensure type-safe navigation.
 */
sealed class Screen(val route: String) {
    object OnBoarding : Screen("onboarding_screen")
    object Home : Screen("home_screen")
    object Classifier : Screen("classifier_screen")
}