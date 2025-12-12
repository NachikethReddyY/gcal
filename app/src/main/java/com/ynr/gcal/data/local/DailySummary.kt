package com.ynr.gcal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val goalCalories: Int,
    val streakCount: Int
)
