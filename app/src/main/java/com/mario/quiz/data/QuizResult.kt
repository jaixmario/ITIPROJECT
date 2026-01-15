package com.mario.quiz.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuizResult(
    val subject: String = "",
    val score: Int = 0,
    val total: Int = 0,
    val timestamp: Long = 0L,
    val userAnswers: List<String> = emptyList()
)
