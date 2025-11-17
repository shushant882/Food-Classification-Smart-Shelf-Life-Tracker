package com.example.minorfinal.ui.screens.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minorfinal.ui.theme.YumQuickOrange

/**
 * A reusable Bottom Navigation Bar with a center Floating Action Button (FAB).
 * This component layers a NavigationBar and a FAB inside a Box.
 *
 * @param selectedIcon The name of the currently selected icon (e.g., "Home").
 * @param onIconClick A lambda function that is called when an icon is clicked.
 * @param onFabClick A lambda function that is called when the center FAB is clicked.
 */
@Composable
fun AppBottomNavBar(
    selectedIcon: String,
    onIconClick: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Standard height for Nav bar + room for FAB
    ) {
        // 1. The orange NavigationBar at the bottom
        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = YumQuickOrange,
            contentColor = Color.White.copy(alpha = 0.7f),
            tonalElevation = 0.dp
        ) {
            // Left Icons
            AppNavBarItem(
                name = "Home",
                icon = Icons.Outlined.Home,
                isSelected = selectedIcon == "Home",
                onClick = { onIconClick("Home") }
            )
            AppNavBarItem(
                name = "Orders",
                icon = Icons.Outlined.Fastfood,
                isSelected = selectedIcon == "Orders",
                onClick = { onIconClick("Orders") }
            )

            // Center Spacer to make room for the FAB
            Spacer(modifier = Modifier.weight(1f))

            // Right Icons
            AppNavBarItem(
                name = "History",
                icon = Icons.Outlined.ReceiptLong,
                isSelected = selectedIcon == "History",
                onClick = { onIconClick("History") }
            )
            AppNavBarItem(
                name = "Support",
                icon = Icons.Outlined.Headset,
                isSelected = selectedIcon == "Support",
                onClick = { onIconClick("Support") }
            )
        }

        // 2. The Floating Action Button (Camera)
        FloatingActionButton(
            onClick = onFabClick,
            shape = CircleShape,
            containerColor = YumQuickOrange,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter) // Align to the top-center of the Box
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = "Classifier")
        }
    }
}

/**
 * A helper composable to create the navigation bar items.
 * This must be called from within a RowScope (like NavigationBar).
 */
@Composable
private fun RowScope.AppNavBarItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = name) },
        label = { Text(name, fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.7f),
            unselectedTextColor = Color.White.copy(alpha = 0.7f),
            indicatorColor = YumQuickOrange // Hides the selection indicator
        )
    )
}

@Preview
@Composable
fun AppBottomNavBarPreview() {
    AppBottomNavBar(
        selectedIcon = "Home",
        onIconClick = {},
        onFabClick = {}
    )
}