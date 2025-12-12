package com.ynr.gcal.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ynr.gcal.data.AppContainer
import com.ynr.gcal.ui.home.HomeScreen
import com.ynr.gcal.ui.home.LogsScreen
import com.ynr.gcal.ui.home.SettingsScreen
import com.ynr.gcal.ui.onboarding.OnboardingScreen
import com.ynr.gcal.ui.onboarding.OnboardingViewModel
import com.ynr.gcal.ui.theme.DeepBlue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ynr.gcal.ui.capture.CaptureSheet
import com.ynr.gcal.ui.capture.CaptureViewModel
import com.ynr.gcal.ui.capture.CameraScreen
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Logs : Screen("logs", Icons.Default.List, "Logs")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
    object Camera : Screen("camera")
}

@Composable
fun GCalNavHost(
    appContainer: AppContainer,
    startDestination: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show BottomBar only on main tabs
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Logs.route, Screen.Settings.route)
    
    var showCaptureSheet by remember { mutableStateOf(false) }
    
    // Result Listener for Camera
    val currentEntry = navController.currentBackStackEntryAsState().value
    val savedStateHandle = currentEntry?.savedStateHandle
    
    val captureViewModel: CaptureViewModel = viewModel(
        factory = CaptureViewModel.Factory(
            appContainer.mealDao,
            appContainer.geminiRepository,
            appContainer.userRepository
        )
    )

    val context = LocalContext.current
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getLiveData<Uri>("captured_uri")?.observeForever { uri ->
            if (uri != null) {
                // Load Bitmap
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                     android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                     @Suppress("DEPRECATION")
                     android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                
                captureViewModel.capturedBitmap = bitmap
                showCaptureSheet = true
                savedStateHandle.remove<Uri>("captured_uri")
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    val items = listOf(Screen.Home, Screen.Logs, Screen.Settings)
                    items.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                            label = { Text(screen.label!!) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { showCaptureSheet = true },
                    containerColor = DeepBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, "Capture")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End 
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                val viewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModel.Factory(
                        appContainer.userRepository,
                        appContainer.geminiRepository
                    )
                )
                OnboardingScreen(
                    viewModel = viewModel,
                    onOnboardingComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) { HomeScreen(appContainer) }
            composable(Screen.Logs.route) { LogsScreen(appContainer) }
            composable(Screen.Settings.route) { SettingsScreen(appContainer) }
            composable(Screen.Camera.route) {
                CameraScreen(
                    onPhotoCaptured = { uri ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("captured_uri", uri)
                        navController.popBackStack()
                    },
                    onGallerySelected = { uri ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("captured_uri", uri)
                        navController.popBackStack()
                    },
                    onClose = { navController.popBackStack() }
                )
            }
        }
        
        if (showCaptureSheet) {
            CaptureSheet(
                viewModel = captureViewModel,
                onDismiss = { showCaptureSheet = false },
                onCameraClick = {
                    showCaptureSheet = false
                    navController.navigate(Screen.Camera.route)
                }
            )
        }
    }
}
