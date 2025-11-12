package com.example.minorfinal



import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons


import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Model class representing a single onboarding page.
 *
 * @param title The title text for the page.
 * @param description The description text for the page.
 * @param icon The Material Icon to display.
 * @param imageRes The local drawable resource ID for the background image.
 */
data class OnBoardingItem(
    val title: String,
    val description: String,

    @DrawableRes val imageRes: Int
)

/**
 * Repository (data source) for onboarding items.
 * This provides the static list of pages.
 */
object OnBoardingRepository {

    /**
     * Gets the list of all onboarding pages.
     *
     * IMPORTANT:
     * This code assumes you have images in `res/drawable` named:
     * - onboarding_image_1.jpg
     * - onboarding_image_2.jpg
     * - onboarding_image_3.jpg
     */

    fun getOnBoardingItems(): List<OnBoardingItem> {
        return listOf(
            OnBoardingItem(
                title = "Order For Food",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                imageRes = R.drawable.onboarding_image_1
            ),
            OnBoardingItem(
                title = "Easy Payment",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                imageRes = R.drawable.onboarding_image_2
            ),
            OnBoardingItem(
                title = "Fast Delivery",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",

                imageRes = R.drawable.onboarding_image_3
            )
        )
    }
}






