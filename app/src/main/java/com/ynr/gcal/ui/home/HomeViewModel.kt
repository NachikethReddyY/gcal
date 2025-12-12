package com.ynr.gcal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ynr.gcal.data.local.DailySummary
import com.ynr.gcal.data.local.MealDao
import com.ynr.gcal.data.local.MealLog
import com.ynr.gcal.data.repository.UserRepository
import com.ynr.gcal.data.repository.UserTargets
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class HomeUiState(
    val targets: UserTargets = UserTargets(2000, 150, 200, 60),
    val consumedCalories: Int = 0,
    val consumedProtein: Int = 0,
    val consumedCarbs: Int = 0,
    val consumedFat: Int = 0,
    val streak: Int = 0,
    val waterIntake: Int = 0,
    val waterGoal: Int = 2500,
    val recentLogs: List<MealLog> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val userRepository: UserRepository,
    private val mealDao: MealDao
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val uiState: StateFlow<HomeUiState> = _selectedDate.flatMapLatest { date ->
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val dateString = date.toString() // YYYY-MM-DD

        combine(
            userRepository.userTargets,
            mealDao.getMealsBetween(startOfDay, endOfDay),
            mealDao.getSummary(dateString)
        ) { targets, meals, summary ->
            val consumedCals = meals.sumOf { it.calories }
            val consumedProt = meals.sumOf { it.protein }
            val consumedCarbs = meals.sumOf { it.carbs }
            val consumedFat = meals.sumOf { it.fat }
            
            // Recent logs (last 3, sorted by timestamp descending)
            val recent = meals.sortedByDescending { it.timestamp }.take(3)
            
            HomeUiState(
                targets = targets,
                consumedCalories = consumedCals,
                consumedProtein = consumedProt,
                consumedCarbs = consumedCarbs,
                consumedFat = consumedFat,
                streak = summary?.streakCount ?: 0,
                waterIntake = summary?.waterIntake ?: 0,
                recentLogs = recent
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun changeDate(daysToAdd: Long) {
        val newDate = _selectedDate.value.plusDays(daysToAdd)
        if (!newDate.isAfter(LocalDate.now())) {
            _selectedDate.value = newDate
        }
    }

    fun updateWater(amount: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val dateString = date.toString()
            val currentSummary = mealDao.getSummary(dateString).firstOrNull()
            
            val newWater = ((currentSummary?.waterIntake ?: 0) + amount).coerceAtLeast(0)
            
            val summary = currentSummary?.copy(waterIntake = newWater) ?: DailySummary(
                date = dateString,
                totalCalories = 0,
                totalProtein = 0,
                totalCarbs = 0,
                totalFat = 0,
                goalCalories = 0,
                streakCount = 0,
                waterIntake = newWater
            )
            mealDao.insertOrUpdateSummary(summary)
        }
    }

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
