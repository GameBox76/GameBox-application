package com.example.ui.games.game2048

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.random.Random

data class BoardState(val grid: List<List<Int>>, val score: Int)

@Composable
fun Game2048(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("game2048")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["game2048"] ?: 0

    var grid by remember { mutableStateOf(List(4) { List(4) { 0 } }) }
    var score by remember { mutableIntStateOf(0) }
    var history by remember { mutableStateOf<List<BoardState>>(emptyList()) }
    var isGameOver by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun spawnTile(currentGrid: List<List<Int>>): List<List<Int>> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (currentGrid[r][c] == 0) emptyCells.add(Pair(r, c))
            }
        }
        if (emptyCells.isEmpty()) return currentGrid

        val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
        val value = if (Random.nextFloat() < 0.9f) 2 else 4
        val mutable = currentGrid.map { it.toMutableList() }
        mutable[r][c] = value
        return mutable.map { it.toList() }
    }

    fun initGame() {
        var newGrid = List(4) { List(4) { 0 } }
        newGrid = spawnTile(newGrid)
        newGrid = spawnTile(newGrid)
        grid = newGrid
        score = 0
        history = emptyList()
        isGameOver = false
        hasWon = false
        soundManager.playClick()
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    fun checkGameOver(g: List<List<Int>>): Boolean {
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (g[r][c] == 0) return false
                if (c < 3 && g[r][c] == g[r][c + 1]) return false
                if (r < 3 && g[r][c] == g[r + 1][c]) return false
            }
        }
        return true
    }

    fun slideRow(row: List<Int>): Pair<List<Int>, Int> {
        val nonZero = row.filter { it != 0 }
        val result = mutableListOf<Int>()
        var points = 0
        var skip = false

        for (i in nonZero.indices) {
            if (skip) {
                skip = false
                continue
            }
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val merged = nonZero[i] * 2
                result.add(merged)
                points += merged
                skip = true
            } else {
                result.add(nonZero[i])
            }
        }
        while (result.size < 4) {
            result.add(0)
        }
        return Pair(result, points)
    }

    fun move(dir: String) {
        if (isGameOver) return

        val prevGrid = grid
        val prevScore = score
        var totalPoints = 0
        val newGrid = mutableListOf<List<Int>>()

        when (dir) {
            "LEFT" -> {
                for (r in 0 until 4) {
                    val (slid, pts) = slideRow(grid[r])
                    newGrid.add(slid)
                    totalPoints += pts
                }
            }
            "RIGHT" -> {
                for (r in 0 until 4) {
                    val (slid, pts) = slideRow(grid[r].reversed())
                    newGrid.add(slid.reversed())
                    totalPoints += pts
                }
            }
            "UP" -> {
                val transposed = List(4) { c -> List(4) { r -> grid[r][c] } }
                val slidTransposed = mutableListOf<List<Int>>()
                for (c in 0 until 4) {
                    val (slid, pts) = slideRow(transposed[c])
                    slidTransposed.add(slid)
                    totalPoints += pts
                }
                for (r in 0 until 4) {
                    newGrid.add(List(4) { c -> slidTransposed[c][r] })
                }
            }
            "DOWN" -> {
                val transposed = List(4) { c -> List(4) { r -> grid[r][c] } }
                val slidTransposed = mutableListOf<List<Int>>()
                for (c in 0 until 4) {
                    val (slid, pts) = slideRow(transposed[c].reversed())
                    slidTransposed.add(slid.reversed())
                    totalPoints += pts
                }
                for (r in 0 until 4) {
                    newGrid.add(List(4) { c -> slidTransposed[c][r] })
                }
            }
        }

        if (newGrid != prevGrid) {
            history = listOf(BoardState(prevGrid, prevScore)) + history.take(4)
            val spawned = spawnTile(newGrid)
            grid = spawned
            score += totalPoints

            if (totalPoints > 0) {
                soundManager.playScore()
            } else {
                soundManager.playMove()
            }

            // Check win
            if (!hasWon && spawned.any { row -> row.any { it >= 2048 } }) {
                hasWon = true
                soundManager.playWin()
            }

            // Check game over
            if (checkGameOver(spawned)) {
                isGameOver = true
                soundManager.playGameOver()
                prefsManager.recordGamePlayed("game2048", score, isVictory = hasWon || score >= 2048)
            }
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val last = history.first()
            grid = last.grid
            score = last.score
            history = history.drop(1)
            soundManager.playClick()
        }
    }

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = score,
                highScore = highScore,
                onBack = onBack,
                onRestart = { initGame() },
                onShowInstructions = { showInstructions = true }
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
            // Action bar: Undo & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Join numbers to get 2048!",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Button(
                    onClick = { undo() },
                    enabled = history.isNotEmpty() && !isGameOver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameDarkSurfaceVariant,
                        contentColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("undo_button")
                ) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo", fontSize = 12.sp)
                }
            }

            // 4x4 Grid Container with Swipe detection
            var dragAccumulatorX by remember { mutableFloatStateOf(0f) }
            var dragAccumulatorY by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameDarkSurface)
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (kotlin.math.abs(dragAccumulatorX) > kotlin.math.abs(dragAccumulatorY)) {
                                    if (dragAccumulatorX > 40) move("RIGHT")
                                    else if (dragAccumulatorX < -40) move("LEFT")
                                } else {
                                    if (dragAccumulatorY > 40) move("DOWN")
                                    else if (dragAccumulatorY < -40) move("UP")
                                }
                                dragAccumulatorX = 0f
                                dragAccumulatorY = 0f
                            },
                            onDrag = { _, dragAmount ->
                                dragAccumulatorX += dragAmount.x
                                dragAccumulatorY += dragAmount.y
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0 until 4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until 4) {
                                val value = grid[r][c]
                                val tileColor = getTileColor(value)
                                val textColor = if (value in 2..4) Color(0xFF1E293B) else Color.White

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tileColor)
                                        .testTag("tile_${r}_$c"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value > 0) {
                                        Text(
                                            text = "$value",
                                            fontSize = when {
                                                value < 100 -> 26.sp
                                                value < 1000 -> 22.sp
                                                else -> 18.sp
                                            },
                                            fontWeight = FontWeight.Black,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Directional Buttons for touch accessibility
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(
                    onClick = { move("UP") },
                    modifier = Modifier
                        .size(52.dp)
                        .background(GameDarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .testTag("btn_up")
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = CyberCyan)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                    IconButton(
                        onClick = { move("LEFT") },
                        modifier = Modifier
                            .size(52.dp)
                            .background(GameDarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("btn_left")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = CyberCyan)
                    }

                    IconButton(
                        onClick = { move("RIGHT") },
                        modifier = Modifier
                            .size(52.dp)
                            .background(GameDarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("btn_right")
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = CyberCyan)
                    }
                }

                IconButton(
                    onClick = { move("DOWN") },
                    modifier = Modifier
                        .size(52.dp)
                        .background(GameDarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .testTag("btn_down")
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
            isVictory = hasWon,
            earnedXp = (score / 15).coerceAtLeast(30),
            onRestart = { initGame() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}

private fun getTileColor(value: Int): Color {
    return when (value) {
        0 -> Color(0xFF161C2C)
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFF9C27B0)
        1024 -> Color(0xFF673AB7)
        2048 -> Color(0xFF00E5FF)
        else -> Color(0xFFFF2A85)
    }
}
