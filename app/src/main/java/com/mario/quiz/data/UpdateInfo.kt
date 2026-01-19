package com.mario.quiz.data

import com.google.gson.annotations.SerializedName

data class DatabaseUpdate(
    @SerializedName("database")
    val database: UpdateInfo = UpdateInfo()
)

data class UpdateInfo(
    @SerializedName("version")
    val version: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("block")
    val block: String = "FALSE", // Default to FALSE
    @SerializedName("db_message")
    val dbMessage: String = "" // New field for update notifications
)
