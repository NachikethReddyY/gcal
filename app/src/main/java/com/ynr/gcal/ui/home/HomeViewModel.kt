package com.ynr.gcal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ynr.gcal.data.local.MealDao
import com.ynr.gcal.data.repository.UserRepository
import com.ynr.gcal.data.repository.UserTargets
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HomeUiState(
    val targets: UserTargets = UserTargets(2000, 150, 200, 60),
    val consumedCalories: Int = 0,
    val consumedProtein: Int = 0,
    val consumedCarbs: Int = 0,
    val consumedFat: Int = 0,
    val streak: Int = 0
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val mealDao: MealDao
) : ViewModel() {

    // Combine targets and today's meal logs to calculate progress
    // Note: We could also use DailySummary table if we update it transactionally.
    // For simplicity, let's aggregate from MealLogs relative to today's timestamp.
    
    // Using simple flow combination
    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.userTargets,
        // In a real app, we'd filter by Day. For simplicity, let's just get all simple sum or 
        // rely on a specific query. Let's assume getAllMeals() returns everything and we filter in memory
        // OR better, we use getMealsForDay(today defined in DAO).
        mealDao.getMealsForDay(System.currentTimeMillis()) 
    ) { targets, meals ->
        // Calculate totals
        val consumedCals = meals.sumOf { it.calories }
        val consumedProt = meals.sumOf { it.protein }
        val consumedCarbs = meals.sumOf { it.carbs }
        val consumedFat = meals.sumOf { it.fat }
        
        HomeUiState(
            targets = targets,
            consumedCalories = consumedCals,
            consumedProtein = consumedProt,
            consumedCarbs = consumedCarbs,
            consumedFat = consumedFat,
            streak = 0 // Placeholder, requires DailySummary logic
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    class Factory(
        private val userRepository: UserRepository,
        private val mealDao: MealDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(userRepository, mealDao) as T
        }
    }
}
