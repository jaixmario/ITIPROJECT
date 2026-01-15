package com.mario.quiz.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mario.quiz.data.FirebaseManager
import com.mario.quiz.data.QuizResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, subject: String?, score: Int, userAnswers: List<String>) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE) }
    val userName = remember { sharedPreferences.getString("user_name", "") }
    var quizHistory by remember { mutableStateOf<List<QuizResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userName, score, subject) {
        if (!userName.isNullOrBlank()) {
            isLoading = true

            // First, save the new result if one was just completed
            if (subject != null && score != -1) {
                val questions = FirebaseManager.getQuestions(subject)
                if (questions.isNotEmpty()) {
                    FirebaseManager.saveQuizResult(userName, subject, score, questions.size, userAnswers)
                }
            }

            // Then, fetch the complete history
            quizHistory = FirebaseManager.getQuizHistory(userName)
            isLoading = false
        }
    }

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Quiz History", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (quizHistory.isEmpty()) {
                Text("You haven't completed any quizzes yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quizHistory) { result ->
                        HistoryCard(result = result)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(result: QuizResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = result.subject.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(result.timestamp))
                Text(text = date, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "${result.score}/${result.total}", style = MaterialTheme.typography.titleLarge)
        }
    }
}
