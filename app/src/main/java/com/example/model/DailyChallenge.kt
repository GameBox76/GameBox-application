package com.example.model

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val targetGameId: String,
    val targetScore: Int,
    val currentScore: Int = 0,
    val rewardXp: Int = 150,
    val rewardCoins: Int = 50,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    val progressPercent: Float
        get() = if (targetScore > 0) (currentScore.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f) else 1f
}
