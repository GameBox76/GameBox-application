package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.data.SoundManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    soundManager: SoundManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val soundEnabled by prefsManager.soundEnabled.collectAsState()
    val hapticsEnabled by prefsManager.hapticsEnabled.collectAsState()
    val darkModeEnabled by prefsManager.darkModeEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
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
            item {
                Text("Audio & Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberCyan)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, GameDarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Sound Effects", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text("Play synthesised retro sound fx", color = TextSecondary, fontSize = 12.sp)
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    prefsManager.setSoundEnabled(it)
                                    soundManager.isSoundEnabled = it
                                    if (it) soundManager.playScore()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberCyan,
                                    checkedTrackColor = GameDarkSurfaceVariant
                                ),
                                modifier = Modifier.testTag("sound_switch")
                            )
                        }

                        Divider(color = GameDarkBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Vibration & Haptics", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Text("Haptic vibration pulses on score & taps", color = TextSecondary, fontSize = 12.sp)
                            }
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = {
                                    prefsManager.setHapticsEnabled(it)
                                    soundManager.isHapticsEnabled = it
                                    if (it) soundManager.vibrateMedium()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberCyan,
                                    checkedTrackColor = GameDarkSurfaceVariant
                                ),
                                modifier = Modifier.testTag("haptics_switch")
                            )
                        }
                    }
                }
            }

            item {
                Text("Display & Appearance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberCyan)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, GameDarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Gaming Mode", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Neon dark cyber interface", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = darkModeEnabled,
                            onCheckedChange = {
                                prefsManager.setDarkMode(it)
                                soundManager.playClick()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = GameDarkSurfaceVariant
                            )
                        )
                    }
                }
            }

            item {
                Text("About GameBox", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberCyan)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GameDarkSurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, GameDarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("GameBox v1.0", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                        Text(
                            "An all-in-one mobile gaming console containing 10 fully playable, original mini-games across Arcade, Puzzle, Classic, Brain & Logic, and 2-Player categories.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GameDarkSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("PLATFORM & ARCHITECTURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Built with native Kotlin and Jetpack Compose 60 FPS Canvas & State engine. Completely offline-capable with local persistence, zero external network requirements, and instant responsive touch controls.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GameDarkSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🛡️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "100% Free, Zero Ads, No In-App Purchases, Guest Friendly.",
                                    fontSize = 12.sp,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
