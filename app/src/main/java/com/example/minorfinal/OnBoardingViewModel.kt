package com.example.minorfinal


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Onboarding screen.
 * @param items The list of pages to display.
 */
data class OnBoardingUiState(
    val items: List<OnBoardingItem> = emptyList()
)

/**
 * ViewModel for the Onboarding screen.
 * Handles loading the onboarding items.
 */
class OnBoardingViewModel : ViewModel() {

    // Private mutable state flow
    private val _uiState = MutableStateFlow(OnBoardingUiState())
    // Public read-only state flow for the UI to observe
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    init {
        // Load the items as soon as the ViewModel is created
        loadItems()
    }

    /**
     * Loads the onboarding items from the repository
     * and updates the UI state.
     */
    private fun loadItems() {
        viewModelScope.launch {
            val items = OnBoardingRepository.getOnBoardingItems()
            _uiState.update { currentState ->
                currentState.copy(items = items)
            }
        }
    }
}