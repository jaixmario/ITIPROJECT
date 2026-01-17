package com.mario.quiz.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mario.quiz.R
import com.mario.quiz.data.FirebaseManager
import com.mario.quiz.data.LocalDataManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    var setupMessage by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        val localDataManager = LocalDataManager(context)
        val prefs = context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = !prefs.contains("database_initialized")

        if (isFirstRun) {
            // This is the first time the app is running.
            setupMessage = "Setting up for the first time..."
            val remoteDbVersion = FirebaseManager.getDbVersion()
            if (remoteDbVersion != null) {
                val subjectsData = FirebaseManager.getSubjectsData()
                if (subjectsData != null) {
                    val dataMap = subjectsData.getValue(object : com.google.firebase.database.GenericTypeIndicator<Map<String, Any>>() {})
                    val json = Gson().toJson(dataMap)
                    localDataManager.saveSubjectsData(json)
                    localDataManager.saveDbVersion(remoteDbVersion)
                    // Mark that the initial download is complete.
                    prefs.edit().putBoolean("database_initialized", true).apply()
                }
            }
        } else {
             // On subsequent launches, just show a quick loading message.
            setupMessage = "Welcome back!"
            delay(1000) // A short delay for branding.
        }

        // Proceed to the appropriate screen.
        val userName = prefs.getString("user_name", null)
        if (userName.isNullOrBlank()) {
            navController.navigate("add_name") { popUpTo("splash") { inclusive = true } }
        } else {
            navController.navigate("home") { popUpTo("splash") { inclusive = true } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ITI IT Quiz",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = setupMessage,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}
