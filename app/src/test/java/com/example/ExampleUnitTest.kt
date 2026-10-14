package com.example

import com.example.data.GameRepository
import com.example.model.GameCategory
import com.example.model.PlayerProfile
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGameRepositoryIntegrity() {
        val games = GameRepository.games
        assertTrue("GameBox must have at least 6 core games", games.size >= 6)
        assertEquals(10, games.size)

        // Verify each game has valid properties
        games.forEach { game ->
            assertTrue(game.title.isNotBlank())
            assertTrue(game.description.isNotBlank())
            assertTrue(game.instructions.isNotEmpty())
        }
    }

    @Test
    fun testCategoryFiltering() {
        val arcadeGames = GameRepository.filterGames("", GameCategory.ARCADE)
        assertTrue(arcadeGames.any { it.id == "snake" })
        assertTrue(arcadeGames.any { it.id == "brickbreaker" })
        assertTrue(arcadeGames.any { it.id == "flappy" })

        val puzzleGames = GameRepository.filterGames("", GameCategory.PUZZLE)
        assertTrue(puzzleGames.any { it.id == "game2048" })
        assertTrue(puzzleGames.any { it.id == "memory" })
        assertTrue(puzzleGames.any { it.id == "minesweeper" })

        val twoPlayerGames = GameRepository.filterGames("", GameCategory.TWO_PLAYER)
        assertTrue(twoPlayerGames.any { it.id == "tictactoe" })
        assertTrue(twoPlayerGames.any { it.id == "connectfour" })
    }

    @Test
    fun testSearchFiltering() {
        val results = GameRepository.filterGames("snake", GameCategory.ALL)
        assertEquals(1, results.size)
        assertEquals("snake", results[0].id)
    }

    @Test
    fun testPlayerProfileProgression() {
        val profile = PlayerProfile(currentXp = 450, level = 3, gamesPlayed = 10, totalVictories = 7)
        assertEquals(70, profile.winRatePercent)
        assertEquals(600, profile.xpForNextLevel)
        assertEquals(0.25f, profile.levelProgress, 0.01f)
    }
}
