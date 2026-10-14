package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Achievement
import com.example.model.DailyChallenge
import com.example.model.PlayerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gamebox_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<PlayerProfile> = _profile.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(prefs.getBoolean("dark_mode", true))
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _achievements = MutableStateFlow(loadAchievements())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _dailyChallenges = MutableStateFlow(loadDailyChallenges())
    val dailyChallenges: StateFlow<List<DailyChallenge>> = _dailyChallenges.asStateFlow()

    private fun loadProfile(): PlayerProfile {
        val username = prefs.getString("username", "CyberPlayer") ?: "CyberPlayer"
        val avatarIndex = prefs.getInt("avatar_index", 0)
        val level = prefs.getInt("level", 1)
        val xp = prefs.getInt("current_xp", 120)
        val coins = prefs.getInt("coins", 250)
        val streak = prefs.getInt("streak_days", 4)
        val gamesPlayed = prefs.getInt("games_played", 12)
        val totalVictories = prefs.getInt("total_victories", 7)
        val playtime = prefs.getInt("playtime_minutes", 45)
        val favs = prefs.getStringSet("favorites", setOf("snake", "game2048")) ?: emptySet()
        val recents = prefs.getString("recents", "snake,game2048,tictactoe")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        val highScores = mutableMapOf<String, Int>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("highscore_") && value is Int) {
                highScores[key.removePrefix("highscore_")] = value
            }
        }
        if (highScores.isEmpty()) {
            highScores["snake"] = 42
            highScores["game2048"] = 2048
            highScores["brickbreaker"] = 380
            highScores["mathquiz"] = 650
            highScores["reaction"] = 230
        }

        return PlayerProfile(
            username = username,
            avatarIndex = avatarIndex,
            level = level,
            currentXp = xp,
            coins = coins,
            streakDays = streak,
            gamesPlayed = gamesPlayed,
            totalVictories = totalVictories,
            playtimeMinutes = playtime,
            favoriteGameIds = favs,
            recentGameIds = recents,
            highScores = highScores
        )
    }

    fun updateProfileName(name: String) {
        prefs.edit().putString("username", name).apply()
        _profile.value = _profile.value.copy(username = name)
    }

    fun updateAvatar(avatarIndex: Int) {
        prefs.edit().putInt("avatar_index", avatarIndex).apply()
        _profile.value = _profile.value.copy(avatarIndex = avatarIndex)
    }

    fun toggleFavorite(gameId: String) {
        val currentFavs = _profile.value.favoriteGameIds.toMutableSet()
        if (currentFavs.contains(gameId)) {
            currentFavs.remove(gameId)
        } else {
            currentFavs.add(gameId)
        }
        prefs.edit().putStringSet("favorites", currentFavs).apply()
        _profile.value = _profile.value.copy(favoriteGameIds = currentFavs)
    }

    fun recordGamePlayed(gameId: String, score: Int, isVictory: Boolean) {
        val currentProfile = _profile.value
        val newPlayed = currentProfile.gamesPlayed + 1
        val newVictories = currentProfile.totalVictories + if (isVictory) 1 else 0
        val earnedXp = if (isVictory) 50 else 20
        val earnedCoins = if (isVictory) 25 else 10

        val totalXp = currentProfile.currentXp + earnedXp
        val newLevel = (totalXp / 200) + 1
        val newCoins = currentProfile.coins + earnedCoins

        // High score
        val currentBest = currentProfile.highScores[gameId] ?: 0
        val updatedHighScores = currentProfile.highScores.toMutableMap()
        if (score > currentBest) {
            updatedHighScores[gameId] = score
            prefs.edit().putInt("highscore_$gameId", score).apply()
        }

        // Recents
        val newRecents = (listOf(gameId) + currentProfile.recentGameIds.filter { it != gameId }).take(6)
        prefs.edit()
            .putInt("games_played", newPlayed)
            .putInt("total_victories", newVictories)
            .putInt("current_xp", totalXp)
            .putInt("level", newLevel)
            .putInt("coins", newCoins)
            .putString("recents", newRecents.joinToString(","))
            .apply()

        _profile.value = currentProfile.copy(
            gamesPlayed = newPlayed,
            totalVictories = newVictories,
            currentXp = totalXp,
            level = newLevel,
            coins = newCoins,
            recentGameIds = newRecents,
            highScores = updatedHighScores
        )

        // Update daily challenges
        checkDailyChallenges(gameId, score)
        // Check achievements
        checkAchievements(newPlayed, newVictories, updatedHighScores)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _darkModeEnabled.value = enabled
    }

    fun claimDailyChallenge(challengeId: String) {
        val currentChallenges = _dailyChallenges.value
        val updated = currentChallenges.map { ch ->
            if (ch.id == challengeId && ch.isCompleted && !ch.isClaimed) {
                // Add rewards
                val curr = _profile.value
                val newXp = curr.currentXp + ch.rewardXp
                val newCoins = curr.coins + ch.rewardCoins
                val newLvl = (newXp / 200) + 1
                prefs.edit()
                    .putInt("current_xp", newXp)
                    .putInt("coins", newCoins)
                    .putInt("level", newLvl)
                    .putBoolean("challenge_claimed_$challengeId", true)
                    .apply()
                _profile.value = curr.copy(currentXp = newXp, coins = newCoins, level = newLvl)
                ch.copy(isClaimed = true)
            } else ch
        }
        _dailyChallenges.value = updated
    }

    private fun checkDailyChallenges(gameId: String, score: Int) {
        val current = _dailyChallenges.value
        val updated = current.map { ch ->
            if (ch.targetGameId == gameId) {
                val newScore = maxOf(ch.currentScore, score)
                val completed = newScore >= ch.targetScore
                ch.copy(currentScore = newScore, isCompleted = completed)
            } else ch
        }
        _dailyChallenges.value = updated
    }

    private fun checkAchievements(gamesPlayed: Int, victories: Int, highScores: Map<String, Int>) {
        val current = _achievements.value
        val updated = current.map { ach ->
            val currVal = when (ach.id) {
                "first_play" -> gamesPlayed
                "ten_plays" -> gamesPlayed
                "five_victories" -> victories
                "snake_pro" -> highScores["snake"] ?: 0
                "puzzle_master" -> highScores["game2048"] ?: 0
                "brick_smasher" -> highScores["brickbreaker"] ?: 0
                else -> ach.current
            }
            val unlocked = currVal >= ach.target
            ach.copy(current = currVal, isUnlocked = unlocked)
        }
        _achievements.value = updated
    }

    private fun loadDailyChallenges(): List<DailyChallenge> {
        return listOf(
            DailyChallenge(
                id = "daily_snake_30",
                title = "Snake Nibbler",
                description = "Score at least 30 points in Classic Snake",
                targetGameId = "snake",
                targetScore = 30,
                currentScore = 15,
                rewardXp = 150,
                rewardCoins = 50,
                isCompleted = false
            ),
            DailyChallenge(
                id = "daily_2048_512",
                title = "Tile Fusionist",
                description = "Merge up to a 512 tile in 2048",
                targetGameId = "game2048",
                targetScore = 512,
                currentScore = 512,
                rewardXp = 200,
                rewardCoins = 75,
                isCompleted = true,
                isClaimed = false
            ),
            DailyChallenge(
                id = "daily_brick_200",
                title = "Wall Demolisher",
                description = "Score 200+ points in Brick Breaker",
                targetGameId = "brickbreaker",
                targetScore = 200,
                currentScore = 220,
                rewardXp = 180,
                rewardCoins = 60,
                isCompleted = true,
                isClaimed = false
            ),
            DailyChallenge(
                id = "daily_reaction_250",
                title = "Lightning Reflex",
                description = "Hit under 260ms in Reaction Test",
                targetGameId = "reaction",
                targetScore = 260,
                currentScore = 240,
                rewardXp = 220,
                rewardCoins = 80,
                isCompleted = true,
                isClaimed = false
            )
        )
    }

    private fun loadAchievements(): List<Achievement> {
        return listOf(
            Achievement("first_play", "First Step", "Play your first game in GameBox", "🎮", 1, 1, 50, true),
            Achievement("ten_plays", "Arcade Regular", "Play a total of 10 mini-games", "🕹️", 10, 12, 100, true),
            Achievement("five_victories", "Rising Champion", "Achieve 5 game victories", "🏆", 5, 7, 150, true),
            Achievement("snake_pro", "Serpent Master", "Reach a score of 50 in Snake", "🐍", 50, 42, 200, false),
            Achievement("puzzle_master", "2048 Legend", "Reach the 2048 tile in 2048", "🔢", 2048, 2048, 300, true),
            Achievement("brick_smasher", "Brick Annihilator", "Clear 300 points in Brick Breaker", "🧱", 300, 380, 200, true),
            Achievement("reflex_king", "Sub-200ms Reaction", "Score under 200ms in Reaction Time", "⚡", 200, 230, 250, false)
        )
    }
}
