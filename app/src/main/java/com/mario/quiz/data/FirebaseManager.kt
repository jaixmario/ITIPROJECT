package com.mario.quiz.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FirebaseManager {
    private val database = FirebaseDatabase.getInstance("https://itiprojects-aa75f-default-rtdb.asia-southeast1.firebasedatabase.app")

    fun saveUserName(name: String) {
        val usersRef = database.getReference("users")
        usersRef.child(name).setValue(mapOf("name" to name))
    }

    fun saveQuizResult(userName: String, score: Int, totalQuestions: Int) {
        val resultsRef = database.getReference("results").child(userName).push()
        val result = mapOf(
            "score" to score,
            "total" to totalQuestions,
            "timestamp" to System.currentTimeMillis()
        )
        resultsRef.setValue(result)
    }

    suspend fun getQuizHistory(userName: String): List<Map<String, Long>> = suspendCoroutine { continuation ->
        val resultsRef = database.getReference("results").child(userName)
        resultsRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val history = mutableListOf<Map<String, Long>>()
                for (child in snapshot.children) {
                    val score = child.child("score").getValue(Long::class.java) ?: 0L
                    val total = child.child("total").getValue(Long::class.java) ?: 0L
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    history.add(mapOf("score" to score, "total" to total, "timestamp" to timestamp))
                }
                continuation.resume(history.reversed()) // Newest first
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }

    suspend fun getSubjects(): List<String> = suspendCoroutine { continuation ->
        val subjectsRef = database.getReference("subjects")
        subjectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val subjects = mutableListOf<String>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.key?.let { subjects.add(it) }
                }
                continuation.resume(subjects)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }

    suspend fun getQuestions(subject: String): List<Question> = suspendCoroutine { continuation ->
        val questionsRef = database.getReference("subjects").child(subject)
        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questions = mutableListOf<Question>()
                for (childSnapshot in snapshot.children) {
                    val question = childSnapshot.getValue(Question::class.java)
                    if (question != null) {
                        questions.add(question)
                    }
                }
                continuation.resume(questions)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }
}
