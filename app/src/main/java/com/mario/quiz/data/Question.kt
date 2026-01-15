package com.mario.quiz.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Question(
    val question: String = "",
    val options: Map<String, String> = emptyMap(),
    val answer: String = ""
)
