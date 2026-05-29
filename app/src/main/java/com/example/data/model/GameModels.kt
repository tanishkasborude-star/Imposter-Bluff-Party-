package com.example.data.model

data class Player(
    val name: String,
    val isBot: Boolean = false,
    val isImposter: Boolean = false,
    val secretWord: String = "",
    val activeClue: String = "",
    val isAlive: Boolean = true,
    val voteCount: Int = 0,
    val avatarEmoji: String = "👤",
    val score: Int = 0
)

data class Pack(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // e.g. "Everyone", "Adults Only", "Sci-Fi"
    val countLabel: String, // e.g. "50 Cards", "100 Cards"
    val iconName: String, // celebration, family_restroom, rocket_launch
    val imageUrl: String,
    val isFree: Boolean = false,
    val words: List<String>
)

enum class GameStage {
    HOME,
    LOBBY,
    ROLE_REVEAL,
    CLUE_INPUT,
    VOTING,
    REVEAL_VOTE,
    IMPOSTER_GUESS,
    GAME_OVER
}
