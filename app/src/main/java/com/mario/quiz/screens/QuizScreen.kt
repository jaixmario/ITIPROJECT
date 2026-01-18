package com.mario.quiz.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mario.quiz.data.LocalDataManager
import com.mario.quiz.data.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController, subject: String?) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionKey by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    val userAnswers = remember { mutableListOf<String>() }
    var answerRevealed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val localDataManager = remember { LocalDataManager(context) }

    LaunchedEffect(subject) {
        if (subject != null) {
            questions = localDataManager.getQuestions(subject).shuffled() // Shuffle questions for variety
        }
    }

    Scaffold(
        topBar = {
            if (subject != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = subject.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        bottomBar = {
             // Intentionally empty for a cleaner quiz focus.
        }
    ) { paddingValues ->
        if (subject == null) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Please select a subject first.", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("home") }) { Text("Choose a Subject") }
            }
        } else if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val question = questions[currentQuestionIndex]
            val progress by animateFloatAsState(targetValue = (currentQuestionIndex) / questions.size.toFloat(), label = "QuizProgress", animationSpec = tween(600))

            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                // Progress Bar and Counter
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Question ${currentQuestionIndex + 1} of ${questions.size}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))

                // Question Text
                Text(text = question.question, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp))

                // Answer Options
                question.options.toSortedMap().forEach { (key, optionText) ->
                    SelectableAnswerOption(
                        text = optionText,
                        isSelected = selectedOptionKey == key,
                        isCorrect = key == question.answer,
                        answerRevealed = answerRevealed,
                        onOptionSelected = { if (!answerRevealed) selectedOptionKey = key }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                if (answerRevealed) {
                    Button(
                        onClick = {
                            userAnswers.add(selectedOptionKey ?: "")
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedOptionKey = null
                                answerRevealed = false
                            } else {
                                navController.navigate("quiz_result?subject=$subject&score=$score&userAnswers=${userAnswers.joinToString(",")}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(if (currentQuestionIndex < questions.size - 1) "Next Question" else "Finish Quiz", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Button(
                        onClick = {
                            if (selectedOptionKey != null) {
                                answerRevealed = true
                                if (selectedOptionKey == question.answer) score++
                            }
                        },
                        enabled = selectedOptionKey != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Confirm", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableAnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    answerRevealed: Boolean,
    onOptionSelected: () -> Unit
) {
    val correctColor = Color(0xFF388E3C) // A calmer green
    val incorrectColor = MaterialTheme.colorScheme.error

    val targetBorderColor = when {
        !answerRevealed && isSelected -> MaterialTheme.colorScheme.primary
        answerRevealed && isCorrect -> correctColor
        answerRevealed && isSelected && !isCorrect -> incorrectColor
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // More subtle default border
    }

    val targetBackgroundColor = when {
        !answerRevealed && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        answerRevealed && isCorrect -> correctColor.copy(alpha = 0.1f)
        answerRevealed && isSelected && !isCorrect -> incorrectColor.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor by animateColorAsState(targetValue = targetBorderColor, label = "borderColorAnim", animationSpec = tween(300))
    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor, label = "bgColorAnim", animationSpec = tween(300))

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOptionSelected),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, borderColor), // Corrected: Consistent border width
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)

            if (answerRevealed) {
                if (isCorrect) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Correct", tint = correctColor, modifier = Modifier.size(24.dp))
                } else if (isSelected) {
                    Icon(Icons.Filled.Close, contentDescription = "Incorrect", tint = incorrectColor, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
