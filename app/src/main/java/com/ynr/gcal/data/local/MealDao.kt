package com.ynr.gcal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    // Meal Logs
    @Insert
    suspend fun insertMeal(meal: MealLog)

    @Query("SELECT * FROM meal_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getMealsBetween(startTime: Long, endTime: Long): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE date(timestamp / 1000, 'unixepoch') = date(:timestamp / 1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getMealsForDay(timestamp: Long): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealLog>>

    // Daily Summaries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSummary(summary: DailySummary)

    @Query("SELECT * FROM daily_summaries WHERE date = :date")
    fun getSummary(date: String): Flow<DailySummary?>

    @Query("SELECT * FROM daily_summaries ORDER BY date DESC LIMIT 7")
    fun getRecentSummaries(): Flow<List<DailySummary>>
}
