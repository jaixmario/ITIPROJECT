package com.mario.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mario.quiz.navigation.QuizNavigation
import com.mario.quiz.ui.theme.QUIZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QUIZTheme {
                QuizNavigation()
            }
        }
    }
}
