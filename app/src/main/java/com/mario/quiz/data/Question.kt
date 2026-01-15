package com.mario.quiz.data

data class Question(
    val question: String,
    val options: List<String>,
    val answer: String
)

val mockQuestions = listOf(
    Question(
        question = "What does CPU stand for?",
        options = listOf("Central Processing Unit", "Computer Processing Unit", "Control Program Unit", "Central Program Utility"),
        answer = "Central Processing Unit"
    ),
    Question(
        question = "Which of the following is an input device?",
        options = listOf("Monitor", "Printer", "Keyboard", "Speaker"),
        answer = "Keyboard"
    ),
    Question(
        question = "Which memory is volatile?",
        options = listOf("ROM", "Hard Disk", "RAM", "DVD"),
        answer = "RAM"
    ),
    Question(
        question = "What is the full form of LAN?",
        options = listOf("Local Area Network", "Large Area Network", "Long Area Node", "Local Access Network"),
        answer = "Local Area Network"
    ),
    Question(
        question = "Which operating system is open-source?",
        options = listOf("Windows", "macOS", "Linux", "DOS"),
        answer = "Linux"
    )
)
