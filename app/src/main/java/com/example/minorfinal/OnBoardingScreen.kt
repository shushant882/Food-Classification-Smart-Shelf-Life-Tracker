package com.example.minorfinal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager // <-- USES THE CORRECT IMPORT
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

/**
 * Stateful composable for the Onboarding screen.
 * Connects to the ViewModel and handles navigation logic.
 */
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isLastPage = pagerState.currentPage == items.size - 1

    fun navigateToHome() {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.OnBoarding.route) {
                inclusive = true
            }
        }
    }

    OnBoardingScreenContent(
        items = items,
        pagerState = pagerState,
        isLastPage = isLastPage,
        onSkipClicked = { navigateToHome() },
        onNextClicked = {
            if (!isLastPage) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        },
        onGetStartedClicked = { navigateToHome() }
    )
}

/**
 * Stateless composable that draws the Onboarding UI.
 */
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
            Text(text = "Skip", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // THIS IS THE LINE THAT WAS CRASHING
            // It now uses the correctly implemented HorizontalPagerIndicator from the library
            HorizontalPagerIndicator(
                pageCount = items.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(top = 16.dp)
            )


            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLastPage) {
                        onGetStartedClicked()
                    } else {
                        onNextClicked()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE65100)
                )
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Composable for drawing a single page of the onboarding flow.
 */
@Composable
fun OnBoardingPage(item: OnBoardingItem) {
    Column(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
            contentScale = ContentScale.Crop
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {


                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

}}
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
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}