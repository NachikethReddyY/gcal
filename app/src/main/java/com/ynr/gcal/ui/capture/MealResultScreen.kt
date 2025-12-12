package com.ynr.gcal.ui.capture

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.ynr.gcal.ui.theme.DeepBlue
import com.ynr.gcal.ui.theme.EnergeticOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealResultScreen(
    viewModel: CaptureViewModel,
    onSaveComplete: () -> Unit
) {
    val result = viewModel.analysisResult
    
    // Safety check if accessed without result
    if (result == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No analysis result found.")
            Button(onClick = onSaveComplete) {
                Text("Go Back")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meal Analysis") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Preview
            viewModel.capturedBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Captured Food",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 24.dp)
                )
            }

            Text(result.foodName, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${result.calories} kcal", style = MaterialTheme.typography.displayMedium, color = DeepBlue)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Macros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem("Carbs", result.carbs)
                MacroItem("Protein", result.protein)
                MacroItem("Fat", result.fat)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    viewModel.saveMeal()
                    onSaveComplete()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EnergeticOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save to Log", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString() + "g", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
