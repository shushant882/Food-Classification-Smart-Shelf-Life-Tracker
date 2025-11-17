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
                title = "Fresh And Healthy Food",
                description = "Fuel your day, sharpen your focus, and feel your absolute best with our curated selection of clean, healthy food.",
                imageRes = R.drawable.onboarding_image_1
            ),
            OnBoardingItem(
                title = "AI-Powered Food Recognition",
                description = "See beyond the plate. Our AI identifies and understands your food photos, turning any meal into useful data.",
                imageRes = R.drawable.onboarding_image_2
            ),
            OnBoardingItem(
                title = "Live Weather Shelf Life Analysis",
                description = "Stop guessing. Our AI uses your phone's live location data to fetch the real-time temperature and humidity, then predicts the true shelf life of your food.",

                imageRes = R.drawable.onboarding_image_3
            )
        )
    }
}






