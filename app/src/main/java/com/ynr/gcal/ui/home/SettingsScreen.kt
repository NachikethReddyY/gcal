package com.ynr.gcal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ynr.gcal.data.AppContainer
import com.ynr.gcal.data.repository.UserRepository
import com.ynr.gcal.ui.theme.DeepBlue
import com.ynr.gcal.ui.theme.OffWhite
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.ynr.gcal.data.repository.UserTargets

class SettingsViewModel(private val repository: UserRepository) : androidx.lifecycle.ViewModel() {
    val apiKey = repository.apiKey
    val targets = repository.userTargets
    val diet = repository.diet

    fun updateApiKey(key: String) {
        viewModelScope.launch { repository.saveApiKey(key) }
    }
    
    fun updateTargets(cals: Int, p: Int, c: Int, f: Int) {
        viewModelScope.launch { repository.saveTargets(cals, p, c, f) }
    }
    
    fun updateDiet(newDiet: String) {
        viewModelScope.launch { repository.updateDiet(newDiet) }
    }

    // Factory
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: UserRepository) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}

@Composable
fun SettingsScreen(appContainer: AppContainer) {
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(appContainer.userRepository))
    val apiKey by viewModel.apiKey.collectAsState(initial = "")
    val targets by viewModel.targets.collectAsState(initial = UserTargets(2000, 150, 200, 60))
    val diet by viewModel.diet.collectAsState(initial = "Balanced")
    
    var showApiDialog by remember { mutableStateOf(false) }
    var showTargetsDialog by remember { mutableStateOf(false) }
    var showDietSelectionDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = DeepBlue,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection("Goals") {
            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Daily Targets",
                subtitle = "${targets.calories} kcal • ${targets.protein}g P • ${targets.carbs}g C • ${targets.fat}g F",
                onClick = { showTargetsDialog = true }
            )
            SettingsItem(
                icon = Icons.Default.Person, // Reusing icon, consider a more appropriate one
                title = "Diet Type",
                subtitle = diet,
                onClick = { showDietSelectionDialog = true }
            )
        }

        SettingsSection("Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "My Profile",
                subtitle = "Update weight, goal, activity",
                onClick = { /* TODO: Re-run onboarding or edit profile */ }
            )
        }

        SettingsSection("Preferences") {
            SettingsItem(
                icon = Icons.Default.Edit,
                title = "API Key",
                subtitle = if (apiKey.isNullOrEmpty()) "Not Set" else "••••••••",
                onClick = { showApiDialog = true }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { /* TODO: Clear Data */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Reset App Data", color = Color.Red)
        }
    }

    if (showApiDialog) {
        ApiKeyDialog(
            currentKey = apiKey ?: "",
            onDismiss = { showApiDialog = false },
            onSave = { 
                viewModel.updateApiKey(it)
                showApiDialog = false 
            }
        )
    }
    
    if (showTargetsDialog) {
        EditTargetsDialog(
            currentTargets = targets,
            onDismiss = { showTargetsDialog = false },
            onSave = { c, p, ca, f -> 
                viewModel.updateTargets(c, p, ca, f)
                showTargetsDialog = false
            }
        )
    }

    if (showDietSelectionDialog) {
        DietSelectionDialog(
            currentDiet = diet,
            onDismiss = { showDietSelectionDialog = false },
            onSave = { newDiet ->
                viewModel.updateDiet(newDiet)
                showDietSelectionDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = DeepBlue)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
fun ApiKeyDialog(currentKey: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var apiKeyInput by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gemini API Key") },
        text = {
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Enter Key") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(apiKeyInput) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun EditTargetsDialog(
    currentTargets: UserTargets,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit
) {
    var calories by remember { mutableStateOf(currentTargets.calories.toString()) }
    var protein by remember { mutableStateOf(currentTargets.protein.toString()) }
    var carbs by remember { mutableStateOf(currentTargets.carbs.toString()) }
    var fat by remember { mutableStateOf(currentTargets.fat.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Targets") },
        text = {
            Column {
                OutlinedTextField(
                    value = calories, 
                    onValueChange = { calories = it }, 
                    label = { Text("Calories") },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = protein, 
                    onValueChange = { protein = it }, 
                    label = { Text("Protein (g)") },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = carbs, 
                    onValueChange = { carbs = it }, 
                    label = { Text("Carbs (g)") },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = fat, 
                    onValueChange = { fat = it }, 
                    label = { Text("Fat (g)") },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    calories.toIntOrNull() ?: currentTargets.calories,
                    protein.toIntOrNull() ?: currentTargets.protein,
                    carbs.toIntOrNull() ?: currentTargets.carbs,
                    fat.toIntOrNull() ?: currentTargets.fat
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DietSelectionDialog(
    currentDiet: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val diets = listOf("Balanced", "Low Carb", "Keto", "High Protein", "Vegan", "Paleo")
    var selected by remember { mutableStateOf(currentDiet) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Diet Preference") },
        text = {
            Column {
                diets.forEach { diet ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = diet }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selected == diet),
                            onClick = { selected = diet }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = diet, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selected) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
