package com.ynr.gcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ynr.gcal.ui.navigation.GCalNavHost
import com.ynr.gcal.ui.navigation.Screen
import com.ynr.gcal.ui.theme.GCalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as GCalApplication).container
        
        setContent {
            GCalTheme {
                val onboardingCompleted by appContainer.userRepository.onboardingCompleted.collectAsState(initial = null)
                
                if (onboardingCompleted != null) {
                    GCalNavHost(
                        appContainer = appContainer,
                        startDestination = if (onboardingCompleted == true) Screen.Home.route else Screen.Onboarding.route
                    )
                } else {
                    // Loading State (Empty for now, or Splash)
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = com.ynr.gcal.ui.theme.DeepBlue)
                    }
                }
            }
        }
    }
}
