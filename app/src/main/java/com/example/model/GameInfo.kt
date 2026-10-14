package com.example.model

data class GameInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: GameCategory,
    val colorPrimary: Long,
    val colorSecondary: Long,
    val players: String = "1 Player",
    val difficulty: String = "Casual",
    val isTwoPlayerSupported: Boolean = false,
    val tags: List<String> = emptyList(),
    val instructions: List<String> = emptyList()
)
