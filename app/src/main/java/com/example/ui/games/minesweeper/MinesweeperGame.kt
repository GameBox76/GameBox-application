package com.example.ui.games.minesweeper

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

data class MineCell(
    val r: Int,
    val c: Int,
    val isMine: Boolean = false,
    val neighborMines: Int = 0,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false
)

@Composable
fun MinesweeperGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("minesweeper")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["minesweeper"] ?: 0

    val rows = 8
    val cols = 8
    val totalMines = 10

    var grid by remember { mutableStateOf<List<List<MineCell>>>(emptyList()) }
    var isFlagMode by remember { mutableStateOf(false) }
    var isFirstClick by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }
    var isVictory by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var showInstructions by remember { mutableStateOf(false) }

    fun initEmptyBoard() {
        grid = List(rows) { r -> List(cols) { c -> MineCell(r, c) } }
        isFirstClick = true
        isGameOver = false
        isVictory = false
        timerSeconds = 0
        soundManager.playClick()
    }

    LaunchedEffect(Unit) {
        initEmptyBoard()
    }

    // Timer
    LaunchedEffect(isFirstClick, isGameOver) {
        if (!isFirstClick && !isGameOver) {
            while (true) {
                delay(1000)
                timerSeconds++
            }
        }
    }

    fun generateBoard(safeR: Int, safeC: Int): List<List<MineCell>> {
        val allCoords = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (r != safeR || c != safeC) {
                    allCoords.add(Pair(r, c))
                }
            }
        }
        val mineCoords = allCoords.shuffled().take(totalMines).toSet()

        val newGrid = MutableList(rows) { r ->
            MutableList(cols) { c ->
                val isMine = Pair(r, c) in mineCoords
                MineCell(r, c, isMine = isMine)
            }
        }

        // Count neighbor mines
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!newGrid[r][c].isMine) {
                    var count = 0
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until rows && nc in 0 until cols && newGrid[nr][nc].isMine) {
                                count++
                            }
                        }
                    }
                    newGrid[r][c] = newGrid[r][c].copy(neighborMines = count)
                }
            }
        }
        return newGrid.map { it.toList() }
    }

    fun revealCell(startR: Int, startC: Int) {
        if (isGameOver) return

        var currentGrid = grid
        if (isFirstClick) {
            currentGrid = generateBoard(startR, startC)
            isFirstClick = false
        }

        val cell = currentGrid[startR][startC]
        if (cell.isRevealed || cell.isFlagged) return

        if (cell.isMine) {
            // Detonated!
            soundManager.playGameOver()
            val revealedAll = currentGrid.map { row ->
                row.map { if (it.isMine) it.copy(isRevealed = true) else it }
            }
            grid = revealedAll
            isGameOver = true
            isVictory = false
            prefsManager.recordGamePlayed("minesweeper", 0, isVictory = false)
            return
        }

        // Flood fill reveal
        soundManager.playMove()
        val mutable = currentGrid.map { it.toMutableList() }.toMutableList()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(startR, startC))
        mutable[startR][startC] = mutable[startR][startC].copy(isRevealed = true)

        while (queue.isNotEmpty()) {
            val (r, c) = queue.removeFirst()
            if (mutable[r][c].neighborMines == 0) {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until rows && nc in 0 until cols) {
                            val neighbor = mutable[nr][nc]
                            if (!neighbor.isRevealed && !neighbor.isFlagged && !neighbor.isMine) {
                                mutable[nr][nc] = neighbor.copy(isRevealed = true)
                                if (neighbor.neighborMines == 0) {
                                    queue.add(Pair(nr, nc))
                                }
                            }
                        }
                    }
                }
            }
        }

        val finalGrid = mutable.map { it.toList() }
        grid = finalGrid

        // Check victory
        val unrevealedSafe = finalGrid.flatten().count { !it.isMine && !it.isRevealed }
        if (unrevealedSafe == 0) {
            soundManager.playWin()
            isVictory = true
            isGameOver = true
            val score = maxOf(50, 500 - timerSeconds * 2)
            prefsManager.recordGamePlayed("minesweeper", score, isVictory = true)
        }
    }

    fun toggleFlag(r: Int, c: Int) {
        if (isGameOver || grid[r][c].isRevealed) return
        val current = grid[r][c]
        val mutable = grid.map { it.toMutableList() }
        mutable[r][c] = current.copy(isFlagged = !current.isFlagged)
        grid = mutable.map { it.toList() }
        soundManager.playClick()
    }

    val flagsCount = grid.flatten().count { it.isFlagged }
    val remainingMines = (totalMines - flagsCount).coerceAtLeast(0)

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = remainingMines,
                highScore = highScore,
                onBack = onBack,
                onRestart = { initEmptyBoard() },
                onShowInstructions = { showInstructions = true },
                extraScoreLabel = "MINES"
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
            // Dashboard header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💣 ", fontSize = 16.sp)
                    Text("$remainingMines", fontWeight = FontWeight.Bold, color = NeonPink, fontSize = 16.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱️ ", fontSize = 16.sp)
                    Text("${timerSeconds}s", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 16.sp)
                }
            }

            // 8x8 Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameDarkSurface)
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until cols) {
                                val cell = if (grid.isNotEmpty()) grid[r][c] else MineCell(r, c)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                cell.isRevealed && cell.isMine -> NeonRed.copy(alpha = 0.5f)
                                                cell.isRevealed -> GameDarkSurfaceVariant
                                                else -> GameDarkCard
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (cell.isRevealed) GameDarkBorder else CyberCyanDim.copy(alpha = 0.4f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable(enabled = !isGameOver) {
                                            if (isFlagMode) {
                                                toggleFlag(r, c)
                                            } else {
                                                revealCell(r, c)
                                            }
                                        }
                                        .testTag("mine_${r}_$c"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cell.isFlagged && !cell.isRevealed) {
                                        Text("🚩", fontSize = 16.sp)
                                    } else if (cell.isRevealed) {
                                        if (cell.isMine) {
                                            Text("💣", fontSize = 16.sp)
                                        } else if (cell.neighborMines > 0) {
                                            Text(
                                                text = "${cell.neighborMines}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = getNumberColor(cell.neighborMines)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Mode Toggle (Reveal vs Flag)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isFlagMode = false },
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(end = 6.dp)
                        .testTag("mode_reveal"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isFlagMode) CyberCyan else GameDarkSurfaceVariant,
                        contentColor = if (!isFlagMode) Color(0xFF00363D) else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🔍 REVEAL", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isFlagMode = true },
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(start = 6.dp)
                        .testTag("mode_flag"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFlagMode) NeonPink else GameDarkSurfaceVariant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🚩 FLAG", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (isGameOver) {
        val score = if (isVictory) maxOf(50, 500 - timerSeconds * 2) else 0
        GameOverDialog(
            score = score,
            highScore = highScore,
            isVictory = isVictory,
            earnedXp = if (isVictory) 80 else 15,
            onRestart = { initEmptyBoard() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}

private fun getNumberColor(num: Int): Color {
    return when (num) {
        1 -> Color(0xFF40C4FF)
        2 -> Color(0xFF00E676)
        3 -> Color(0xFFFF5252)
        4 -> Color(0xFF7C4DFF)
        5 -> Color(0xFFFF9100)
        6 -> Color(0xFF00B0FF)
        7 -> Color(0xFFFF4081)
        else -> Color(0xFFE040FB)
    }
}
