package com.example.minorfinal



sealed class Screen(val route: String) {
    object OnBoarding : Screen("onboarding_screen")
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object Signup : Screen("signup_screen")
    object Home : Screen("home_screen")
    object Classifier : Screen("classifier_screen")
    object Support : Screen("AboutUs_screen")
}