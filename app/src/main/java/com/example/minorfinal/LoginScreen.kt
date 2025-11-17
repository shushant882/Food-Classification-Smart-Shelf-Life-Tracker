package com.example.minorfinal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.minorfinal.ui.theme.YumQuickDarkText
import com.example.minorfinal.ui.theme.YumQuickGrayText
import com.example.minorfinal.ui.theme.YumQuickInputBg
import com.example.minorfinal.ui.theme.YumQuickOrange
import com.example.minorfinal.ui.theme.YumQuickOrangeLight
import com.example.minorfinal.ui.theme.YumQuickWhite
import com.example.minorfinal.ui.theme.YumQuickYellowBg


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    var emailOrMobile by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hello!",
                        fontWeight = FontWeight.Bold,
                        color = YumQuickDarkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YumQuickDarkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YumQuickYellowBg)
            )
        },
        containerColor = YumQuickYellowBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = YumQuickWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = YumQuickDarkText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AuthTextField(
                        value = emailOrMobile,
                        onValueChange = { emailOrMobile = it },
                        label = "Email or Mobile Number",
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggleClick = { passwordVisible = !passwordVisible }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Forget Password",
                        color = YumQuickOrangeLight,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { /* TODO: Handle Forget Password */ }
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onLoginClick, // Connected to NavHost
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YumQuickOrange),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = "Log In",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "or", color = YumQuickGrayText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Use Fingerprint",
                        tint = YumQuickOrange,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { /* Handle fingerprint logic */ }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        Text("Don't have an account? ", color = YumQuickGrayText)
                        Text(
                            text = "Sign Up",
                            color = YumQuickOrangeLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onSignupClick) // Connected to NavHost
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggleClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = YumQuickDarkText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = YumQuickInputBg,
                unfocusedContainerColor = YumQuickInputBg,
                disabledContainerColor = YumQuickInputBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = onPasswordToggleClick) {
                        Icon(imageVector = icon, contentDescription = "Toggle password", tint = YumQuickOrangeLight)
                    }
                }
            }
        )
    }
}