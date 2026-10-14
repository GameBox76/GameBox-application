package com.example.ui.games.snake

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

enum class Direction { UP, DOWN, LEFT, RIGHT }
data class GridPoint(val x: Int, val y: Int)

@Composable
fun SnakeGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("snake")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["snake"] ?: 0

    val gridSize = 20
    var snake by remember {
        mutableStateOf(listOf(GridPoint(10, 10), GridPoint(9, 10), GridPoint(8, 10)))
    }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var nextDirection by remember { mutableStateOf(Direction.RIGHT) }
    var food by remember { mutableStateOf(GridPoint(15, 10)) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun spawnFood(currentSnake: List<GridPoint>): GridPoint {
        val occupied = currentSnake.toSet()
        val emptyCells = mutableListOf<GridPoint>()
        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                val pt = GridPoint(x, y)
                if (pt !in occupied) emptyCells.add(pt)
            }
        }
        return if (emptyCells.isNotEmpty()) emptyCells[Random.nextInt(emptyCells.size)] else GridPoint(0, 0)
    }

    fun restartGame() {
        snake = listOf(GridPoint(10, 10), GridPoint(9, 10), GridPoint(8, 10))
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        food = spawnFood(snake)
        score = 0
        isGameOver = false
        isPaused = false
        soundManager.playClick()
    }

    // Game loop
    LaunchedEffect(isGameOver, isPaused) {
        while (!isGameOver && !isPaused) {
            val speed = (200 - (score * 2)).coerceAtLeast(80)
            delay(speed.toLong())

            direction = nextDirection
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> GridPoint(head.x, head.y - 1)
                Direction.DOWN -> GridPoint(head.x, head.y + 1)
                Direction.LEFT -> GridPoint(head.x - 1, head.y)
                Direction.RIGHT -> GridPoint(head.x + 1, head.y)
            }

            // Wall collision
            if (newHead.x !in 0 until gridSize || newHead.y !in 0 until gridSize) {
                isGameOver = true
                soundManager.playGameOver()
                prefsManager.recordGamePlayed("snake", score, isVictory = score >= 50)
                break
            }

            // Tail collision
            if (newHead in snake) {
                isGameOver = true
                soundManager.playGameOver()
                prefsManager.recordGamePlayed("snake", score, isVictory = score >= 50)
                break
            }

            // Eat food
            if (newHead == food) {
                val newSnake = listOf(newHead) + snake
                snake = newSnake
                score += 10
                food = spawnFood(newSnake)
                soundManager.playScore()
            } else {
                snake = listOf(newHead) + snake.dropLast(1)
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
            // Game Board Canvas with swipe detector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameDarkSurface)
                    .border(2.dp, CyberCyanDim.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val (dx, dy) = dragAmount
                            if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                if (dx > 20 && direction != Direction.LEFT) nextDirection = Direction.RIGHT
                                else if (dx < -20 && direction != Direction.RIGHT) nextDirection = Direction.LEFT
                            } else {
                                if (dy > 20 && direction != Direction.UP) nextDirection = Direction.DOWN
                                else if (dy < -20 && direction != Direction.DOWN) nextDirection = Direction.UP
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width / gridSize

                    // Draw subtle grid dots
                    for (x in 0 until gridSize) {
                        for (y in 0 until gridSize) {
                            drawCircle(
                                color = Color(0xFF1E2638),
                                radius = 1.dp.toPx(),
                                center = Offset(x * cellSize + cellSize / 2, y * cellSize + cellSize / 2)
                            )
                        }
                    }

                    // Draw Food
                    drawCircle(
                        color = Color(0xFFFF2A85),
                        radius = (cellSize / 2) * 0.85f,
                        center = Offset(food.x * cellSize + cellSize / 2, food.y * cellSize + cellSize / 2)
                    )
                    // Glow inner
                    drawCircle(
                        color = Color.White,
                        radius = (cellSize / 2) * 0.35f,
                        center = Offset(food.x * cellSize + cellSize / 2, food.y * cellSize + cellSize / 2)
                    )

                    // Draw Snake
                    snake.forEachIndexed { index, pt ->
                        val isHead = index == 0
                        val color = if (isHead) CyberCyan else NeonGreen.copy(alpha = (1f - (index * 0.02f)).coerceAtLeast(0.4f))
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(pt.x * cellSize + 1.dp.toPx(), pt.y * cellSize + 1.dp.toPx()),
                            size = Size(cellSize - 2.dp.toPx(), cellSize - 2.dp.toPx()),
                            cornerRadius = CornerRadius(if (isHead) 6.dp.toPx() else 4.dp.toPx())
                        )
                    }
                }

                if (isPaused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PAUSED",
                            fontSize = 28.sp,
                            color = CyberCyan,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }

            // Virtual D-Pad for precise touch controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // UP
                IconButton(
                    onClick = {
                        if (direction != Direction.DOWN) nextDirection = Direction.UP
                        soundManager.playMove()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(GameDarkSurfaceVariant, CircleShape)
                        .border(1.dp, GameDarkBorder, CircleShape)
                        .testTag("dpad_up")
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = CyberCyan)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT
                    IconButton(
                        onClick = {
                            if (direction != Direction.RIGHT) nextDirection = Direction.LEFT
                            soundManager.playMove()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(GameDarkSurfaceVariant, CircleShape)
                            .border(1.dp, GameDarkBorder, CircleShape)
                            .testTag("dpad_left")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = CyberCyan)
                    }

                    // RIGHT
                    IconButton(
                        onClick = {
                            if (direction != Direction.LEFT) nextDirection = Direction.RIGHT
                            soundManager.playMove()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(GameDarkSurfaceVariant, CircleShape)
                            .border(1.dp, GameDarkBorder, CircleShape)
                            .testTag("dpad_right")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = CyberCyan)
                    }
                }

                // DOWN
                IconButton(
                    onClick = {
                        if (direction != Direction.UP) nextDirection = Direction.DOWN
                        soundManager.playMove()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(GameDarkSurfaceVariant, CircleShape)
                        .border(1.dp, GameDarkBorder, CircleShape)
                        .testTag("dpad_down")
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = CyberCyan)
                }
            }
        }
    }

    if (isGameOver) {
        GameOverDialog(
            score = score,
            highScore = highScore,
            isVictory = score >= 50,
            earnedXp = (score / 2).coerceAtLeast(20),
            onRestart = { restartGame() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
