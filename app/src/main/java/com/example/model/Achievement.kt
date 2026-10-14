package com.example.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val target: Int,
    val current: Int = 0,
    val rewardXp: Int = 100,
    val isUnlocked: Boolean = false
) {
    val progressPercent: Float
        get() = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 1f
}
