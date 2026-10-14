package com.example.ui.games.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameInfo
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GameDarkBorder
import com.example.ui.theme.GameDarkCard
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    gameInfo: GameInfo,
    score: Int,
    highScore: Int,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onShowInstructions: () -> Unit,
    onTogglePause: (() -> Unit)? = null,
    isPaused: Boolean = false,
    extraScoreLabel: String = "SCORE"
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0F1422),
            titleContentColor = TextPrimary
        ),
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("game_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to dashboard",
                    tint = CyberCyan
                )
            }
        },
        title = {
            Column {
                Text(
                    text = gameInfo.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = gameInfo.category.displayName,
                    fontSize = 12.sp,
                    color = CyberCyan
                )
            }
        },
        actions = {
            // Score pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E2538),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = extraScoreLabel,
                            fontSize = 9.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$score",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberCyan
                        )
                    }
                    if (highScore > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "BEST",
                                fontSize = 9.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$highScore",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD600)
                            )
                        }
                    }
                }
            }

            if (onTogglePause != null) {
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.testTag("pause_button")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = TextPrimary
                    )
                }
            }

            IconButton(
                onClick = onRestart,
                modifier = Modifier.testTag("restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = TextPrimary
                )
            }

            IconButton(
                onClick = onShowInstructions,
                modifier = Modifier.testTag("instructions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Instructions",
                    tint = TextSecondary
                )
            }
        }
    )
}

@Composable
fun GameOverDialog(
    score: Int,
    highScore: Int,
    isVictory: Boolean = false,
    earnedXp: Int = 50,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val isNewRecord = score > highScore && score > 0

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GameDarkCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, if (isVictory) NeonGreen else if (isNewRecord) CyberCyan else NeonPink, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    if (isVictory) NeonGreen else CyberCyan,
                                    if (isVictory) CyberCyan else NeonPurple
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVictory) "🏆" else if (isNewRecord) "⭐" else "💀",
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isVictory) "VICTORY!" else "GAME OVER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isVictory) NeonGreen else TextPrimary
                )

                if (isNewRecord) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2E2405)
                    ) {
                        Text(
                            text = "🎉 NEW PERSONAL RECORD! 🎉",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD600),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1422))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FINAL SCORE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("$score", fontSize = 24.sp, fontWeight = FontWeight.Black, color = CyberCyan)
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = GameDarkBorder
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HIGH SCORE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("${maxOf(score, highScore)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD600))
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = GameDarkBorder
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("XP EARNED", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("+$earnedXp", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("game_over_play_again"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = Color(0xFF00363D))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF00363D))
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("game_over_exit"),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(GameDarkBorder, GameDarkBorder)))
                ) {
                    Text("BACK TO DASHBOARD", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun InstructionsDialog(
    gameInfo: GameInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "How to Play: ${gameInfo.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                gameInfo.instructions.forEachIndexed { index, rule ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${index + 1}.",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = rule,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("GOT IT!", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = GameDarkCard,
        shape = RoundedCornerShape(20.dp)
    )
}
