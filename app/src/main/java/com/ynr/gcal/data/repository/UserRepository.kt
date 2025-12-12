package com.ynr.gcal.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserRepository(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val AGE = intPreferencesKey("age")
        val HEIGHT = floatPreferencesKey("height")
        val WEIGHT = floatPreferencesKey("weight")
        val GENDER = stringPreferencesKey("gender")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val GOAL = stringPreferencesKey("goal")
        val DIET = stringPreferencesKey("diet")
        
        // Calculated Targets
        val TARGET_CALORIES = intPreferencesKey("target_calories")
        val TARGET_PROTEIN = intPreferencesKey("target_protein")
        val TARGET_CARBS = intPreferencesKey("target_carbs")
        val TARGET_FAT = intPreferencesKey("target_fat")
        
        val ONBOARDING_COMPLETED = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
    }

    val apiKey: Flow<String?> = context.dataStore.data.map { it[API_KEY] }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    
    val userTargets: Flow<UserTargets> = context.dataStore.data.map { prefs ->
        UserTargets(
            calories = prefs[TARGET_CALORIES] ?: 2000,
            protein = prefs[TARGET_PROTEIN] ?: 150,
            carbs = prefs[TARGET_CARBS] ?: 200,
            fat = prefs[TARGET_FAT] ?: 60
        )
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }

    suspend fun saveOnboardingData(
        age: Int, height: Float, weight: Float, 
        gender: String, activity: String, goal: String, diet: String
    ) {
        context.dataStore.edit {
            it[AGE] = age
            it[HEIGHT] = height
            it[WEIGHT] = weight
            it[GENDER] = gender
            it[ACTIVITY_LEVEL] = activity
            it[GOAL] = goal
            it[DIET] = diet
        }
    }

    suspend fun saveTargets(calories: Int, protein: Int, carbs: Int, fat: Int) {
        context.dataStore.edit {
            it[TARGET_CALORIES] = calories
            it[TARGET_PROTEIN] = protein
            it[TARGET_CARBS] = carbs
            it[TARGET_FAT] = fat
            it[ONBOARDING_COMPLETED] = true
        }
    }
}

data class UserTargets(val calories: Int, val protein: Int, val carbs: Int, val fat: Int)
