package com.ynr.gcal.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ynr.gcal.data.AppContainer
import com.ynr.gcal.data.local.MealLog
import com.ynr.gcal.ui.theme.DeepBlue
import com.ynr.gcal.ui.theme.EnergeticOrange
import com.ynr.gcal.ui.theme.OffWhite
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

class LogsViewModel(private val appContainer: AppContainer) : androidx.lifecycle.ViewModel() {
    val meals = appContainer.mealDao.getAllMeals()
    
    fun deleteMeal(meal: MealLog) {
        viewModelScope.launch {
            appContainer.mealDao.deleteMeal(meal)
        }
    }
    
    // Factory
    @Suppress("UNCHECKED_CAST")
    class Factory(private val appContainer: AppContainer) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return LogsViewModel(appContainer) as T
        }
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(appContainer: AppContainer) {
    val viewModel: LogsViewModel = viewModel(factory = LogsViewModel.Factory(appContainer))
    val meals by viewModel.meals.collectAsState(initial = emptyList())
    
    // Group meals by Date
    val groupedMeals = remember(meals) {
        meals.groupBy { 
            java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate() 
        }
    }
    
    val dateFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd") }
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("h:mm a") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
    ) {
        Text(
            "Meal History",
            style = MaterialTheme.typography.headlineMedium,
            color = DeepBlue,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (meals.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No meals logged yet. Tap + to add one!", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedMeals.forEach { (date, dailyMeals) ->
                    stickyHeader {
                        Text(
                            text = date.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(OffWhite)
                                .padding(vertical = 8.dp)
                        )
                    }
                    
                    items(dailyMeals, key = { it.id }) { meal ->
                        val dismissState = rememberDismissState(
                            confirmValueChange = {
                                if (it == DismissValue.DismissedToStart) {
                                    viewModel.deleteMeal(meal)
                                    true
                                } else false
                            }
                        )
                        
                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Red, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            },
                            dismissContent = {
                                MealCard(meal, timeFormatter)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealCard(meal: MealLog, timeFormatter: java.time.format.DateTimeFormatter) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for image or icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                 if (meal.photoUri != null) {
                     Text("📷")
                 } else {
                     Text(
                        text = meal.foodName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepBlue,
                        fontWeight = FontWeight.Bold
                    )
                 }
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val time = java.time.Instant.ofEpochMilli(meal.timestamp).atZone(java.time.ZoneId.systemDefault())
                Text(
                    text = time.format(timeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${meal.calories} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = EnergeticOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "P:${meal.protein} C:${meal.carbs} F:${meal.fat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}


