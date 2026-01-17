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

    suspend fun saveQuizResult(userName: String, subject: String, score: Int, totalQuestions: Int, userAnswers: List<String>): Unit = suspendCoroutine { continuation ->
        val resultsRef = database.getReference("results").child(userName).push()
        val result = QuizResult(
            subject = subject,
            score = score,
            total = totalQuestions,
            timestamp = System.currentTimeMillis(),
            userAnswers = userAnswers
        )
        resultsRef.setValue(result)
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnFailureListener { continuation.resume(Unit) } // Resume anyway to not block the app
    }

    suspend fun getQuizHistory(userName: String): List<QuizResult> = suspendCoroutine { continuation ->
        val resultsRef = database.getReference("results").child(userName)
        resultsRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val history = mutableListOf<QuizResult>()
                for (child in snapshot.children) {
                    val result = child.getValue(QuizResult::class.java)
                    if (result != null) {
                        history.add(result)
                    }
                }
                continuation.resume(history.reversed()) // Newest first
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(emptyList())
            }
        })
    }

    suspend fun getDbVersion(): String? = suspendCoroutine { continuation ->
        val versionRef = database.getReference("db_version")
        versionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot.getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(null)
            }
        })
    }

    suspend fun getSubjectsData(): DataSnapshot? = suspendCoroutine { continuation ->
        val subjectsRef = database.getReference("subjects")
        subjectsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(null)
            }
        })
    }
}
