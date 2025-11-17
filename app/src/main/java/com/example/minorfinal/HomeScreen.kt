package com.example.minorfinal


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.minorfinal.R
import com.example.minorfinal.Screen
import com.example.minorfinal.ui.screens.components.AppBottomNavBar
import com.example.minorfinal.ui.theme.*

// --- Data Classes for Models ---

data class Category(
    val name: String,
    val icon: ImageVector
)

data class FoodItem(
    val name: String,
    val imageRes: Int, // Use Int for R.drawable
    val price: String,
    val rating: String? = null
)

// --- Composable ---
fun adaptiveTextColor(color: Color): Color {
    return if (color == Color.Black) Color.White else color
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    // --- State & Data ---
    var selectedBottomIcon by rememberSaveable { mutableStateOf("Home") }

    val categories = listOf(
        Category("Snacks", Icons.Default.Fastfood),
        Category("Meal", Icons.Default.Restaurant),
        Category("Vegan", Icons.Default.Eco),
        Category("Dessert", Icons.Default.Cake),
        Category("Drinks", Icons.Default.LocalBar)
    )

    // IMPORTANT: Add these images to res/drawable
    val bestSellers = listOf(
        FoodItem("Sushi Rolls", R.drawable.food_sushi, "$103.0"),
        FoodItem("Healthy Bowl", R.drawable.food_bowl, "$50.0"),
        FoodItem("Lasagna", R.drawable.food_lasagna, "$12.99"),
        FoodItem("Berry Cupcake", R.drawable.food_cupcake, "$8.20")
    )

    // IMPORTANT: Add these images to res/drawable
    val recommendations = listOf(
        FoodItem("Chicken Burger", R.drawable.food_burger, "$10.0", "5.0"),
        FoodItem("Spring Rolls", R.drawable.food_spring_rolls, "$25.0", "5.0")
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = { HomeTopAppBar() },
        bottomBar = {
            AppBottomNavBar(
                selectedIcon = selectedBottomIcon,
                onIconClick = { selectedBottomIcon = it },
                onFabClick = {
                    // This is the navigation to your Classifier
                    navController.navigate(Screen.Classifier.route)
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Welcome Title ---
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Good Morning",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Rise And Shine! It's Breakfast Time",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            // --- Categories ---
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(category)
                    }
                }
            }

            // --- Main Content Card ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = YumQuickWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Best Seller Section
                        SectionWithTitle(title = "Best Seller", onViewAllClick = {}) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(bestSellers) { item ->
                                    BestSellerCard(item)
                                }
                            }
                        }

                        // 30% Off Banner
                        // IMPORTANT: Add 'banner_30_off.png' to res/drawable
                        Image(
                            painter = painterResource(id = R.drawable.banner_30_off),
                            contentDescription = "30% Off Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Recommend Section
                        SectionWithTitle(title = "Recommend", onViewAllClick = null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                recommendations.forEach { item ->
                                    RecommendCard(
                                        item = item,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        // Add padding to the bottom of the content
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


// --- Sub-Composables ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar() {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search", color = YumQuickGrayText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = YumQuickOrange) },
            shape = RoundedCornerShape(25.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = YumQuickWhite,
                unfocusedContainerColor = YumQuickWhite,
                disabledContainerColor = YumQuickWhite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            singleLine = true
        )
        // Icon Buttons
        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = YumQuickOrange)
        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = YumQuickOrange)
        Icon(Icons.Default.Person, contentDescription = "Profile", tint = YumQuickOrange)
    }
}

@Composable
private fun CategoryItem(category: Category) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(YumQuickWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, contentDescription = category.name, tint = YumQuickOrange)
        }
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color =  Color.White
        )
    }
}

@Composable
private fun SectionWithTitle(
    title: String,
    onViewAllClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = YumQuickDarkText
            )
            if (onViewAllClick != null) {
                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = YumQuickOrange,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }
        content()
    }
}

@Composable
private fun BestSellerCard(item: FoodItem) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YumQuickWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.price,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = YumQuickOrange
                )
            }
        }
    }
}

@Composable
private fun RecommendCard(item: FoodItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = YumQuickWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.price,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = YumQuickOrange
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(
                        text = item.rating ?: "0.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = YumQuickDarkText
                    )
                }
            }
        }
    }
}


// --- Preview ---

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun HomeScreenPreview() {
    // We pass a fake NavController for the preview
    HomeScreen(navController = rememberNavController())
}