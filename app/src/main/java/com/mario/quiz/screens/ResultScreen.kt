package com.mario.quiz.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mario.quiz.data.FirebaseManager
import com.mario.quiz.data.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, subject: String?, score: Int, userAnswers: List<String>) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(subject) {
        if (subject != null) {
            questions = FirebaseManager.getQuestions(subject)
        }
        if (score != -1) {
            val sharedPreferences = context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE)
            val userName = sharedPreferences.getString("user_name", "") ?: ""
            if (questions.isNotEmpty()) {
                FirebaseManager.saveQuizResult(userName, score, questions.size)
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) {
        if (subject == null || score == -1) {
            Column(
                modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "You haven\'t completed a quiz yet.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("home") }) {
                    Text(text = "Choose a Subject")
                }
            }
        } else if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val totalQuestions = questions.size
            val progress by animateFloatAsState(targetValue = score.toFloat() / totalQuestions.toFloat(), label = "")

            Column(
                modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 16.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    Text(
                        text = "$score/$totalQuestions",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Correct: $score", style = MaterialTheme.typography.titleMedium)
                Text(text = "Wrong: ${totalQuestions - score}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showReviewDialog = true }) {
                    Text(text = "Review Answers")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("quiz?subject=$subject") }) {
                    Text(text = "Retry Test")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) {
                    Text(text = "Back to Home")
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text(text = "Review Answers") },
            text = {
                LazyColumn {
                    itemsIndexed(questions) { index, question ->
                        val userAnswer = userAnswers.getOrNull(index)
                        val isCorrect = userAnswer == question.answer
                        val backgroundColor = if (isCorrect) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(backgroundColor)
                                .padding(16.dp)
                        ) {
                            Text(text = "Question ${index + 1}: ${question.question}", style = MaterialTheme.typography.bodyMedium)
                            val userAnswerText = question.options[userAnswer] ?: "No answer"
                            Text(text = "Your answer: $userAnswerText", style = MaterialTheme.typography.bodySmall)
                            if (!isCorrect) {
                                val correctAnswerText = question.options[question.answer] ?: ""
                                Text(text = "Correct answer: $correctAnswerText", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
