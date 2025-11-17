package com.example.minorfinal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minorfinal.ui.theme.*
import com.example.minorfinal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavController) {

    val teamMembers = listOf(
        Profile(
            name = "Shushant",
            enrollmentNumber = "231B336",
            photoRes = R.drawable.me,
            profileUrl = "https://github.com/shushant882",
            linkedinUrl = "https://www.linkedin.com/in/shushant336/"
        ),
        Profile(
            name = "Sonal Chauhan",
            enrollmentNumber = "231B344",
            photoRes = R.drawable.sonal,
            profileUrl = "https://github.com/sonal-chauhan0406",
            linkedinUrl = "https://www.linkedin.com/in/sonal-chauhan-s0419/"
        ),
        Profile(
            name = "Dharmraj Singh",
            enrollmentNumber = "231B101",
            photoRes = R.drawable.dj,
            profileUrl = "https://github.com/DharmrajSingh09",
            linkedinUrl = "https://www.linkedin.com/in/dharmraj-singh-13b669284"
        )
    )

    Scaffold(
        containerColor = Color(0xFF0D0D0D), // DARK BACKGROUND
        topBar = {
            TopAppBar(
                title = { Text("About Us", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0D0D0D)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(teamMembers.size) { index ->
                SimpleProfileCard(
                    name = teamMembers[index].name,
                    enrollmentNumber = teamMembers[index].enrollmentNumber,
                    photoRes = teamMembers[index].photoRes,
                    profileUrl = teamMembers[index].profileUrl,
                    linkedinUrl = teamMembers[index].linkedinUrl
                )
            }
        }

    }
}

private data class Profile(
    val name: String,
    val enrollmentNumber: String,
    @DrawableRes val photoRes: Int,
    val profileUrl: String,
    val linkedinUrl: String
)

@Composable
private fun SimpleProfileCard(
    name: String,
    enrollmentNumber: String,
    @DrawableRes photoRes: Int,
    profileUrl: String,
    linkedinUrl: String
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A) // DARK CARD
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = photoRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Text(
                text = name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = enrollmentNumber,
                fontSize = 16.sp,
                color = Color(0xFFBBBBBB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { uriHandler.openUri(profileUrl) }) {
                Text(
                    text = "GitHub Profile",
                    color = Color(0xFFFF6F00), // orange accent
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = { uriHandler.openUri(linkedinUrl) }) {
                Text(
                    text = "LinkedIn Profile",
                    color = Color(0xFF00A8FF), // cyan accent
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AboutUsScreenPreview() {
    AboutUsScreen(navController = rememberNavController())
}
