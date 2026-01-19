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
import com.mario.quiz.data.UpdateRepository
import com.mario.quiz.data.isVersionNewer
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    var setupMessage by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        // Use the repository to get update info (handles offline caching)
        val updateRepository = UpdateRepository(context)
        val updateInfo = updateRepository.getUpdateInfo()

        // 1. Check for app block first
        if (updateInfo != null && updateInfo.database.block.equals("TRUE", ignoreCase = true)) {
            val message = URLEncoder.encode(updateInfo.database.message, StandardCharsets.UTF_8.toString())
            navController.navigate("blocked?message=$message") { popUpTo("splash") { inclusive = true } }
            return@LaunchedEffect // Stop all further execution
        }

        // 2. Handle first-time database setup
        val localDataManager = LocalDataManager(context)
        val prefs = context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = !prefs.contains("database_initialized")

        if (isFirstRun) {
            setupMessage = "Setting up for the first time..."
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
        } else if (updateInfo != null) {
            // 3. On subsequent runs, check for a new DB version and show message
            val localVersion = localDataManager.getDbVersion()
            if (localVersion != null && isVersionNewer(updateInfo.database.version, localVersion)) {
                Toast.makeText(context, updateInfo.database.dbMessage, Toast.LENGTH_LONG).show()
            }
        }
        
        // 4. Proceed to the main app
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
