package com.example.ui.games.flappy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

data class Pipe(
    var x: Float, // 0.0 to 1.0+
    val topHeight: Float, // relative 0.1 to 0.5
    val gap: Float = 0.32f, // gap size
    var isPassed: Boolean = false
)

@Composable
fun FlappyGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("flappy")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["flappy"] ?: 0

    // Bird position & physics (relative 0.0 to 1.0)
    var birdY by remember { mutableFloatStateOf(0.5f) }
    var velocityY by remember { mutableFloatStateOf(0f) }
    val gravity = 0.0016f
    val flapStrength = -0.024f
    val birdSizeRel = 0.06f

    var pipes by remember { mutableStateOf<List<Pipe>>(emptyList()) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun flap() {
        if (isGameOver || isPaused) return
        if (!isStarted) {
            isStarted = true
        }
        velocityY = flapStrength
        soundManager.playMove()
    }

    fun restartGame() {
        birdY = 0.5f
        velocityY = 0f
        pipes = listOf(
            Pipe(x = 1.0f, topHeight = Random.nextFloat() * 0.35f + 0.1f),
            Pipe(x = 1.6f, topHeight = Random.nextFloat() * 0.35f + 0.1f)
        )
        score = 0
        isGameOver = false
        isStarted = false
        isPaused = false
        soundManager.playClick()
    }

    LaunchedEffect(Unit) {
        restartGame()
    }

    // Game loop
    LaunchedEffect(isStarted, isGameOver, isPaused) {
        while (isStarted && !isGameOver && !isPaused) {
            delay(16) // ~60 FPS

            velocityY += gravity
            val newBirdY = birdY + velocityY

            // Ceiling or Ground collision
            if (newBirdY <= 0f || newBirdY >= 0.95f) {
                isGameOver = true
                soundManager.playGameOver()
                prefsManager.recordGamePlayed("flappy", score, isVictory = score >= 15)
                break
            }

            birdY = newBirdY

            // Move pipes
            val birdX = 0.25f
            val pipeSpeed = 0.007f
            val pipeWidth = 0.14f
            var hitPipe = false

            val updatedPipes = pipes.map { pipe ->
                val newX = pipe.x - pipeSpeed
                // Check if bird passed pipe
                if (!pipe.isPassed && newX + pipeWidth < birdX) {
                    score++
                    soundManager.playScore()
                    pipe.copy(x = newX, isPassed = true)
                } else {
                    // Check collision
                    if (birdX + (birdSizeRel / 2) >= newX && birdX - (birdSizeRel / 2) <= newX + pipeWidth) {
                        val bottomPipeTop = pipe.topHeight + pipe.gap
                        if (birdY - (birdSizeRel / 2) <= pipe.topHeight || birdY + (birdSizeRel / 2) >= bottomPipeTop) {
                            hitPipe = true
                        }
                    }
                    pipe.copy(x = newX)
                }
            }

            if (hitPipe) {
                isGameOver = true
                soundManager.playGameOver()
                prefsManager.recordGamePlayed("flappy", score, isVictory = score >= 15)
                break
            }

            // Recycle pipes
            val filteredPipes = updatedPipes.filter { it.x > -0.2f }.toMutableList()
            if (filteredPipes.size < 2) {
                val lastX = filteredPipes.lastOrNull()?.x ?: 1.0f
                filteredPipes.add(
                    Pipe(x = lastX + 0.6f, topHeight = Random.nextFloat() * 0.35f + 0.1f)
                )
            }
            pipes = filteredPipes
        }
    }

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = score,
                highScore = highScore,
                onBack = onBack,
                onRestart = { restartGame() },
                onShowInstructions = { showInstructions = true },
                onTogglePause = { isPaused = !isPaused },
                isPaused = isPaused
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
            // Live score pill
            Text(
                text = if (!isStarted) "TAP TO FLY" else "SCORE: $score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = CyberCyan
            )

            // Game Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF070B14))
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(20.dp))
                    .clickable { flap() }
                    .testTag("flappy_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw pipes
                    val pW = 0.14f * w
                    pipes.forEach { pipe ->
                        val pX = pipe.x * w
                        val topH = pipe.topHeight * h
                        val gapH = pipe.gap * h
                        val botY = topH + gapH

                        // Top Pipe
                        drawRoundRect(
                            color = NeonGreen,
                            topLeft = Offset(pX, 0f),
                            size = Size(pW, topH),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        // Top Pipe rim
                        drawRoundRect(
                            color = CyberCyanDim,
                            topLeft = Offset(pX - 4.dp.toPx(), topH - 12.dp.toPx()),
                            size = Size(pW + 8.dp.toPx(), 12.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        // Bottom Pipe
                        drawRoundRect(
                            color = NeonGreen,
                            topLeft = Offset(pX, botY),
                            size = Size(pW, h - botY),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        // Bottom Pipe rim
                        drawRoundRect(
                            color = CyberCyanDim,
                            topLeft = Offset(pX - 4.dp.toPx(), botY),
                            size = Size(pW + 8.dp.toPx(), 12.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }

                    // Draw Player (Flying Cyber Box)
                    val bSize = birdSizeRel * w
                    val bX = 0.25f * w - (bSize / 2)
                    val bY = birdY * h - (bSize / 2)

                    drawRoundRect(
                        color = Color(0xFFFF9100),
                        topLeft = Offset(bX, bY),
                        size = Size(bSize, bSize),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    // Glow eye
                    drawCircle(
                        color = Color.White,
                        radius = bSize * 0.18f,
                        center = Offset(bX + bSize * 0.7f, bY + bSize * 0.35f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = bSize * 0.09f,
                        center = Offset(bX + bSize * 0.72f, bY + bSize * 0.35f)
                    )
                }

                if (!isStarted && !isGameOver) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GameDarkCard.copy(alpha = 0.9f),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(CyberCyan, CyberCyan)))
                        ) {
                            Text(
                                "TAP ANYWHERE TO FLAP",
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Big Tap Button for accessibility
            Button(
                onClick = { flap() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(bottom = 6.dp)
                    .testTag("flappy_tap_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("TAP / FLAP", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF00363D))
            }
        }
    }

    if (isGameOver) {
        GameOverDialog(
            score = score,
            highScore = highScore,
            isVictory = score >= 15,
            earnedXp = (score * 10).coerceAtLeast(20),
            onRestart = { restartGame() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
