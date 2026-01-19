package com.mario.quiz.data

/**
 * Compares two version strings to see if the first is newer than the second.
 * Handles versions like "1.10" and "1.9" correctly.
 */
fun isVersionNewer(remoteVersion: String, localVersion: String): Boolean {
    try {
        val remoteParts = remoteVersion.split('.').map { it.toInt() }
        val localParts = localVersion.split('.').map { it.toInt() }
        val partCount = maxOf(remoteParts.size, localParts.size)

        for (i in 0 until partCount) {
            val remotePart = remoteParts.getOrNull(i) ?: 0
            val localPart = localParts.getOrNull(i) ?: 0
            if (remotePart > localPart) {
                return true
            }
            if (remotePart < localPart) {
                return false
            }
        }
    } catch (e: NumberFormatException) {
        // If versions are not valid numbers, fall back to simple string comparison
        return remoteVersion > localVersion
    }
    return false // Versions are identical
}
