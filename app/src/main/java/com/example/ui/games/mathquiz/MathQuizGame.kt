package com.example.ui.games.mathquiz

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

data class MathQuestion(
    val equationText: String,
    val correctAnswer: Int,
    val options: List<Int>
)

@Composable
fun MathQuizGame(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gameInfo = remember { GameRepository.getGameById("mathquiz")!! }
    val profile by prefsManager.profile.collectAsState()
    val highScore = profile.highScores["mathquiz"] ?: 0

    var timeLeft by remember { mutableIntStateOf(30) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf<MathQuestion?>(null) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isGameOver by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    fun generateQuestion(): MathQuestion {
        val op = listOf("+", "-", "*").random()
        val num1: Int
        val num2: Int
        val answer: Int

        when (op) {
            "+" -> {
                num1 = Random.nextInt(5, 50)
                num2 = Random.nextInt(5, 50)
                answer = num1 + num2
            }
            "-" -> {
                num1 = Random.nextInt(15, 60)
                num2 = Random.nextInt(5, num1)
                answer = num1 - num2
            }
            "*" -> {
                num1 = Random.nextInt(3, 12)
                num2 = Random.nextInt(3, 12)
                answer = num1 * num2
            }
            else -> {
                num1 = 10
                num2 = 10
                answer = 20
            }
        }

        val wrongOptions = mutableSetOf<Int>()
        var attempts = 0
        while (wrongOptions.size < 3 && attempts < 20) {
            attempts++
            val offset = listOf(-10, -5, -2, -1, 1, 2, 5, 10).random()
            val candidate = answer + offset
            if (candidate != answer && candidate >= 0) wrongOptions.add(candidate)
        }
        var fallback = 1
        while (wrongOptions.size < 3) {
            val candidate = answer + fallback
            if (candidate != answer) wrongOptions.add(candidate)
            fallback++
        }

        val allOptions = (wrongOptions.toList() + answer).shuffled()
        return MathQuestion("$num1 $op $num2 = ?", answer, allOptions)
    }

    fun startGame() {
        score = 0
        streak = 0
        maxStreak = 0
        timeLeft = 30
        isGameOver = false
        selectedOption = null
        currentQuestion = generateQuestion()
        soundManager.playClick()
    }

    LaunchedEffect(Unit) {
        startGame()
    }

    // Countdown Timer Loop
    LaunchedEffect(isGameOver) {
        while (!isGameOver && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft <= 0) {
                isGameOver = true
                soundManager.playGameOver()
                prefsManager.recordGamePlayed("mathquiz", score, isVictory = score >= 300)
                break
            }
        }
    }

    fun handleAnswer(choice: Int) {
        if (isGameOver || selectedOption != null) return
        val q = currentQuestion ?: return
        selectedOption = choice

        if (choice == q.correctAnswer) {
            val mult = (1 + (streak / 3)).coerceAtMost(4)
            val gainedPoints = 10 * mult
            score += gainedPoints
            streak++
            maxStreak = maxOf(maxStreak, streak)
            timeLeft = (timeLeft + 2).coerceAtMost(45)
            soundManager.playScore()
        } else {
            streak = 0
            timeLeft = (timeLeft - 3).coerceAtLeast(0)
            soundManager.playGameOver()
        }

        // Delay to show green/red feedback then next question
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).run {
            // schedule next question
        }
    }

    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            delay(300)
            selectedOption = null
            if (!isGameOver) {
                currentQuestion = generateQuestion()
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
                onRestart = { startGame() },
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
            // Stats bar: Timer, Streak multiplier
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏳ ", fontSize = 16.sp)
                    Text(
                        "${timeLeft}s",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (timeLeft <= 5) NeonRed else CyberCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥 ", fontSize = 16.sp)
                    val mult = (1 + (streak / 3)).coerceAtMost(4)
                    Text(
                        "${streak} Streak (${mult}x)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (streak >= 3) Color(0xFFFFD600) else TextSecondary
                    )
                }
            }

            // Question Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(2.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SOLVE FAST", fontSize = 12.sp, color = NeonPurple, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentQuestion?.equationText ?: "Loading...",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            }

            // 4 Options Grid (2x2)
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val q = currentQuestion
                val options = q?.options ?: listOf(0, 0, 0, 0)

                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0 until 2) {
                            val idx = row * 2 + col
                            val choice = options[idx]
                            val isSelected = selectedOption == choice
                            val isCorrect = choice == q?.correctAnswer

                            val btnBg by animateColorAsState(
                                targetValue = when {
                                    isSelected && isCorrect -> NeonGreen
                                    isSelected && !isCorrect -> NeonRed
                                    else -> GameDarkSurfaceVariant
                                }, label = "mathBtn"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(btnBg)
                                    .border(
                                        1.5.dp,
                                        if (isSelected && isCorrect) NeonGreen else GameDarkBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = selectedOption == null && !isGameOver) {
                                        handleAnswer(choice)
                                    }
                                    .testTag("math_option_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$choice",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isGameOver) {
        GameOverDialog(
            score = score,
            highScore = highScore,
            isVictory = score >= 300,
            earnedXp = (score / 4).coerceAtLeast(30),
            onRestart = { startGame() },
            onExit = onBack
        )
    }

    if (showInstructions) {
        InstructionsDialog(gameInfo = gameInfo, onDismiss = { showInstructions = false })
    }
}
