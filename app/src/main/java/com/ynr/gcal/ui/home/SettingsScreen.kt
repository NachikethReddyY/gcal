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
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserRepository) : androidx.lifecycle.ViewModel() {
    val apiKey = repository.apiKey

    fun updateApiKey(key: String) {
        viewModelScope.launch {
            repository.saveApiKey(key)
        }
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
    var showApiDialog by remember { mutableStateOf(false) }

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


