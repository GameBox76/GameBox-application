package com.example.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.PreferencesManager
import com.example.data.SoundManager
import com.example.ui.dashboard.AVATAR_LIST
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val profile by prefsManager.profile.collectAsState()
    val achievements by prefsManager.achievements.collectAsState()

    var isEditingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(profile.username) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GameDarkBackground)
            )
        },
        containerColor = GameDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GameDarkBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CyberCyan, NeonPurple)))
                                .border(2.dp, CyberCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AVATAR_LIST.getOrElse(profile.avatarIndex) { "🤖" },
                                fontSize = 42.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Avatar Picker Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            AVATAR_LIST.forEachIndexed { index, emoji ->
                                val isSelected = profile.avatarIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else GameDarkSurfaceVariant)
                                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) CyberCyan else GameDarkBorder, CircleShape)
                                        .clickable {
                                            prefsManager.updateAvatar(index)
                                            soundManager.playClick()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Username edit
                        if (isEditingName) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                IconButton(onClick = {
                                    if (nameInput.isNotBlank()) {
                                        prefsManager.updateProfileName(nameInput)
                                        soundManager.playClick()
                                    }
                                    isEditingName = false
                                }) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = NeonGreen)
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.username,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                IconButton(onClick = {
                                    nameInput = profile.username
                                    isEditingName = true
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit name", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Level & XP Bar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E2738),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "LEVEL ${profile.level} GAMER",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("XP Progress", fontSize = 11.sp, color = TextSecondary)
                                Text("${profile.currentXp % 200} / 200 XP", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { profile.levelProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = CyberCyan,
                                trackColor = Color(0xFF1E2738)
                            )
                        }
                    }
                }
            }

            // Career Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("GAMES", "${profile.gamesPlayed}", "🎮", Modifier.weight(1f))
                    StatCard("VICTORIES", "${profile.totalVictories}", "🏆", Modifier.weight(1f))
                    StatCard("WIN RATE", "${profile.winRatePercent}%", "📈", Modifier.weight(1f))
                }
            }

            // High Scores Section
            item {
                Text(
                    text = "Personal Best Records",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GameRepository.games.forEach { game ->
                            val score = profile.highScores[game.id] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(game.title, fontSize = 14.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (score > 0) Color(0xFF28220A) else GameDarkSurfaceVariant
                                ) {
                                    Text(
                                        text = if (score > 0) "$score PTS" else "NOT PLAYED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (score > 0) Color(0xFFFFD600) else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Achievements Section
            item {
                Text(
                    text = "Achievements & Badges",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(achievements) { ach ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.isUnlocked) Color(0xFF132030) else GameDarkSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (ach.isUnlocked) CyberCyanDim else GameDarkBorder,
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (ach.isUnlocked) Color(0xFF0F3244) else GameDarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ach.iconEmoji, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                if (ach.isUnlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("✓ UNLOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                }
                            }
                            Text(ach.description, fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ach.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (ach.isUnlocked) NeonGreen else CyberCyan,
                                trackColor = Color(0xFF1E2738)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "+${ach.rewardXp} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ach.isUnlocked) NeonGreen else TextMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
        modifier = modifier.border(1.dp, GameDarkBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = CyberCyan)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
    }
}
