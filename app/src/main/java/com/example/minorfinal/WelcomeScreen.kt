package com.example.minorfinal
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minorfinal.ui.theme.YumQuickButtonText
import com.example.minorfinal.ui.theme.YumQuickLoginButton
import com.example.minorfinal.ui.theme.YumQuickOrange
import com.example.minorfinal.ui.theme.YumQuickSignupButton

@Composable
fun WelcomeScreen(
    navController: NavController,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YumQuickOrange)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // IMPORTANT: Add your logo to res/drawable and name it 'logo_yumquick.png'
        Image(
            painter = painterResource(id = R.drawable.logo_yumquick),
            contentDescription = "App Logo",
            modifier = Modifier.size(180.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(20.dp))

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "SmartBite's unique Dynamic Freshness Engine uses your phone's live location to fetch real-time temperature and humidity. It then runs a machine learning model to predict a true shelf life based on your specific environment.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(60.dp))
        Button(
            onClick = onLoginClick, // This is now connected to your NavHost
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YumQuickLoginButton),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Log In",
                color = YumQuickButtonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignupClick, // This is now connected to your NavHost
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YumQuickSignupButton),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Sign Up",
                color = YumQuickButtonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}