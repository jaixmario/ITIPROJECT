package com.mario.quiz.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mario.quiz.data.LocalDataManager
import com.mario.quiz.data.Question
import com.mario.quiz.data.QuizResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(navController: NavController, subject: String?, score: Int, userAnswers: List<String>) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val localDataManager = remember { LocalDataManager(context) }

    LaunchedEffect(subject) {
        if (subject != null) {
            val fetchedQuestions = localDataManager.getQuestions(subject)
            questions = fetchedQuestions

            // Save the result to local history
            if (score != -1 && fetchedQuestions.isNotEmpty()) {
                val sharedPreferences = context.getSharedPreferences("quiz_app_prefs", Context.MODE_PRIVATE)
                val userName = sharedPreferences.getString("user_name", "") ?: ""
                val newResult = QuizResult(subject, score, fetchedQuestions.size, System.currentTimeMillis(), userAnswers)
                localDataManager.saveQuizResult(userName, newResult)
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) {
        if (subject == null || score == -1) {
            // This part should ideally not be reached in this screen
            Column(modifier = Modifier.fillMaxSize().padding(it), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No result found.")
                Button(onClick = { navController.navigate("home") }) {
                    Text("Go to Home")
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
                Text("Quiz Completed!", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(200.dp), strokeWidth = 16.dp)
                    Text(text = "$score/$totalQuestions", style = MaterialTheme.typography.displayLarge)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Correct: $score", style = MaterialTheme.typography.titleMedium)
                Text(text = "Wrong: ${totalQuestions - score}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showReviewDialog = true }) {
                    Text("Review Answers")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = { navController.navigate("quiz?subject=$subject") }, modifier = Modifier.weight(1f)) {
                        Text("Retry Test")
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Button(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }, modifier = Modifier.weight(1f)) {
                        Text("Back to Home")
                    }
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(backgroundColor).padding(16.dp)
                        ) {
                            Text(text = "Q${index + 1}: ${question.question}", style = MaterialTheme.typography.bodyMedium)
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
            confirmButton = { TextButton(onClick = { showReviewDialog = false }) { Text("OK") } }
        )
    }
}
