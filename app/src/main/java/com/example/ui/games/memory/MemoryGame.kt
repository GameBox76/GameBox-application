package com.example.ui.games.memory

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
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

data class MemoryCard(
    val id: Int,
    val icon: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("memory")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["memory"] ?: 0

    val allIcons = remember { listOf("🎮", "🕹️", "🚀", "👾", "💎", "⚡", "🏆", "🎯", "🔥", "⭐") }
    var gridType by remember { mutableStateOf("4x4") } // "3x4" (6 pairs) or "4x4" (8 pairs)
    var cards by remember { mutableStateOf<List<MemoryCard>>(emptyList()) }
    var flippedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var moves by remember { mutableIntStateOf(0) }
    var isBusy by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun initCards() {
        val numPairs = if (gridType == "3x4") 6 else 8
        val selectedIcons = allIcons.shuffled().take(numPairs)
        val deck = (selectedIcons + selectedIcons).shuffled().mapIndexed { index, icon ->
            MemoryCard(id = index, icon = icon)
        }
        cards = deck
        flippedIndices = emptyList()
        moves = 0
        isBusy = false
        isGameOver = false
        soundManager.playClick()
    }

    LaunchedEffect(gridType) {
        initCards()
    }

    fun handleCardClick(index: Int) {
        if (isBusy || cards[index].isFlipped || cards[index].isMatched || isGameOver) return

        soundManager.playMove()
        val newCards = cards.toMutableList()
        newCards[index] = newCards[index].copy(isFlipped = true)
        cards = newCards

        val newFlipped = flippedIndices + index
        if (newFlipped.size == 2) {
            moves++
            isBusy = true
            val (idx1, idx2) = Pair(newFlipped[0], newFlipped[1])

            if (cards[idx1].icon == cards[idx2].icon) {
                // Match!
                soundManager.playScore()
                newCards[idx1] = newCards[idx1].copy(isMatched = true)
                newCards[idx2] = newCards[idx2].copy(isMatched = true)
                cards = newCards
                flippedIndices = emptyList()
                isBusy = false

                // Check win
                if (newCards.all { it.isMatched }) {
                    isGameOver = true
                    soundManager.playWin()
                    val calculatedScore = maxOf(10, 1000 - (moves * 25))
                    prefsManager.recordGamePlayed("memory", calculatedScore, isVictory = true)
                }
            } else {
                // Not a match, flip back after brief pause
                flippedIndices = newFlipped
            }
        } else {
            flippedIndices = newFlipped
        }
    }

    // Flip back delay effect
    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            val (idx1, idx2) = Pair(flippedIndices[0], flippedIndices[1])
            if (cards[idx1].icon != cards[idx2].icon) {
                delay(800)
                val newCards = cards.toMutableList()
                newCards[idx1] = newCards[idx1].copy(isFlipped = false)
                newCards[idx2] = newCards[idx2].copy(isFlipped = false)
                cards = newCards
                flippedIndices = emptyList()
                isBusy = false
            }
        }
    }

    Scaffold(
        topBar = {
            GameTopBar(
                gameInfo = gameInfo,
                score = moves,
                highScore = highScore,
                onBack = onBack,
                onRestart = { initCards() },
                onShowInstructions = { showInstructions = true },
                extraScoreLabel = "MOVES"
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
            // Difficulty Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameDarkSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = gridType == "3x4",
                    onClick = { gridType = "3x4" },
                    label = { Text("Easy (12 Cards)", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = Color(0xFF00363D)
                    ),
                    modifier = Modifier.testTag("memory_easy")
                )
                FilterChip(
                    selected = gridType == "4x4",
                    onClick = { gridType = "4x4" },
                    label = { Text("Normal (16 Cards)", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPink,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("memory_normal")
                )
            }

            // Cards Grid
            val numCols = 4
            val numRows = if (gridType == "3x4") 3 else 4

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (gridType == "3x4") 4f / 3.2f else 1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GameDarkSurface)
                    .border(2.dp, GameDarkBorder, RoundedCornerShape(18.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (r in 0 until numRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until numCols) {
                                val idx = r * numCols + c
                                if (idx < cards.size) {
                                    val card = cards[idx]
                                    val isVisible = card.isFlipped || card.isMatched

                                    val rotation by animateFloatAsState(
                                        targetValue = if (isVisible) 180f else 0f,
                                        animationSpec = tween(300),
                                        label = "flip"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .graphicsLayer {
                                                rotationY = rotation
                                                cameraDistance = 12f * density
                                            }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    card.isMatched -> NeonGreen.copy(alpha = 0.2f)
                                                    isVisible -> GameDarkSurfaceVariant
                                                    else -> GameDarkCard
                                                }
                                            )
                                            .border(
                                                1.5.dp,
                                                when {
                                                    card.isMatched -> NeonGreen
                                                    isVisible -> CyberCyan
                                                    else -> GameDarkBorder
                                                },
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable(enabled = !card.isMatched && !card.isFlipped) {
                                                handleCardClick(idx)
                                            }
                                            .testTag("card_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (rotation > 90f) {
                                            Text(
                                                text = card.icon,
                                                fontSize = 28.sp,
                                                modifier = Modifier.graphicsLayer { rotationY = 180f }
                                            )
                                        } else {
                                            Text(
                                                text = "?",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyberCyanDim
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stats footer
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL MOVES", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("$moves", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val matchedCount = cards.count { it.isMatched } / 2
                        val totalPairs = cards.size / 2
                        Text("PAIRS MATCHED", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("$matchedCount / $totalPairs", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                    }
                }
            }
        }
    }

    if (isGameOver) {
        val calculatedScore = maxOf(10, 1000 - (moves * 25))
        GameOverDialog(
            score = calculatedScore,
            highScore = highScore,
            isVictory = true,
            earnedXp = 60,
            onRestart = { initCards() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
