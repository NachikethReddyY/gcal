package com.ynr.gcal.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ynr.gcal.data.repository.GeminiRepository
import com.ynr.gcal.data.repository.UserRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    // Form State
    var age by mutableStateOf("")
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var gender by mutableStateOf("Male") // Default
    var activityLevel by mutableStateOf("Sedentary (Student/Desk)")
    var goal by mutableStateOf("Maintain")
    var diet by mutableStateOf("Standard")
    var apiKey by mutableStateOf("")

    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var onboardingComplete by mutableStateOf(false)

    // Navigation Step
    var currentStep by mutableStateOf(0)
    val totalSteps = 5 // Welcome, Basics, Activity, Goal, Diet

    fun nextStep() {
        if (currentStep < totalSteps - 1) {
            currentStep++
        } else {
            submitOnboarding()
        }
    }

    fun previousStep() {
        if (currentStep > 0) currentStep--
    }

    private fun submitOnboarding() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. Save Basic Data
                userRepository.saveApiKey(apiKey) // Assume user entered it or we use default/env
                userRepository.saveOnboardingData(
                    age.toIntOrNull() ?: 25,
                    height.toFloatOrNull() ?: 170f,
                    weight.toFloatOrNull() ?: 70f,
                    gender, activityLevel, goal, diet
                )

                // 2. Call Gemini
                // If API Key is empty, this will fail. We should probably enforce it.
                // For now, let's assume valid key is provided in step 0 or hardcoded for dev test if user skips.
                if (apiKey.isBlank()) {
                   // error("API Key is required") - Decide how to handle. For now, let's proceed and fail at network layer if needed
                }

                val jsonResponse = geminiRepository.calculateTargets(
                    apiKey,
                    age.toIntOrNull() ?: 25,
                    weight.toFloatOrNull() ?: 70f,
                    gender, activityLevel, goal, diet
                )

                // 3. Parse and Save
                val json = JSONObject(jsonResponse)
                val calories = json.optInt("calories", 2000)
                val protein = json.optInt("protein", 150)
                val carbs = json.optInt("carbs", 200)
                val fat = json.optInt("fat", 60)

                userRepository.saveTargets(calories, protein, carbs, fat)
                onboardingComplete = true

            } catch (e: Exception) {
                errorMessage = "Failed to generate targets: ${e.localizedMessage}"
                // Fallback for demo? No, let's show error
            } finally {
                isLoading = false
            }
        }
    }
    
    // Factory for manual DI
    class Factory(
        private val userRepository: UserRepository,
        private val geminiRepository: GeminiRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(userRepository, geminiRepository) as T
        }
    }
}
