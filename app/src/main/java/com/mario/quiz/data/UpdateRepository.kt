package com.mario.quiz.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateRepository(context: Context) {
    private val localDataManager = LocalDataManager(context)

    /**
     * Gets the update info. It first tries to fetch from the network.
     * If the network fails, it returns the last cached version.
     */
    suspend fun getUpdateInfo(): DatabaseUpdate? {
        return withContext(Dispatchers.IO) {
            val remoteData = fetchFromNetwork()
            if (remoteData != null) {
                // If network fetch is successful, save it and return it
                localDataManager.saveUpdateInfo(remoteData)
                remoteData
            } else {
                // If network fails, return the last saved data from cache
                localDataManager.getUpdateInfo()
            }
        }
    }

    private fun fetchFromNetwork(): DatabaseUpdate? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL("https://raw.githubusercontent.com/jaixmario/database/refs/heads/main/database2.json")
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000 // 5-second timeout
            connection.readTimeout = 5000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = InputStreamReader(inputStream)
                Gson().fromJson(reader, DatabaseUpdate::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null // Any exception means the fetch failed
        } finally {
            connection?.disconnect()
        }
    }
}
