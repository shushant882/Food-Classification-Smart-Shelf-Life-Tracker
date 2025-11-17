package com.example.minorfinal

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minorfinal.R
import com.example.minorfinal.ui.theme.*

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
        containerColor = YumQuickYellowBg,
        topBar = {
            TopAppBar(
                title = { Text("About Us", color = YumQuickDarkText) },
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

/**
 * Updated Profile class with LinkedIn URL
 */
private data class Profile(
    val name: String,
    val enrollmentNumber: String,
    @DrawableRes val photoRes: Int,
    val profileUrl: String,
    val linkedinUrl: String
)

/**
 * Profile Card with GitHub + LinkedIn buttons
 */
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
        colors = CardDefaults.cardColors(containerColor = YumQuickWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                color = YumQuickDarkText
            )

            Text(
                text = enrollmentNumber,
                fontSize = 16.sp,
                color = YumQuickGrayText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GitHub Button
            TextButton(onClick = { uriHandler.openUri(profileUrl) }) {
                Text(
                    text = "GitHub Profile",
                    color = YumQuickOrangeLight,
                    fontWeight = FontWeight.Bold
                )
            }

            // LinkedIn Button
            TextButton(onClick = { uriHandler.openUri(linkedinUrl) }) {
                Text(
                    text = "LinkedIn Profile",
                    color = YumQuickOrangeLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AboutUsScreenPreview() {
    val navController = rememberNavController()
    AboutUsScreen(navController = navController)
}
