package com.mario.quiz.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mario.quiz.R
import com.mario.quiz.data.FirebaseManager
import com.mario.quiz.data.LocalDataManager
import com.mario.quiz.data.fetchUpdateInfo
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    var setupMessage by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        val localDataManager = LocalDataManager(context)
        val prefs = context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE)

        // --- Start of Update Logic ---
        val isFirstRun = !prefs.contains("database_initialized")

        if (isFirstRun) {
            // First time app is running: download database and show message.
            setupMessage = "Setting up for the first time..."
            withTimeoutOrNull(10000L) { // 10-second timeout for first run
                val remoteDbVersion = FirebaseManager.getDbVersion()
                if (remoteDbVersion != null) {
                    val subjectsData = FirebaseManager.getSubjectsData()
                    if (subjectsData != null) {
                        val dataMap = subjectsData.getValue(object : com.google.firebase.database.GenericTypeIndicator<Map<String, Any>>() {})
                        val json = Gson().toJson(dataMap)
                        localDataManager.saveSubjectsData(json)
                        localDataManager.saveDbVersion(remoteDbVersion)
                        prefs.edit().putBoolean("database_initialized", true).apply()
                    }
                }
            }
        } else {
            // Subsequent launches: check for remote update in the background.
            val updateInfo = fetchUpdateInfo()
            if (updateInfo != null) {
                val localVersion = localDataManager.getDbVersion()
                if (updateInfo.database.version != localVersion) {
                    // If there's a new version, show the message from your JSON.
                    Toast.makeText(context, updateInfo.database.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        // --- End of Update Logic ---
        
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
        Icon(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = "App Logo", tint = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "ITI IT Quiz", style = MaterialTheme.typography.displayLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = setupMessage, style = MaterialTheme.typography.titleMedium, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}
