package com.ynr.gcal.data.repository

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiRepository {

    private fun getModel(apiKey: String, modelName: String): GenerativeModel {
        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey
        )
    }

    suspend fun calculateTargets(
        apiKey: String,
        age: Int, weight: Float, gender: String, activity: String, goal: String, diet: String
    ): String = withContext(Dispatchers.IO) {
        val model = getModel(apiKey, "gemini-2.5-flash")
        val prompt = "User is $age, $weight kg, $gender, $diet, Activity Level: $activity. Goal: $goal. " +
                "Calculate precise Daily Calorie, Protein, Carb, and Fat targets. " +
                "Return strictly JSON with keys: 'calories', 'protein', 'carbs', 'fat'. No markdown."
        
        val response = model.generateContent(prompt)
        response.text ?: "{}"
    }

    suspend fun analyzeFoodText(apiKey: String, text: String): String = withContext(Dispatchers.IO) {
        val model = getModel(apiKey, "gemini-2.5-flash")
        val prompt = "Estimate nutritional values for standard portion size of: \"$text\". " +
                "Return strictly JSON with keys: 'foodName', 'calories', 'protein', 'carbs', 'fat'. No markdown."
        
        val response = model.generateContent(prompt)
        response.text ?: "{}"
    }

    suspend fun analyzeFoodImage(apiKey: String, bitmap: Bitmap, hint: String): String = withContext(Dispatchers.IO) {
        val model = getModel(apiKey, "gemini-2.5-flash")
        
        val inputContent = content {
            image(bitmap)
            text("Analyze this food. Hint: $hint. Return strictly JSON with keys: 'foodName', 'calories', 'protein', 'carbs', 'fat'. No markdown.")
        }

        val response = model.generateContent(inputContent)
        response.text ?: "{}"
    }
}
