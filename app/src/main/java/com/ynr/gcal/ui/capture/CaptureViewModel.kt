package com.ynr.gcal.ui.capture

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ynr.gcal.data.local.MealDao
import com.ynr.gcal.data.local.MealLog
import com.ynr.gcal.data.repository.GeminiRepository
import com.ynr.gcal.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

class CaptureViewModel(
    private val mealDao: MealDao,
    private val geminiRepository: GeminiRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var capturedBitmap by mutableStateOf<Bitmap?>(null)
    var textInput by mutableStateOf("")
    
    var isAnalyzing by mutableStateOf(false)
    var analysisResult by mutableStateOf<MealLog?>(null)
    var error by mutableStateOf<String?>(null)

    fun analyzeText(text: String) {
        viewModelScope.launch {
            isAnalyzing = true
            error = null
            try {
                val apiKey = userRepository.apiKey.first()
                if (apiKey.isNullOrBlank()) {
                    error = "API Key needed in Settings"
                    return@launch
                }

                val jsonStr = geminiRepository.analyzeFoodText(apiKey, text)
                parseandSetResult(jsonStr, isManual = true)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isAnalyzing = false
            }
        }
    }

    fun analyzeImage(bitmap: Bitmap, hint: String) {
        viewModelScope.launch {
            isAnalyzing = true
            error = null
            try {
                val apiKey = userRepository.apiKey.first()
                if (apiKey.isNullOrBlank()) {
                    error = "API Key needed in Settings"
                    return@launch
                }

                val jsonStr = geminiRepository.analyzeFoodImage(apiKey, bitmap, hint)
                parseandSetResult(jsonStr, isManual = false)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isAnalyzing = false
            }
        }
    }

    private fun parseandSetResult(jsonStr: String, isManual: Boolean) {
        try {
            val json = JSONObject(jsonStr)
            val name = json.optString("foodName", "Unknown Food")
            val cals = json.optInt("calories", 0)
            val p = json.optInt("protein", 0)
            val c = json.optInt("carbs", 0)
            val f = json.optInt("fat", 0)

            analysisResult = MealLog(
                timestamp = System.currentTimeMillis(),
                foodName = name,
                calories = cals,
                protein = p,
                carbs = c,
                fat = f,
                isManual = isManual
            )
        } catch (e: Exception) {
            error = "Failed to parse AI response"
        }
    }

    fun saveMeal() {
        viewModelScope.launch {
            analysisResult?.let {
                mealDao.insertMeal(it)
                reset()
            }
        }
    }
    
    fun reset() {
        capturedBitmap = null
        textInput = ""
        isAnalyzing = false
        analysisResult = null
        error = null
    }

    class Factory(
        private val mealDao: MealDao,
        private val geminiRepository: GeminiRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CaptureViewModel(mealDao, geminiRepository, userRepository) as T
        }
    }
}
