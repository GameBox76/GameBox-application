package com.example.ui.games.reaction

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.PreferencesManager
import com.example.data.SoundManager
import com.example.ui.games.common.GameOverDialog
import com.example.ui.games.common.GameTopBar
import com.example.ui.games.common.InstructionsDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class ReactionState { IDLE, WAITING, READY, TOO_EARLY, ROUND_OVER }

@Composable
fun ReactionGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("reaction")!! }
    val profile by prefsManager.profile.collectAsState()
    val bestScore = profile.highScores["reaction"] ?: 0

    var state by remember { mutableStateOf(ReactionState.IDLE) }
    var startTime by remember { mutableLongStateOf(0L) }
    var currentReactionMs by remember { mutableLongStateOf(0L) }
    var roundTimes by remember { mutableStateOf<List<Long>>(emptyList()) }
    var round by remember { mutableIntStateOf(1) }
    val totalRounds = 5
    var isGameOver by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun startWaiting() {
        state = ReactionState.WAITING
        soundManager.playClick()
    }

    LaunchedEffect(state) {
        if (state == ReactionState.WAITING) {
            // Wait random delay between 1.5s and 4.0s
            val waitMs = Random.nextLong(1500, 4000)
            delay(waitMs)
            if (state == ReactionState.WAITING) {
                startTime = System.currentTimeMillis()
                state = ReactionState.READY
                soundManager.vibrateMedium()
            }
        }
    }

    fun handleTap() {
        when (state) {
            ReactionState.IDLE -> {
                startWaiting()
            }
            ReactionState.WAITING -> {
                // Tapped too early!
                state = ReactionState.TOO_EARLY
                soundManager.playGameOver()
            }
            ReactionState.READY -> {
                val elapsed = System.currentTimeMillis() - startTime
                currentReactionMs = elapsed
                soundManager.playScore()
                val updated = roundTimes + elapsed
                roundTimes = updated
                state = ReactionState.ROUND_OVER

                if (updated.size >= totalRounds) {
                    val avg = updated.average().toLong()
                    isGameOver = true
                    soundManager.playWin()
                    val calculatedScore = maxOf(50, (1000 - avg).toInt())
                    prefsManager.recordGamePlayed("reaction", calculatedScore, isVictory = avg < 300)
                }
            }
            ReactionState.TOO_EARLY -> {
                startWaiting()
            }
            ReactionState.ROUND_OVER -> {
                round++
                startWaiting()
            }
        }
    }

    fun resetGame() {
        state = ReactionState.IDLE
        roundTimes = emptyList()
        round = 1
        currentReactionMs = 0L
        isGameOver = false
        soundManager.playClick()
    }

    val avgReaction = if (roundTimes.isNotEmpty()) roundTimes.average().toLong() else 0L

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = if (currentReactionMs > 0) currentReactionMs.toInt() else 0,
                highScore = bestScore,
                onBack = onBack,
                onRestart = { resetGame() },
                onShowInstructions = { showInstructions = true },
                extraScoreLabel = "MS"
            )
        },
        containerColor = GameDarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Round Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ROUND: $round / $totalRounds", fontWeight = FontWeight.Bold, color = CyberCyan)
                if (roundTimes.isNotEmpty()) {
                    Text("AVG: ${avgReaction}ms", fontWeight = FontWeight.Bold, color = Color(0xFFFFD600))
                }
            }

            // Central Touch Canvas Area
            val areaBgColor = when (state) {
                ReactionState.IDLE -> GameDarkSurface
                ReactionState.WAITING -> Color(0xFF8B0000) // Red
                ReactionState.READY -> Color(0xFF00C853) // Green
                ReactionState.TOO_EARLY -> Color(0xFFFF6D00) // Orange
                ReactionState.ROUND_OVER -> GameDarkSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(areaBgColor)
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(24.dp))
                    .clickable { handleTap() }
                    .testTag("reaction_tap_surface"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    when (state) {
                        ReactionState.IDLE -> {
                            Text("⚡", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("TAP TO START", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Wait for the screen to turn GREEN, then tap as fast as you can!", color = TextSecondary, textAlign = TextAlign.Center)
                        }
                        ReactionState.WAITING -> {
                            Text("🛑", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("WAIT FOR GREEN...", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Do not tap yet!", color = Color.White.copy(alpha = 0.8f))
                        }
                        ReactionState.READY -> {
                            Text("🟢", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("TAP NOW!", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        ReactionState.TOO_EARLY -> {
                            Text("⚠️", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("TOO EARLY!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to try this round again.", color = Color.White.copy(alpha = 0.9f))
                        }
                        ReactionState.ROUND_OVER -> {
                            Text("${currentReactionMs}ms", fontSize = 48.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(getRank(currentReactionMs), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD600))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tap anywhere for next round", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Round History Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0 until totalRounds) {
                    val time = roundTimes.getOrNull(i)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (time != null) GameDarkSurfaceVariant else GameDarkSurface,
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(GameDarkBorder, GameDarkBorder)))
                    ) {
                        Text(
                            text = if (time != null) "${time}ms" else "R${i + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (time != null) CyberCyan else TextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    if (isGameOver) {
        val calculatedScore = maxOf(50, (1000 - avgReaction).toInt())
        GameOverDialog(
            score = avgReaction.toInt(),
            highScore = bestScore,
            isVictory = avgReaction < 300,
            earnedXp = if (avgReaction < 250) 80 else 40,
            onRestart = { resetGame() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}

private fun getRank(ms: Long): String {
    return when {
        ms < 200 -> "⚡ CYBER GODLIKE REFLEXES!"
        ms < 250 -> "🏆 ESPORTS PRO REFLEXES!"
        ms < 300 -> "🎯 QUICK REFLEXES"
        ms < 380 -> "👍 SOLID GAMER"
        else -> "🐢 SLOTH SPEED"
    }
}
