package com.example.minorfinal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

val DarkBg = Color(0xFF000000)
val DarkCard = Color(0xFF1A1A1A)
val AccentOrange = Color(0xFFE65100)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(
    navController: NavController,
    viewModel: OnBoardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val items = uiState.items
    val pagerState = rememberPagerState { items.size }
    val scope = rememberCoroutineScope()

    if (items.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentOrange)
        }
        return
    }

    val isLastPage = pagerState.currentPage == items.size - 1

    fun navigateToWelcome() {
        navController.navigate(Screen.Welcome.route) {
            popUpTo(Screen.OnBoarding.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        OnBoardingScreenContent(
            items = items,
            pagerState = pagerState,
            isLastPage = isLastPage,
            onSkipClicked = { navigateToWelcome() },
            onNextClicked = {
                if (!isLastPage) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            onGetStartedClicked = { navigateToWelcome() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreenContent(
    items: List<OnBoardingItem>,
    pagerState: PagerState,
    isLastPage: Boolean,
    onSkipClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onGetStartedClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnBoardingPage(item = items[page])
        }

        TextButton(
            onClick = onSkipClicked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Skip",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPagerIndicator(
                pageCount = items.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLastPage) onGetStartedClicked() else onNextClicked()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
@Composable
fun OnBoardingPage(item: OnBoardingItem) {
    Column(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f),
            contentScale = ContentScale.Crop
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f),
            color = DarkCard,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top  // start text immediately at top
            ) {

                Spacer(modifier = Modifier.height(12.dp)) // small space only

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFCCCCCC)
                )
            }
        }
    }
}

@Composable
fun HorizontalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->

            val color = if (index == currentPage)
                AccentOrange
            else Color.Gray.copy(alpha = 0.4f)

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)    // FIXED SPACING
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
