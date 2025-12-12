package com.ynr.gcal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MealLog::class, DailySummary::class], version = 1, exportSchema = false)
abstract class GCalDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    companion object {
        @Volatile
        private var INSTANCE: GCalDatabase? = null

        fun getDatabase(context: Context): GCalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GCalDatabase::class.java,
                    "gcal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
