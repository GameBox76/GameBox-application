package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.PreferencesManager
import com.example.data.SoundManager
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.games.brickbreaker.BrickBreakerGame
import com.example.ui.games.connectfour.ConnectFourGame
import com.example.ui.games.flappy.FlappyGame
import com.example.ui.games.game2048.Game2048
import com.example.ui.games.mathquiz.MathQuizGame
import com.example.ui.games.memory.MemoryGame
import com.example.ui.games.minesweeper.MinesweeperGame
import com.example.ui.games.reaction.ReactionGame
import com.example.ui.games.snake.SnakeGame
import com.example.ui.games.tictactoe.TicTacToeGame
import com.example.ui.missions.MissionsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GameBoxTheme
import com.example.ui.theme.GameDarkBackground
import com.example.ui.theme.GameDarkBorder
import com.example.ui.theme.GameDarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)
        prefsManager = PreferencesManager(this)

        setContent {
            val darkMode by prefsManager.darkModeEnabled.collectAsState()
            val soundEnabled by prefsManager.soundEnabled.collectAsState()
            val hapticsEnabled by prefsManager.hapticsEnabled.collectAsState()

            soundManager.isSoundEnabled = soundEnabled
            soundManager.isHapticsEnabled = hapticsEnabled

            GameBoxTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isGameScreen = currentRoute?.startsWith("game/") == true

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    bottomBar = {
                        if (!isGameScreen) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 3.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "dashboard",
                                    onClick = {
                                        soundManager.playClick()
                                        navController.navigate("dashboard") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(imageVector = Icons.Default.SportsEsports, contentDescription = "Games")
                                    },
                                    label = { Text("Games", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00363D),
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_item_games")
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "missions",
                                    onClick = {
                                        soundManager.playClick()
                                        navController.navigate("missions") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Missions")
                                    },
                                    label = { Text("Missions", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00363D),
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_item_missions")
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        soundManager.playClick()
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                                    },
                                    label = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00363D),
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_item_profile")
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        soundManager.playClick()
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                                    },
                                    label = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00363D),
                                        selectedTextColor = CyberCyan,
                                        indicatorColor = CyberCyan,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag("nav_item_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                soundManager = soundManager,
                                prefsManager = prefsManager,
                                onSelectGame = { gameId ->
                                    navController.navigate("game/$gameId")
                                }
                            )
                        }

                        composable("missions") {
                            MissionsScreen(
                                soundManager = soundManager,
                                prefsManager = prefsManager,
                                onLaunchGame = { gameId ->
                                    navController.navigate("game/$gameId")
                                }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                soundManager = soundManager,
                                prefsManager = prefsManager,
                                onBack = { navController.navigate("dashboard") }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                soundManager = soundManager,
                                prefsManager = prefsManager,
                                onBack = { navController.navigate("dashboard") }
                            )
                        }

                        composable(
                            route = "game/{gameId}",
                            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val gameId = backStackEntry.arguments?.getString("gameId") ?: "snake"
                            when (gameId) {
                                "snake" -> SnakeGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "tictactoe" -> TicTacToeGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "game2048" -> Game2048(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "memory" -> MemoryGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "brickbreaker" -> BrickBreakerGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "minesweeper" -> MinesweeperGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "connectfour" -> ConnectFourGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "reaction" -> ReactionGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "mathquiz" -> MathQuizGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                "flappy" -> FlappyGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                                else -> SnakeGame(
                                    soundManager = soundManager,
                                    prefsManager = prefsManager,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
