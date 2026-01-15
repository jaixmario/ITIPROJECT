package com.mario.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mario.quiz.screens.AddNameScreen
import com.mario.quiz.screens.HomeScreen
import com.mario.quiz.screens.ProfileScreen
import com.mario.quiz.screens.QuizScreen
import com.mario.quiz.screens.ResultScreen
import com.mario.quiz.screens.SplashScreen

@Composable
fun QuizNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController = navController) }
        composable("add_name") { AddNameScreen(navController = navController) }
        composable("home") { HomeScreen(navController = navController) }
        composable(
            "quiz/{subject}",
            arguments = listOf(navArgument("subject") { type = NavType.StringType })
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            QuizScreen(navController = navController, subject = subject)
        }
        composable(
            "result/{subject}/{score}/{userAnswers}",
            arguments = listOf(
                navArgument("subject") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
                navArgument("userAnswers") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val userAnswers = backStackEntry.arguments?.getString("userAnswers")?.split(",") ?: emptyList()
            ResultScreen(navController = navController, subject = subject, score = score, userAnswers = userAnswers)
        }
        composable("profile") { ProfileScreen(navController = navController) }
    }
}
