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

    suspend fun getDbVersion(): String? = suspendCoroutine { continuation ->
        val versionRef = database.getReference("db_version")
        versionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Object::class.java)
                continuation.resume(value?.toString())
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
