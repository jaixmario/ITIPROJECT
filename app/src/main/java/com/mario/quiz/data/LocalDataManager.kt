package com.mario.quiz.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalDataManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("quiz_app_local_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getDbVersion(): String? {
        return sharedPreferences.getString("db_version", null)
    }

    fun saveDbVersion(version: String) {
        sharedPreferences.edit().putString("db_version", version).apply()
    }

    fun saveSubjectsData(data: String) {
        sharedPreferences.edit().putString("subjects_data", data).apply()
    }

    fun getSubjectsWithCounts(): Map<String, Int> {
        val json = sharedPreferences.getString("subjects_data", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Map<String, Any>>>() {}.type
        val map: Map<String, Map<String, Any>> = gson.fromJson(json, type)
        return map.mapValues { it.value.size }
    }

    fun getQuestions(subject: String): List<Question> {
        val json = sharedPreferences.getString("subjects_data", null) ?: return emptyList()
        val type = object : TypeToken<Map<String, Map<String, Question>>>() {}.type
        val map: Map<String, Map<String, Question>> = gson.fromJson(json, type)
        return map[subject]?.values?.toList() ?: emptyList()
    }

    fun saveQuizResult(userName: String, result: QuizResult) {
        val history = getQuizHistory(userName).toMutableList()
        history.add(0, result) // Add new result to the top
        val json = gson.toJson(history)
        sharedPreferences.edit().putString("quiz_history_$userName", json).apply()
    }

    fun getQuizHistory(userName: String): List<QuizResult> {
        val json = sharedPreferences.getString("quiz_history_$userName", null) ?: return emptyList()
        val type = object : TypeToken<List<QuizResult>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveUpdateInfo(updateInfo: DatabaseUpdate) {
        val json = gson.toJson(updateInfo)
        sharedPreferences.edit().putString("update_info_cache", json).apply()
    }

    fun getUpdateInfo(): DatabaseUpdate? {
        val json = sharedPreferences.getString("update_info_cache", null)
        return if (json != null) {
            gson.fromJson(json, DatabaseUpdate::class.java)
        } else {
            null
        }
    }
}
