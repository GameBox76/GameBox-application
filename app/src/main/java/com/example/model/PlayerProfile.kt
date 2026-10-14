package com.example.model

data class PlayerProfile(
    val username: String = "CyberPlayer",
    val avatarIndex: Int = 0,
    val level: Int = 1,
    val currentXp: Int = 0,
    val coins: Int = 100,
    val streakDays: Int = 3,
    val gamesPlayed: Int = 0,
    val totalVictories: Int = 0,
    val playtimeMinutes: Int = 0,
    val favoriteGameIds: Set<String> = emptySet(),
    val recentGameIds: List<String> = emptyList(),
    val highScores: Map<String, Int> = emptyMap()
) {
    val xpForNextLevel: Int
        get() = level * 200

    val levelProgress: Float
        get() = (currentXp % 200).toFloat() / 200f

    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((totalVictories.toFloat() / gamesPlayed.toFloat()) * 100).toInt() else 0
}
