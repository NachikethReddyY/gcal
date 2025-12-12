package com.ynr.gcal.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ynr.gcal.data.AppContainer
import com.ynr.gcal.ui.theme.DeepBlue
import com.ynr.gcal.ui.theme.EnergeticOrange
import com.ynr.gcal.ui.theme.SuccessGreen

@Composable
fun HomeScreen(
    appContainer: AppContainer
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            appContainer.userRepository,
            appContainer.mealDao
        )
    )
    
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Today", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main Calorie Ring
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            MacroRing(
                progress = if (state.targets.calories > 0) state.consumedCalories / state.targets.calories.toFloat() else 0f,
                color = DeepBlue,
                label = "Calories",
                value = "${state.consumedCalories} / ${state.targets.calories}",
                modifier = Modifier.width(200.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Small Rings for Macros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MacroRing(
                progress = if (state.targets.protein > 0) state.consumedProtein / state.targets.protein.toFloat() else 0f,
                color = EnergeticOrange,
                label = "Protein",
                value = "${state.consumedProtein}g",
                modifier = Modifier.width(80.dp),
                strokeWidth = 8.dp
            )
            MacroRing(
                progress = if (state.targets.carbs > 0) state.consumedCarbs / state.targets.carbs.toFloat() else 0f,
                color = SuccessGreen,
                label = "Carbs",
                value = "${state.consumedCarbs}g",
                modifier = Modifier.width(80.dp),
                strokeWidth = 8.dp
            )
            MacroRing(
                progress = if (state.targets.fat > 0) state.consumedFat / state.targets.fat.toFloat() else 0f,
                color = Color.Yellow,
                label = "Fat",
                value = "${state.consumedFat}g",
                modifier = Modifier.width(80.dp),
                strokeWidth = 8.dp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Streak Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Streak", style = MaterialTheme.typography.titleMedium)
                Text("${state.streak} Days 🔥", style = MaterialTheme.typography.headlineSmall, color = EnergeticOrange)
            }
        }
    }
}
