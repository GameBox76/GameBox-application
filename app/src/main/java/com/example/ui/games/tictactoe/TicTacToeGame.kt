package com.example.ui.games.tictactoe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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

enum class Player { X, O, NONE }
enum class GameMode { VS_AI, TWO_PLAYER }
enum class AiDifficulty { EASY, HARD }

@Composable
fun TicTacToeGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("tictactoe")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["tictactoe"] ?: 0

    var board by remember { mutableStateOf(List(9) { Player.NONE }) }
    var currentPlayer by remember { mutableStateOf(Player.X) }
    var gameMode by remember { mutableStateOf(GameMode.VS_AI) }
    var aiDifficulty by remember { mutableStateOf(AiDifficulty.HARD) }
    var xWins by remember { mutableIntStateOf(0) }
    var oWins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }
    var winningIndices by remember { mutableStateOf<List<Int>?>(null) }
    var isGameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<Player?>(null) }
    var showInstructions by remember { mutableStateOf(false) }

    fun checkWin(b: List<Player>): Pair<Player, List<Int>>? {
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
            listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonals
        )
        for (combo in winCombos) {
            val (a, bIdx, c) = combo
            if (b[a] != Player.NONE && b[a] == b[bIdx] && b[a] == b[c]) {
                return Pair(b[a], combo)
            }
        }
        return null
    }

    fun minimax(b: MutableList<Player>, depth: Int, isMaximizing: Boolean): Int {
        val win = checkWin(b)
        if (win != null) {
            return if (win.first == Player.O) 10 - depth else depth - 10
        }
        if (b.none { it == Player.NONE }) return 0

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0 until 9) {
                if (b[i] == Player.NONE) {
                    b[i] = Player.O
                    val score = minimax(b, depth + 1, false)
                    b[i] = Player.NONE
                    bestScore = maxOf(score, bestScore)
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0 until 9) {
                if (b[i] == Player.NONE) {
                    b[i] = Player.X
                    val score = minimax(b, depth + 1, true)
                    b[i] = Player.NONE
                    bestScore = minOf(score, bestScore)
                }
            }
            return bestScore
        }
    }

    fun getBestMove(b: List<Player>): Int {
        if (aiDifficulty == AiDifficulty.EASY) {
            val empty = b.indices.filter { b[it] == Player.NONE }
            return if (empty.isNotEmpty()) empty[Random.nextInt(empty.size)] else -1
        }
        var bestScore = Int.MIN_VALUE
        var bestMove = -1
        val mutableB = b.toMutableList()
        for (i in 0 until 9) {
            if (mutableB[i] == Player.NONE) {
                mutableB[i] = Player.O
                val score = minimax(mutableB, 0, false)
                mutableB[i] = Player.NONE
                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }
        return bestMove
    }

    fun handleMove(index: Int) {
        if (board[index] != Player.NONE || isGameOver) return

        val newBoard = board.toMutableList()
        newBoard[index] = currentPlayer
        board = newBoard
        soundManager.playMove()

        val winResult = checkWin(newBoard)
        if (winResult != null) {
            winningIndices = winResult.second
            winner = winResult.first
            isGameOver = true
            if (winner == Player.X) xWins++ else oWins++
            val isVictory = (gameMode == GameMode.VS_AI && winner == Player.X) || gameMode == GameMode.TWO_PLAYER
            if (isVictory) soundManager.playWin() else soundManager.playGameOver()
            prefsManager.recordGamePlayed("tictactoe", xWins * 10, isVictory = winner == Player.X)
            return
        }

        if (newBoard.none { it == Player.NONE }) {
            isGameOver = true
            winner = null
            draws++
            soundManager.playGameOver()
            prefsManager.recordGamePlayed("tictactoe", xWins * 10, isVictory = false)
            return
        }

        currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
    }

    // AI Turn
    LaunchedEffect(currentPlayer, gameMode, isGameOver) {
        if (!isGameOver && gameMode == GameMode.VS_AI && currentPlayer == Player.O) {
            delay(350)
            val move = getBestMove(board)
            if (move != -1) {
                handleMove(move)
            }
        }
    }

    fun restartRound() {
        board = List(9) { Player.NONE }
        currentPlayer = Player.X
        winningIndices = null
        isGameOver = false
        winner = null
        soundManager.playClick()
    }

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = xWins,
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
                    selected = gameMode == GameMode.VS_AI,
                    onClick = {
                        gameMode = GameMode.VS_AI
                        restartRound()
                    },
                    label = { Text("Vs AI", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = Color(0xFF00363D)
                    ),
                    modifier = Modifier.testTag("mode_vs_ai")
                )
                FilterChip(
                    selected = gameMode == GameMode.TWO_PLAYER,
                    onClick = {
                        gameMode = GameMode.TWO_PLAYER
                        restartRound()
                    },
                    label = { Text("2-Player Local", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPurple,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("mode_two_player")
                )
            }

            // AI difficulty toggle if in VS_AI
            if (gameMode == GameMode.VS_AI) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Level:", color = TextSecondary, fontSize = 12.sp)
                    AssistChip(
                        onClick = {
                            aiDifficulty = if (aiDifficulty == AiDifficulty.HARD) AiDifficulty.EASY else AiDifficulty.HARD
                        },
                        label = { Text(aiDifficulty.name, fontWeight = FontWeight.Bold, color = if (aiDifficulty == AiDifficulty.HARD) NeonPink else NeonGreen) }
                    )
                }
            }

            // Current Turn Indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GameDarkCard,
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(GameDarkBorder, GameDarkBorder))),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val turnColor = if (currentPlayer == Player.X) CyberCyan else NeonPink
                    Text(
                        text = if (isGameOver) {
                            when {
                                winner != null -> "Player ${winner!!.name} Wins!"
                                else -> "It's a Draw!"
                            }
                        } else {
                            if (gameMode == GameMode.VS_AI && currentPlayer == Player.O) "AI is thinking..."
                            else "Player ${currentPlayer.name}'s Turn"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isGameOver && winner != null) NeonGreen else turnColor
                    )
                }
            }

            // 3x3 Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GameDarkSurface)
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 3) {
                                val idx = row * 3 + col
                                val player = board[idx]
                                val isWinningCell = winningIndices?.contains(idx) == true
                                val cellBg by animateColorAsState(
                                    targetValue = when {
                                        isWinningCell -> NeonGreen.copy(alpha = 0.25f)
                                        player != Player.NONE -> GameDarkSurfaceVariant
                                        else -> GameDarkCard
                                    }, label = "cellBg"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(cellBg)
                                        .border(
                                            1.5.dp,
                                            if (isWinningCell) NeonGreen else GameDarkBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable(enabled = player == Player.NONE && !isGameOver && !(gameMode == GameMode.VS_AI && currentPlayer == Player.O)) {
                                            handleMove(idx)
                                        }
                                        .testTag("cell_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (player) {
                                            Player.X -> "X"
                                            Player.O -> "O"
                                            Player.NONE -> ""
                                        },
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (player == Player.X) CyberCyan else NeonPink
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Match Scoreboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GameDarkSurface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("X (YOU)", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Text("$xWins", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                }
                Divider(modifier = Modifier.height(32.dp).width(1.dp), color = GameDarkBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DRAWS", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text("$draws", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                }
                Divider(modifier = Modifier.height(32.dp).width(1.dp), color = GameDarkBorder)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (gameMode == GameMode.VS_AI) "O (AI)" else "O (P2)", fontSize = 11.sp, color = NeonPink, fontWeight = FontWeight.Bold)
                    Text("$oWins", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                }
            }
        }
    }

    if (isGameOver && winner != null) {
        val isUserVictory = (gameMode == GameMode.VS_AI && winner == Player.X) || gameMode == GameMode.TWO_PLAYER
        GameOverDialog(
            score = xWins,
            highScore = highScore,
            isVictory = isUserVictory,
            earnedXp = if (isUserVictory) 40 else 15,
            onRestart = { restartRound() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
