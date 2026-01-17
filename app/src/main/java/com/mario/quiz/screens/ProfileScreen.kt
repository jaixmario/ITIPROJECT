package com.mario.quiz.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mario.quiz.data.FirebaseManager
import com.mario.quiz.data.LocalDataManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE) }
    val userName = remember { sharedPreferences.getString("user_name", "Student") }
    val localDataManager = remember { LocalDataManager(context) }
    var dbVersion by remember { mutableStateOf(localDataManager.getDbVersion() ?: "N/A") }
    var isUpdating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Profile", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Logged in as: $userName", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Database Version: $dbVersion", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isUpdating) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    scope.launch {
                        isUpdating = true
                        val remoteDbVersion = FirebaseManager.getDbVersion()
                        if (remoteDbVersion != null && remoteDbVersion != dbVersion) {
                            val subjectsData = FirebaseManager.getSubjectsData()
                            if (subjectsData != null) {
                                val dataMap = subjectsData.getValue(object : com.google.firebase.database.GenericTypeIndicator<Map<String, Any>>() {})
                                val json = Gson().toJson(dataMap)
                                localDataManager.saveSubjectsData(json)
                                localDataManager.saveDbVersion(remoteDbVersion)
                                dbVersion = remoteDbVersion // Update the UI
                            }
                        }
                        isUpdating = false
                    }
                }) {
                    Text("Check for Updates")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                with(sharedPreferences.edit()) {
                    clear()
                    apply()
                }
                navController.navigate("splash") { popUpTo(navController.graph.id) { inclusive = true } }
            }) {
                Text("Logout")
            }
        }
    }
}
