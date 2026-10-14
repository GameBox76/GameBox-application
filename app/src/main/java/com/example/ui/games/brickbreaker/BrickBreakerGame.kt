package com.example.ui.games.brickbreaker

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.cos
import kotlin.math.sin

data class Brick(
    val row: Int,
    val col: Int,
    val color: Color,
    val points: Int,
    var isDestroyed: Boolean = false
)

@Composable
fun BrickBreakerGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("brickbreaker")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["brickbreaker"] ?: 0

    val brickRows = 5
    val brickCols = 6
    val brickColors = remember {
        listOf(
            Color(0xFFFF1744), // Red
            Color(0xFFFF9100), // Orange
            Color(0xFFFFEA00), // Yellow
            Color(0xFF00E676), // Green
            Color(0xFF00E5FF)  // Cyan
        )
    }

    var bricks by remember {
        mutableStateOf(
            List(brickRows * brickCols) { idx ->
                val r = idx / brickCols
                val c = idx % brickCols
                Brick(r, c, brickColors[r % brickColors.size], points = (5 - r) * 10)
            }
        )
    }

    // Relative positions (0.0 to 1.0)
    var paddleX by remember { mutableFloatStateOf(0.5f) }
    val paddleWidthRel = 0.26f
    val paddleHeightRel = 0.035f
    val paddleYRel = 0.90f

    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballY by remember { mutableFloatStateOf(0.70f) }
    var ballVx by remember { mutableFloatStateOf(0.007f) }
    var ballVy by remember { mutableFloatStateOf(-0.012f) }
    val ballRadiusRel = 0.022f

    var lives by remember { mutableIntStateOf(3) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun resetBall() {
        ballX = paddleX
        ballY = paddleYRel - 0.05f
        ballVx = if (kotlin.random.Random.nextBoolean()) 0.008f else -0.008f
        ballVy = -0.013f
    }

    fun restartGame() {
        bricks = List(brickRows * brickCols) { idx ->
            val r = idx / brickCols
            val c = idx % brickCols
            Brick(r, c, brickColors[r % brickColors.size], points = (5 - r) * 10)
        }
        paddleX = 0.5f
        resetBall()
        lives = 3
        score = 0
        isGameOver = false
        isVictory = false
        isPaused = false
        soundManager.playClick()
    }

    // Physics Engine Loop
    LaunchedEffect(isGameOver, isPaused) {
        while (!isGameOver && !isPaused) {
            delay(16) // ~60 FPS

            var newBallX = ballX + ballVx
            var newBallY = ballY + ballVy

            // Wall Collisions
            if (newBallX - ballRadiusRel <= 0f) {
                newBallX = ballRadiusRel
                ballVx = -ballVx
                soundManager.playHit()
            } else if (newBallX + ballRadiusRel >= 1f) {
                newBallX = 1f - ballRadiusRel
                ballVx = -ballVx
                soundManager.playHit()
            }

            if (newBallY - ballRadiusRel <= 0f) {
                newBallY = ballRadiusRel
                ballVy = -ballVy
                soundManager.playHit()
            }

            // Paddle Collision
            val paddleLeft = paddleX - (paddleWidthRel / 2)
            val paddleRight = paddleX + (paddleWidthRel / 2)
            val paddleTop = paddleYRel
            val paddleBottom = paddleYRel + paddleHeightRel

            if (newBallY + ballRadiusRel >= paddleTop &&
                newBallY - ballRadiusRel <= paddleBottom &&
                newBallX >= paddleLeft && newBallX <= paddleRight
            ) {
                // Determine hit position relative to paddle center (-1 to 1)
                val hitOffset = ((newBallX - paddleX) / (paddleWidthRel / 2)).coerceIn(-1f, 1f)
                val speed = kotlin.math.sqrt(ballVx * ballVx + ballVy * ballVy).coerceAtLeast(0.014f)
                val maxBounceAngle = Math.toRadians(60.0).toFloat()
                val bounceAngle = hitOffset * maxBounceAngle

                ballVx = (speed * sin(bounceAngle)).toFloat()
                ballVy = (-speed * cos(bounceAngle)).toFloat().coerceAtMost(-0.008f)
                newBallY = paddleTop - ballRadiusRel
                soundManager.playMove()
            }

            // Bricks Collision
            val brickAreaTop = 0.08f
            val brickAreaBottom = 0.38f
            val brickHeight = (brickAreaBottom - brickAreaTop) / brickRows
            val brickWidth = 1f / brickCols

            if (newBallY <= brickAreaBottom && newBallY >= brickAreaTop) {
                var hitBrick = false
                val updatedBricks = bricks.map { brick ->
                    if (!brick.isDestroyed && !hitBrick) {
                        val bLeft = brick.col * brickWidth
                        val bRight = bLeft + brickWidth
                        val bTop = brickAreaTop + brick.row * brickHeight
                        val bBottom = bTop + brickHeight

                        if (newBallX + ballRadiusRel >= bLeft && newBallX - ballRadiusRel <= bRight &&
                            newBallY + ballRadiusRel >= bTop && newBallY - ballRadiusRel <= bBottom
                        ) {
                            hitBrick = true
                            score += brick.points
                            ballVy = -ballVy
                            soundManager.playScore()
                            brick.copy(isDestroyed = true)
                        } else brick
                    } else brick
                }

                if (hitBrick) {
                    bricks = updatedBricks
                    if (updatedBricks.all { it.isDestroyed }) {
                        isVictory = true
                        isGameOver = true
                        soundManager.playWin()
                        prefsManager.recordGamePlayed("brickbreaker", score, isVictory = true)
                    }
                }
            }

            // Bottom Boundary (Fall)
            if (newBallY >= 1.05f) {
                lives--
                soundManager.playGameOver()
                if (lives <= 0) {
                    isGameOver = true
                    prefsManager.recordGamePlayed("brickbreaker", score, isVictory = isVictory)
                } else {
                    resetBall()
                }
            } else {
                ballX = newBallX
                ballY = newBallY
            }
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
            // Lives Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("LIVES: ", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    repeat(3) { i ->
                        Text(
                            text = if (i < lives) "❤️" else "🖤",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                Text("SCORE: $score", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            }

            // Game Playfield Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameDarkSurface)
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val canvasWidth = size.width.toFloat()
                            if (canvasWidth > 0) {
                                paddleX = (paddleX + (dragAmount.x / canvasWidth)).coerceIn(
                                    paddleWidthRel / 2,
                                    1f - (paddleWidthRel / 2)
                                )
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Bricks
                    val brickAreaTop = 0.08f * h
                    val brickAreaBottom = 0.38f * h
                    val bH = (brickAreaBottom - brickAreaTop) / brickRows
                    val bW = w / brickCols

                    bricks.forEach { brick ->
                        if (!brick.isDestroyed) {
                            drawRoundRect(
                                color = brick.color,
                                topLeft = Offset(brick.col * bW + 2.dp.toPx(), brickAreaTop + brick.row * bH + 2.dp.toPx()),
                                size = Size(bW - 4.dp.toPx(), bH - 4.dp.toPx()),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                    }

                    // Draw Paddle
                    val padW = paddleWidthRel * w
                    val padH = paddleHeightRel * h
                    val padX = (paddleX * w) - (padW / 2)
                    val padY = paddleYRel * h
                    drawRoundRect(
                        color = CyberCyan,
                        topLeft = Offset(padX, padY),
                        size = Size(padW, padH),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    // Draw Ball
                    val bRadius = ballRadiusRel * w
                    drawCircle(
                        color = Color.White,
                        radius = bRadius,
                        center = Offset(ballX * w, ballY * h)
                    )
                }

                if (isPaused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PAUSED", fontSize = 28.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Paddle control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        paddleX = (paddleX - 0.08f).coerceAtLeast(paddleWidthRel / 2)
                        soundManager.playMove()
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(end = 8.dp)
                        .testTag("paddle_left"),
                    colors = ButtonDefaults.buttonColors(containerColor = GameDarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Move Left", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LEFT", fontWeight = FontWeight.Bold, color = CyberCyan)
                }

                Button(
                    onClick = {
                        paddleX = (paddleX + 0.08f).coerceAtMost(1f - paddleWidthRel / 2)
                        soundManager.playMove()
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                        .testTag("paddle_right"),
                    colors = ButtonDefaults.buttonColors(containerColor = GameDarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RIGHT", fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Move Right", tint = CyberCyan)
                }
            }
        }
    }

    if (isGameOver) {
        GameOverDialog(
            score = score,
            highScore = highScore,
            isVictory = isVictory,
            earnedXp = (score / 5).coerceAtLeast(25),
            onRestart = { restartGame() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
