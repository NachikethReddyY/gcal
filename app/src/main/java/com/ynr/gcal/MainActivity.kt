package com.ynr.gcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
                val onboardingCompleted by appContainer.userRepository.onboardingCompleted.collectAsState(initial = false)
                
                GCalNavHost(
                    appContainer = appContainer,
                    startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route
                )
            }
        }
    }
}
