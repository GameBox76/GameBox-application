package com.example.data

import com.example.model.GameCategory
import com.example.model.GameInfo

object GameRepository {
    val games: List<GameInfo> = listOf(
        GameInfo(
            id = "snake",
            title = "Classic Snake",
            subtitle = "Retro arcade slither",
            description = "Navigate your serpent across the cyber grid, feast on energizer pellets, and grow your score without colliding with walls or yourself.",
            category = GameCategory.ARCADE,
            colorPrimary = 0xFF00E676,
            colorSecondary = 0xFF00B0FF,
            players = "1 Player",
            difficulty = "Medium",
            tags = listOf("Retro", "Fast", "Reflex"),
            instructions = listOf(
                "Use the directional buttons or swipe on screen to steer the snake.",
                "Eat glowing food pellets to grow and earn 10 points each.",
                "Avoid crashing into the grid walls or your own tail.",
                "Speed increases slightly as your snake grows longer!"
            )
        ),
        GameInfo(
            id = "tictactoe",
            title = "Tic-Tac-Toe",
            subtitle = "X's and O's faceoff",
            description = "The timeless tactical grid. Play solo against smart AI with multiple difficulties or pass-and-play locally with a friend.",
            category = GameCategory.CLASSIC,
            colorPrimary = 0xFF00E5FF,
            colorSecondary = 0xFF8C52FF,
            players = "1-2 Players",
            difficulty = "Easy / Hard",
            isTwoPlayerSupported = true,
            tags = listOf("Strategy", "2-Player", "Casual"),
            instructions = listOf(
                "Take turns placing your symbol (X or O) on the 3x3 grid.",
                "Get three of your marks in a horizontal, vertical, or diagonal line to win.",
                "Toggle between Solo (vs AI) and 2-Player Pass & Play mode anytime.",
                "Hard AI uses minimax for an unbeatable challenge!"
            )
        ),
        GameInfo(
            id = "game2048",
            title = "2048",
            subtitle = "Legendary tile merger",
            description = "Slide and merge matching number tiles on a 4x4 matrix. Combine pairs from 2 to 4, 8, 16... all the way to 2048 and beyond!",
            category = GameCategory.PUZZLE,
            colorPrimary = 0xFFFFB300,
            colorSecondary = 0xFFFF3D00,
            players = "1 Player",
            difficulty = "Hard",
            tags = listOf("Numbers", "Logic", "Addictive"),
            instructions = listOf(
                "Swipe in any direction (or use directional buttons) to slide all tiles.",
                "When two tiles with the same number collide, they merge into one!",
                "Plan ahead to keep your highest value tiles in a corner.",
                "Use the Undo button if you make a misstep!"
            )
        ),
        GameInfo(
            id = "memory",
            title = "Memory Match",
            subtitle = "Card flipping challenge",
            description = "Test and train your visual memory. Flip cards, remember their positions, and match all identical icon pairs.",
            category = GameCategory.PUZZLE,
            colorPrimary = 0xFFFF2A85,
            colorSecondary = 0xFF7000FF,
            players = "1 Player",
            difficulty = "Easy / Medium",
            tags = listOf("Brain", "Memory", "Casual"),
            instructions = listOf(
                "Tap cards to flip them over and reveal hidden arcade symbols.",
                "Match two identical cards to remove them from the board.",
                "Try to clear all pairs in as few moves and as quickly as possible.",
                "Choose between 3x4 (12 cards) and 4x4 (16 cards) grid sizes."
            )
        ),
        GameInfo(
            id = "brickbreaker",
            title = "Brick Breaker",
            subtitle = "Paddle & ball demolition",
            description = "Control the cyber paddle to bounce plasma balls into colorful brick structures. Clear rows, bounce off walls, and rack up high combos.",
            category = GameCategory.ARCADE,
            colorPrimary = 0xFFFF1744,
            colorSecondary = 0xFFFF9100,
            players = "1 Player",
            difficulty = "Medium",
            tags = listOf("Action", "Physics", "Retro"),
            instructions = listOf(
                "Drag the paddle horizontally or use the left/right control buttons.",
                "Bounce the ball into the bricks to destroy them and gain points.",
                "Hit different parts of the paddle to adjust the ball's rebound angle.",
                "Don't let the ball slip past your paddle! You have 3 lives."
            )
        ),
        GameInfo(
            id = "minesweeper",
            title = "Minesweeper",
            subtitle = "Tactical mine clearance",
            description = "Carefully uncover safe sectors of the minefield using numerical proximity clues. Flag dangerous mines and sweep the zone.",
            category = GameCategory.PUZZLE,
            colorPrimary = 0xFF00E5FF,
            colorSecondary = 0xFF00E676,
            players = "1 Player",
            difficulty = "Medium",
            tags = listOf("Logic", "Classic", "Strategy"),
            instructions = listOf(
                "Tap a tile to reveal what lies beneath. Your first tap is always safe!",
                "Numbers tell you exactly how many mines touch that specific cell.",
                "Toggle 'Flag Mode' to mark tiles you suspect contain mines.",
                "Reveal all non-mine tiles to secure victory!"
            )
        ),
        GameInfo(
            id = "connectfour",
            title = "Connect Four",
            subtitle = "Vertical 4-in-a-row",
            description = "Drop tokens into the vertical 7x6 grid. Connect 4 discs of your color vertically, horizontally, or diagonally before your opponent does.",
            category = GameCategory.CLASSIC,
            colorPrimary = 0xFFFFD600,
            colorSecondary = 0xFFFF3366,
            players = "1-2 Players",
            difficulty = "Medium",
            isTwoPlayerSupported = true,
            tags = listOf("2-Player", "Strategy", "Classic"),
            instructions = listOf(
                "Tap any column to drop a colored disc into the lowest available slot.",
                "Connect 4 pieces of your color horizontally, vertically, or diagonally.",
                "Play against smart AI or challenge a friend in local 2-Player mode.",
                "Block your opponent's 3-in-a-row threats before they connect 4!"
            )
        ),
        GameInfo(
            id = "reaction",
            title = "Reflex Test",
            subtitle = "Precision reaction timer",
            description = "Measure your raw neurological reaction speed down to the millisecond. Wait for the cyber light to switch from red to green and tap instantly!",
            category = GameCategory.BRAIN,
            colorPrimary = 0xFF00E5FF,
            colorSecondary = 0xFFFF0055,
            players = "1 Player",
            difficulty = "All Skills",
            tags = listOf("Speed", "Reflex", "Brain"),
            instructions = listOf(
                "Tap the screen to start the round. The screen turns RED.",
                "Stay focused! When the screen turns vibrant GREEN, tap as fast as humanly possible!",
                "If you tap too early, a penalty is triggered.",
                "Complete 5 rounds to calculate your official average gamer reflex rank."
            )
        ),
        GameInfo(
            id = "mathquiz",
            title = "Math Blitz",
            subtitle = "Speed arithmetic duel",
            description = "Race against a 30-second ticking clock to solve quick mental math equations. Build multipliers with answer streaks!",
            category = GameCategory.BRAIN,
            colorPrimary = 0xFF8C52FF,
            colorSecondary = 0xFF00E5FF,
            players = "1 Player",
            difficulty = "Fast",
            tags = listOf("Math", "Brain", "Trivia"),
            instructions = listOf(
                "Look at the equation and pick the correct solution from 4 options.",
                "Each correct answer adds points and bonus seconds to your timer.",
                "Chain consecutive right answers to trigger 2x, 3x, and 4x combo multipliers!",
                "How many equations can you solve before the clock hits zero?"
            )
        ),
        GameInfo(
            id = "flappy",
            title = "Flappy Box",
            subtitle = "Acrobatic gravity jump",
            description = "Tap to flap your thrusters, stay airborne, and navigate through treacherous cyber pipes with razor-sharp gaps.",
            category = GameCategory.ARCADE,
            colorPrimary = 0xFFFF9100,
            colorSecondary = 0xFFFF2A85,
            players = "1 Player",
            difficulty = "Challenging",
            tags = listOf("Arcade", "One-Tap", "Skill"),
            instructions = listOf(
                "Tap anywhere on screen to fire thrusters and flap upward.",
                "Gravity pulls you downward continuously—keep a steady rhythm.",
                "Pass through pipe openings to score 1 point per obstacle.",
                "Hitting any pipe or falling to the ground ends the run!"
            )
        )
    )

    fun getGameById(id: String): GameInfo? = games.find { it.id == id }

    fun filterGames(query: String, category: GameCategory): List<GameInfo> {
        return games.filter { game ->
            val matchesCat = category == GameCategory.ALL ||
                    game.category == category ||
                    (category == GameCategory.TWO_PLAYER && game.isTwoPlayerSupported)
            val matchesQuery = query.isBlank() ||
                    game.title.contains(query, ignoreCase = true) ||
                    game.subtitle.contains(query, ignoreCase = true) ||
                    game.tags.any { it.contains(query, ignoreCase = true) }
            matchesCat && matchesQuery
        }
    }
}
