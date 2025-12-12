package com.ynr.gcal.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ynr.gcal.R
import com.ynr.gcal.ui.theme.DeepBlue
import com.ynr.gcal.ui.theme.SoftGray

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
) {
    if (viewModel.onboardingComplete) {
        onOnboardingComplete()
        return // Should navigate away
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DeepBlue)
                Spacer(modifier = Modifier.height(16.dp))
                Text("AI is crafting your plan...", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                isFirstStep = viewModel.currentStep == 0,
                isLastStep = viewModel.currentStep == viewModel.totalSteps - 1,
                onBack = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Header Progress
            LinearProgressIndicator(
                progress = (viewModel.currentStep + 1) / viewModel.totalSteps.toFloat(),
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = DeepBlue,
                trackColor = SoftGray
            )
            Spacer(modifier = Modifier.height(24.dp))

            when (viewModel.currentStep) {
                0 -> StepWelcome(viewModel)
                1 -> StepBasics(viewModel)
                2 -> StepActivity(viewModel)
                3 -> StepGoal(viewModel)
                4 -> StepDiet(viewModel)
            }
            
            if (viewModel.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun OnboardingBottomBar(
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (!isFirstStep) {
            TextButton(onClick = onBack) {
                Text("Back", color = Color.Gray)
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text(if (isLastStep) "Get My Plan" else "Next")
        }
    }
}

@Composable
fun StepWelcome(viewModel: OnboardingViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.img_onboarding_welcome),
            contentDescription = "Welcome",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            "Welcome to GCal",
            style = MaterialTheme.typography.headlineLarge,
            color = DeepBlue,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your AI-powered nutrition assistant. Let's get to know you.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.apiKey,
            onValueChange = { viewModel.apiKey = it },
            label = { Text("Enter Gemini API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Text(
            "Required for AI features. Get it from aistudio.google.com",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StepBasics(viewModel: OnboardingViewModel) {
    Column {
        Text("The Basics", style = MaterialTheme.typography.headlineMedium)
        Text("We need this to calculate your BMR.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = viewModel.age,
            onValueChange = { viewModel.age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = viewModel.weight,
                onValueChange = { viewModel.weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = viewModel.height,
                onValueChange = { viewModel.height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Gender", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("Male", "Female").forEach { gender ->
                SelectableCard(
                    text = gender,
                    selected = viewModel.gender == gender,
                    onClick = { viewModel.gender = gender },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StepActivity(viewModel: OnboardingViewModel) {
    Column {
        Image(
            painter = painterResource(id = R.drawable.img_onboarding_activity),
            contentDescription = "Activity",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )
        Text("Activity Level", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        val options = listOf(
            "Sedentary (Student/Desk)",
            "Lightly Active (Walking)",
            "Active (Gym 3-4x)",
            "Very Active"
        )
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(options.size) { i ->
                SelectableCard(
                    text = options[i],
                    selected = viewModel.activityLevel == options[i],
                    onClick = { viewModel.activityLevel = options[i] },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StepGoal(viewModel: OnboardingViewModel) {
    Column {
        Text("Your Goal", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        val options = listOf("Lose Fat", "Maintain", "Build Muscle")
        
        options.forEach { goal ->
            SelectableCard(
                text = goal,
                selected = viewModel.goal == goal,
                onClick = { viewModel.goal = goal },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun StepDiet(viewModel: OnboardingViewModel) {
    Column {
        Text("Dietary Preference", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        val options = listOf("Standard", "Vegetarian", "Vegan", "High Protein")

        options.forEach { diet ->
            SelectableCard(
                text = diet,
                selected = viewModel.diet == diet,
                onClick = { viewModel.diet = diet },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun SelectableCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .selectable(selected = selected, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) DeepBlue else SoftGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) Color.White else Color.Black,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
