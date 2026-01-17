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

    fun getSubjects(): List<String> {
        val json = sharedPreferences.getString("subjects_data", null) ?: return emptyList()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(json, type)
        return map.keys.toList()
    }

    fun getQuestions(subject: String): List<Question> {
        val json = sharedPreferences.getString("subjects_data", null) ?: return emptyList()
        val type = object : TypeToken<Map<String, Map<String, Question>>>() {}.type
        val map: Map<String, Map<String, Question>> = gson.fromJson(json, type)
        return map[subject]?.values?.toList() ?: emptyList()
    }
}
