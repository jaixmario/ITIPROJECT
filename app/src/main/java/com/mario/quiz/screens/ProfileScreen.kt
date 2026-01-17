package com.mario.quiz.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.mario.quiz.R
import com.mario.quiz.data.FirebaseManager
import com.mario.quiz.data.LocalDataManager
import com.mario.quiz.data.QuizResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)
    val sharedPreferences = remember { context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE) }
    val userName = remember { sharedPreferences.getString("user_name", "Student") }
    val localDataManager = remember { LocalDataManager(context) }
    var dbVersion by remember { mutableStateOf(localDataManager.getDbVersion() ?: "N/A") }
    var isUpdating by remember { mutableStateOf(false) }
    var quizHistory by remember { mutableStateOf<List<QuizResult>>(emptyList()) }
    val scope = rememberCoroutineScope()

    var selectedAvatar by remember { mutableStateOf(sharedPreferences.getInt("user_avatar", R.drawable.avatar_default)) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    val avatars = listOf(R.drawable.avatar_default, R.drawable.avatar01)

    LaunchedEffect(userName) {
        if (!userName.isNullOrBlank() && userName != "Student") {
            quizHistory = localDataManager.getQuizHistory(userName)
        }
    }

    val totalQuizzes = quizHistory.size
    val overallAccuracy = if (quizHistory.isNotEmpty()) {
        val totalCorrect = quizHistory.sumOf { it.score }
        val totalPossible = quizHistory.sumOf { it.total }
        if (totalPossible > 0) (totalCorrect.toFloat() / totalPossible.toFloat()) * 100 else 0f
    } else 0f
    val bestScore = quizHistory.maxByOrNull { it.score }?.score ?: 0

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = selectedAvatar),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { showAvatarDialog = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userName ?: "Student", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "Database Version: $dbVersion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard(title = "Total Quizzes", value = totalQuizzes.toString())
                StatCard(title = "Accuracy", value = "${String.format("%.1f", overallAccuracy)}%")
                StatCard(title = "Best Score", value = bestScore.toString())
            }
            
            Spacer(modifier = Modifier.height(24.dp))

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
                                // Restart the app to apply changes
                                activity?.recreate()
                            }
                        }
                        isUpdating = false
                    }
                }) {
                    Text("Check for Updates")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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

    if (showAvatarDialog) {
        AvatarPickerDialog(
            avatars = avatars,
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = {
                selectedAvatar = it
                with(sharedPreferences.edit()) {
                    putInt("user_avatar", it)
                    apply()
                }
                showAvatarDialog = false
            }
        )
    }
}

@Composable
fun AvatarPickerDialog(avatars: List<Int>, onDismiss: () -> Unit, onAvatarSelected: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Choose Your Avatar") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(avatars) { avatarResId ->
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "Avatar Option",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable { onAvatarSelected(avatarResId) }
                            .padding(4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
