package com.ynr.gcal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val selectedDate by viewModel.selectedDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Home of GCal", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("🔥 ${state.streak}", style = MaterialTheme.typography.titleMedium, color = EnergeticOrange)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.changeDate(-1) }) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }
            Text(
                text = if (selectedDate == java.time.LocalDate.now()) "Today" else selectedDate.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            IconButton(onClick = { viewModel.changeDate(1) }) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Water Tracker
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Water", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("${state.waterIntake} cups", style = MaterialTheme.typography.headlineSmall, color = DeepBlue)
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.updateWater(-1) }) {
                       Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = DeepBlue)
                    }
                    IconButton(
                        onClick = { viewModel.updateWater(1) },
                        modifier = Modifier.background(DeepBlue, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Add, "Increase", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Recently Logged Section
        Text("Recently logged", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.recentLogs.isEmpty()) {
            Text("No meals logged yet.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
        } else {
            state.recentLogs.forEach { meal ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        // Thumbnail
                         Box(
                            modifier = Modifier.size(48.dp).background(Color.LightGray, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                             if (meal.photoUri != null) {
                                 Text("\uD83D\uDCF7")
                             } else {
                                 Text(meal.foodName.take(1))
                             }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.foodName, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("\uD83D\uDD25 ${meal.calories} kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        
                        // Time
                        val time = java.time.Instant.ofEpochMilli(meal.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                        Text(
                            "${time.hour}:${String.format("%02d", time.minute)}", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
