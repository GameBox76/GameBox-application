package com.example.ui.games.connectfour

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

enum class Disc { EMPTY, RED, YELLOW }

@Composable
fun ConnectFourGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("connectfour")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["connectfour"] ?: 0

    val rows = 6
    val cols = 7

    var board by remember { mutableStateOf(List(rows) { List(cols) { Disc.EMPTY } }) }
    var currentDisc by remember { mutableStateOf(Disc.RED) }
    var isVsAi by remember { mutableStateOf(true) }
    var redWins by remember { mutableIntStateOf(0) }
    var yellowWins by remember { mutableIntStateOf(0) }
    var winningCoords by remember { mutableStateOf<List<Pair<Int, Int>>?>(null) }
    var isGameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<Disc?>(null) }
    var showInstructions by remember { mutableStateOf(false) }

    fun checkWin(b: List<List<Disc>>): Pair<Disc, List<Pair<Int, Int>>>? {
        // Horizontal
        for (r in 0 until rows) {
            for (c in 0 until cols - 3) {
                val d = b[r][c]
                if (d != Disc.EMPTY && d == b[r][c + 1] && d == b[r][c + 2] && d == b[r][c + 3]) {
                    return Pair(d, listOf(Pair(r, c), Pair(r, c + 1), Pair(r, c + 2), Pair(r, c + 3)))
                }
            }
        }
        // Vertical
        for (r in 0 until rows - 3) {
            for (c in 0 until cols) {
                val d = b[r][c]
                if (d != Disc.EMPTY && d == b[r + 1][c] && d == b[r + 2][c] && d == b[r + 3][c]) {
                    return Pair(d, listOf(Pair(r, c), Pair(r + 1, c), Pair(r + 2, c), Pair(r + 3, c)))
                }
            }
        }
        // Diagonal Down-Right
        for (r in 0 until rows - 3) {
            for (c in 0 until cols - 3) {
                val d = b[r][c]
                if (d != Disc.EMPTY && d == b[r + 1][c + 1] && d == b[r + 2][c + 2] && d == b[r + 3][c + 3]) {
                    return Pair(d, listOf(Pair(r, c), Pair(r + 1, c + 1), Pair(r + 2, c + 2), Pair(r + 3, c + 3)))
                }
            }
        }
        // Diagonal Up-Right
        for (r in 3 until rows) {
            for (c in 0 until cols - 3) {
                val d = b[r][c]
                if (d != Disc.EMPTY && d == b[r - 1][c + 1] && d == b[r - 2][c + 2] && d == b[r - 3][c + 3]) {
                    return Pair(d, listOf(Pair(r, c), Pair(r - 1, c + 1), Pair(r - 2, c + 2), Pair(r - 3, c + 3)))
                }
            }
        }
        return null
    }

    fun dropDisc(col: Int): Boolean {
        if (isGameOver) return false
        // Find bottom-most empty row in this column
        var targetRow = -1
        for (r in rows - 1 downTo 0) {
            if (board[r][col] == Disc.EMPTY) {
                targetRow = r
                break
            }
        }
        if (targetRow == -1) return false // Column is full

        soundManager.playMove()
        val mutable = board.map { it.toMutableList() }
        mutable[targetRow][col] = currentDisc
        board = mutable.map { it.toList() }

        val winResult = checkWin(board)
        if (winResult != null) {
            winningCoords = winResult.second
            winner = winResult.first
            isGameOver = true
            if (winner == Disc.RED) redWins++ else yellowWins++
            val isVictory = !isVsAi || winner == Disc.RED
            if (isVictory) soundManager.playWin() else soundManager.playGameOver()
            prefsManager.recordGamePlayed("connectfour", redWins * 20, isVictory = isVictory)
            return true
        }

        if (board.flatten().none { it == Disc.EMPTY }) {
            isGameOver = true
            winner = null
            soundManager.playGameOver()
            prefsManager.recordGamePlayed("connectfour", redWins * 20, isVictory = false)
            return true
        }

        currentDisc = if (currentDisc == Disc.RED) Disc.YELLOW else Disc.RED
        return true
    }

    // Smart AI for Connect Four
    fun aiMove() {
        // 1. Check if AI can win in 1 move
        for (c in 0 until cols) {
            val r = (rows - 1 downTo 0).firstOrNull { board[it][c] == Disc.EMPTY } ?: continue
            val temp = board.map { it.toMutableList() }
            temp[r][c] = Disc.YELLOW
            if (checkWin(temp)?.first == Disc.YELLOW) {
                dropDisc(c)
                return
            }
        }
        // 2. Block player if player can win in 1 move
        for (c in 0 until cols) {
            val r = (rows - 1 downTo 0).firstOrNull { board[it][c] == Disc.EMPTY } ?: continue
            val temp = board.map { it.toMutableList() }
            temp[r][c] = Disc.RED
            if (checkWin(temp)?.first == Disc.RED) {
                dropDisc(c)
                return
            }
        }
        // 3. Prefer center columns
        val prefCols = listOf(3, 2, 4, 1, 5, 0, 6)
        for (c in prefCols) {
            if (board[0][c] == Disc.EMPTY) {
                dropDisc(c)
                return
            }
        }
    }

    LaunchedEffect(currentDisc, isVsAi, isGameOver) {
        if (!isGameOver && isVsAi && currentDisc == Disc.YELLOW) {
            delay(400)
            aiMove()
        }
    }

    fun restartRound() {
        board = List(rows) { List(cols) { Disc.EMPTY } }
        currentDisc = Disc.RED
        winningCoords = null
        isGameOver = false
        winner = null
        soundManager.playClick()
    }

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = redWins,
                highScore = highScore,
                onBack = onBack,
                onRestart = { restartRound() },
                onShowInstructions = { showInstructions = true },
                extraScoreLabel = "WINS"
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
            // Mode Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameDarkSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = isVsAi,
                    onClick = {
                        isVsAi = true
                        restartRound()
                    },
                    label = { Text("Vs AI", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = Color(0xFF00363D)
                    ),
                    modifier = Modifier.testTag("c4_vs_ai")
                )
                FilterChip(
                    selected = !isVsAi,
                    onClick = {
                        isVsAi = false
                        restartRound()
                    },
                    label = { Text("2-Player Local", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPurple,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("c4_two_player")
                )
            }

            // Current Turn Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                val discColor = if (currentDisc == Disc.RED) Color(0xFFFF1744) else Color(0xFFFFD600)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(discColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isGameOver) {
                        when (winner) {
                            Disc.RED -> "Red Wins!"
                            Disc.YELLOW -> "Yellow Wins!"
                            else -> "Game Draw!"
                        }
                    } else {
                        if (isVsAi && currentDisc == Disc.YELLOW) "AI is dropping..."
                        else "${currentDisc.name}'s Turn - Tap column"
                    },
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }

            // 7x6 Connect 4 Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(7f / 6f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF003666))
                    .border(3.dp, CyberCyanDim, RoundedCornerShape(18.dp))
                    .padding(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    for (c in 0 until cols) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(enabled = !isGameOver && !(isVsAi && currentDisc == Disc.YELLOW)) {
                                    dropDisc(c)
                                }
                                .testTag("col_$c"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (r in 0 until rows) {
                                val disc = board[r][c]
                                val isWinningDisc = winningCoords?.contains(Pair(r, c)) == true

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (disc) {
                                                Disc.RED -> Color(0xFFFF1744)
                                                Disc.YELLOW -> Color(0xFFFFD600)
                                                Disc.EMPTY -> Color(0xFF081426)
                                            }
                                        )
                                        .border(
                                            if (isWinningDisc) 3.dp else 1.dp,
                                            if (isWinningDisc) NeonGreen else Color(0x33000000),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Scoreboard footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameDarkSurface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RED (P1)", fontSize = 11.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    Text("$redWins", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                }
                Divider(modifier = Modifier.height(32.dp).width(1.dp), color = GameDarkBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isVsAi) "YELLOW (AI)" else "YELLOW (P2)", fontSize = 11.sp, color = Color(0xFFFFD600), fontWeight = FontWeight.Bold)
                    Text("$yellowWins", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                }
            }
        }
    }

    if (isGameOver && winner != null) {
        val isUserVictory = !isVsAi || winner == Disc.RED
        GameOverDialog(
            score = redWins * 20,
            highScore = highScore,
            isVictory = isUserVictory,
            earnedXp = if (isUserVictory) 50 else 20,
            onRestart = { restartRound() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
