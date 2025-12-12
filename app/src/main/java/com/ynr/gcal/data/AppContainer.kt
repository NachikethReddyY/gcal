package com.ynr.gcal.data

import android.content.Context
import com.ynr.gcal.data.local.GCalDatabase
import com.ynr.gcal.data.repository.GeminiRepository
import com.ynr.gcal.data.repository.UserRepository

class AppContainer(private val context: Context) {
    val database by lazy { GCalDatabase.getDatabase(context) }
    val mealDao by lazy { database.mealDao() }
    
    val userRepository by lazy { UserRepository(context) }
    val geminiRepository by lazy { GeminiRepository() }
}
