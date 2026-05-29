package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.database.CustomPackEntity
import com.example.data.database.GameHistoryEntity
import com.example.data.database.GameRepository
import com.example.data.model.GameStage
import com.example.data.model.Pack
import com.example.data.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = GameRepository(db.gameDao())

    // --- State Variables ---
    private val _gameStage = MutableStateFlow(GameStage.HOME)
    val gameStage: StateFlow<GameStage> = _gameStage.asStateFlow()

    private val _roomCode = MutableStateFlow("B3F9")
    val roomCode: StateFlow<String> = _roomCode.asStateFlow()

    private val _timerSeconds = MutableStateFlow(45)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private var activeTimerJob: kotlinx.coroutines.Job? = null

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _selectedPack = MutableStateFlow<Pack?>(null)
    val selectedPack: StateFlow<Pack?> = _selectedPack.asStateFlow()

    private val _currentSecretWord = MutableStateFlow("")
    val currentSecretWord: StateFlow<String> = _currentSecretWord.asStateFlow()

    // Pass and Play reveal flow
    private val _revealPlayerIndex = MutableStateFlow(0)
    val revealPlayerIndex: StateFlow<Int> = _revealPlayerIndex.asStateFlow()

    // Clue input round flow
    private val _clueInputPlayerIndex = MutableStateFlow(0)
    val clueInputPlayerIndex: StateFlow<Int> = _clueInputPlayerIndex.asStateFlow()

    // Loading indicator for active AI computation
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    // Selected Suspect for human voter
    private val _humanVoteChoice = MutableStateFlow("")
    val humanVoteChoice: StateFlow<String> = _humanVoteChoice.asStateFlow()

    // Whos voting index
    private val _votingPlayerIndex = MutableStateFlow(0)
    val votingPlayerIndex: StateFlow<Int> = _votingPlayerIndex.asStateFlow()

    private val _votedOutPlayer = MutableStateFlow<Player?>(null)
    val votedOutPlayer: StateFlow<Player?> = _votedOutPlayer.asStateFlow()

    private val _winnerRole = MutableStateFlow("") // "IMPOSTER" or "CIVILIANS" or ""
    val winnerRole: StateFlow<String> = _winnerRole.asStateFlow()

    // Imposter guess flow
    private val _imposterGuessWord = MutableStateFlow("")
    val imposterGuessWord: StateFlow<String> = _imposterGuessWord.asStateFlow()

    // Custom pack list from repository
    val customPacks: StateFlow<List<Pack>> = repository.customPacks.map { entities ->
        entities.map { it.toPackModel() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Game statistics derived from db history
    val fullHistory: StateFlow<List<GameHistoryEntity>> = repository.fullHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Preloaded built-in packs as per HTML specs
    val builtInPacks = listOf(
        Pack(
            id = "general",
            name = "General Pop Cult",
            description = "Fun pop culture, tech, and everyday expressions.",
            category = "General",
            countLabel = "80 Cards",
            iconName = "stars",
            isFree = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAi6JOEJWL1g9LoX2pMfHyijsk6VKjwcRX8JUa9Gjdt_wMAl3SKRYXpWmXTDGgjutq5IM3G6vhu6KIY-tk79OCxOtAUkTOE3b4Tsf0GRYYdWg4x4wOhuSEv5BE7FoROs3Ld2vh-9a9G4WPLUbnb1gjf3AtKuwCcIZyaTlR2-iQaS0ZgjHOsPPh7FPgl0-jA0Hc5hozJh9JA_FnMwGLLe6-7r2ESkgxZogmyEyTyRmWrgGR7vM4eojj9X5VtUMa5_kzaVXCPVP2epNZ-",
            words = listOf(
                "Apple", "Money", "Music", "Coffee", "Camera", "Instagram", "Internet", "Television", 
                "Doctor", "Diamond", "Soccer", "Guitar", "Sneakers", "Pizza", "Chocolate", "Subway", 
                "Train", "Airport", "Supermarket", "Perfume", "Clock", "Mirror", "Keys", "Computer"
            )
        ),
        Pack(
            id = "pregame",
            name = "The Pregame",
            description = "Adults Only party deck with flirty and drinking word triggers.",
            category = "Adults Only",
            countLabel = "50 Cards",
            iconName = "celebration",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAi6JOEJWL1g9LoX2pMfHyijsk6VKjwcRX8JUa9Gjdt_wMAl3SKRYXpWmXTDGgjutq5IM3G6vhu6KIY-tk79OCxOtAUkTOE3b4Tsf0GRYYdWg4x4wOhuSEv5BE7FoROs3Ld2vh-9a9G4WPLUbnb1gjf3AtKuwCcIZyaTlR2-iQaS0ZgjHOsPPh7FPgl0-jA0Hc5hozJh9JA_FnMwGLLe6-7r2ESkgxZogmyEyTyRmWrgGR7vM4eojj9X5VtUMa5_kzaVXCPVP2epNZ-",
            words = listOf(
                "Cocktail", "Drunk", "Sober", "Dare", "Karaoke", "Club", "Shot", "Vibe", 
                "Hangover", "Tequila", "Beer", "Champagne", "Salsa", "Truth", "Party", "Flask", 
                "Dance", "Midnight", "Secrets", "Lounge", "Spin Bottle", "VIP", "Mixer", "Cheers"
            )
        ),
        Pack(
            id = "families",
            name = "Family Feud",
            description = "Clean family-friendly word triggers for everyone.",
            category = "Everyone",
            countLabel = "100 Cards",
            iconName = "family_restroom",
            isFree = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuASjJEwTN_Pwb7XQFE05sVo2vJyD2x_auh_eKJ1XFEY_6lbANX0seijeSCGsZvzkqklQ8VAflLI0WMJicMV9_SZlFe9RGkdAdxBaKJBLqTXYXSIl3EBJVvUR3fAxJhiSzAjYopxNdAjzX5ZvOQAH5cU-jvcAjqKRHd7JtK9v2ZJdCj1ZXV0m7fMIyBuWBynoKzAUt_ZADvAjSsOlsd0JQ-F5ruqp8SD-BhuToPaheIw8_Qejr9iVkEOitftARrjdOLC1BAipB6KOYzF",
            words = listOf(
                "Beach", "Campfire", "Vacation", "School", "Dentist", "Cooking", "Shopping", "Homework", 
                "Cinema", "Playground", "Bicycle", "Ice Cream", "Puppy", "Cat", "Kite", "Picnic", 
                "Zoo", "Museum", "Library", "Garden", "Park", "Cake", "Balloon", "Gift", "Rainbow"
            )
        ),
        Pack(
            id = "deepspace",
            name = "Deep Space",
            description = "Sci-Fi futuristic words for space explorers.",
            category = "Sci-Fi",
            countLabel = "75 Cards",
            iconName = "rocket_launch",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCz2PKDqE5bnPnWCbMNIr-iUEq8Nv-kdLCgDROAsYK3aIVWGnh15Q15V-NNC3leAeFqdWxd6QsgexmuM6Fdtd3lieFSmsHR7JjwfnoyPJ56Ow-hj4emWtut2qIHfoXex7HtdgW1Nzaa5UIkJ6P-eTJgnBoGf7pifvAb2T-P8sSHYOXVqmkVXl8-fmwCj-qbtyMyBTbkWsUK3aQGNHPEh51YbBzBgk-pfPBt3Wdf2CqnIK_Rfph7bOtairyOHj4YspxwyjhUKdsFHvtY",
            words = listOf(
                "Alien", "Wormhole", "Spaceship", "Hyperdrive", "Galaxy", "Nebula", "Black Hole", "Orbit", 
                "Laser", "Cyborg", "Teleport", "Zero Gravity", "Astronaut", "Supernova", "Rocket", 
                "Meteor", "Martian", "Stardust", "Eclipse", "Satellite", "Cosmos", "Asteroid", "Pluto"
            )
        )
    )

    init {
        // Default player setups
        _players.value = listOf(
            Player("You (Host)", avatarEmoji = "👑"),
            Player("NeoBot", isBot = true, avatarEmoji = "🤖"),
            Player("QuinnBot", isBot = true, avatarEmoji = "👾"),
            Player("ValBot", isBot = true, avatarEmoji = "🦾")
        )
        _selectedPack.value = builtInPacks[1] // Family as default
    }

    // --- Lobby Configurations ---
    
    fun setPack(pack: Pack) {
        _selectedPack.value = pack
    }

    fun addPlayer(name: String, isBot: Boolean = false) {
        if (name.isBlank()) return
        val currentList = _players.value.toMutableList()
        val emoji = if (isBot) listOf("🤖", "👾", "🦾", "📡", "🧠").random() else listOf("👤", "🐱", "🐶", "🦁", "🦊", "🐼").random()
        currentList.add(Player(name, isBot = isBot, avatarEmoji = emoji))
        _players.value = currentList
    }

    fun removePlayer(player: Player) {
        val currentList = _players.value.filter { it.name != player.name }
        _players.value = currentList
    }

    fun addCustomPack(name: String, desc: String, category: String, wordsString: String) {
        viewModelScope.launch {
            val words = wordsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (name.isNotEmpty() && words.isNotEmpty()) {
                val entity = CustomPackEntity(
                    name = name,
                    creatorName = "You",
                    description = desc.ifBlank { "Unleash custom words." },
                    words = words.joinToString(","),
                    isBuiltIn = false,
                    iconName = "stars"
                )
                repository.addCustomPack(entity)
            }
        }
    }

    fun removeCustomPack(packId: Int) {
        viewModelScope.launch {
            repository.removeCustomPack(packId)
        }
    }

    // --- Core Game Actions ---

    fun startNewGame() {
        val activePack = _selectedPack.value ?: builtInPacks[1]
        val activeWords = activePack.words
        if (activeWords.isEmpty() || _players.value.size < 3) return

        // Pick a secret word
        val secretWord = activeWords.random()
        _currentSecretWord.value = secretWord

        // Reset and assign roles
        val playersList = _players.value.map { it.copy(isAlive = true, isImposter = false, activeClue = "", voteCount = 0) }
        val shuffledPlayers = playersList.toMutableList()
        val imposterIndex = shuffledPlayers.indices.random()
        
        val assignedList = shuffledPlayers.mapIndexed { index, player ->
            val isImpos = (index == imposterIndex)
            player.copy(
                isImposter = isImpos,
                secretWord = if (isImpos) "???" else secretWord
            )
        }

        _players.value = assignedList
        _revealPlayerIndex.value = 0
        _clueInputPlayerIndex.value = 0
        _votingPlayerIndex.value = 0
        _humanVoteChoice.value = ""
        _imposterGuessWord.value = ""
        _votedOutPlayer.value = null
        _winnerRole.value = ""
        
        _gameStage.value = GameStage.ROLE_REVEAL
    }

    fun nextReveal() {
        val currentReveal = _revealPlayerIndex.value
        val totalPlayers = _players.value.size
        
        if (currentReveal < totalPlayers - 1) {
            _revealPlayerIndex.value = currentReveal + 1
        } else {
            // Finished revealing roles! Start clues.
            _clueInputPlayerIndex.value = 0
            _gameStage.value = GameStage.CLUE_INPUT
            // Check if first player is a bot, if so trigger AI, else start countdown
            checkAndTriggerAIPendingClue()
        }
    }

    fun submitHumanClue(clue: String) {
        cancelCountdownTimer()
        val index = _clueInputPlayerIndex.value
        val list = _players.value.toMutableList()
        val currentInputPlayer = list.getOrNull(index) ?: return

        list[index] = currentInputPlayer.copy(activeClue = clue)
        _players.value = list

        advanceClueInputIndex()
    }

    private fun advanceClueInputIndex() {
        cancelCountdownTimer()
        val nextIndex = _clueInputPlayerIndex.value + 1
        val total = _players.value.size

        if (nextIndex < total) {
            _clueInputPlayerIndex.value = nextIndex
            checkAndTriggerAIPendingClue()
        } else {
            // All clues are written! Advance to voting.
            _votingPlayerIndex.value = 0
            _gameStage.value = GameStage.VOTING
            // Start discussion and voting timer: 60s. Auto execute vote on time up!
            startCountdownTimer(60) {
                executeVotingRound()
            }
        }
    }

    private fun checkAndTriggerAIPendingClue() {
        cancelCountdownTimer()
        val index = _clueInputPlayerIndex.value
        val player = _players.value.getOrNull(index) ?: return

        if (player.isBot) {
            _isAILoading.value = true
            viewModelScope.launch {
                // Gather other players' clues so far
                val otherClues = _players.value
                    .filter { it.activeClue.isNotEmpty() }
                    .map { it.name to it.activeClue }

                val generatedClue = GeminiClient.getAICclue(
                    botName = player.name,
                    secretWord = _currentSecretWord.value,
                    isImposter = player.isImposter,
                    otherClues = otherClues
                )

                _isAILoading.value = false
                
                val list = _players.value.toMutableList()
                list[index] = player.copy(activeClue = generatedClue)
                _players.value = list

                advanceClueInputIndex()
            }
        } else {
            // It's human turn! Start a 45s clue timer.
            startCountdownTimer(45) {
                submitHumanClue("Silent Hint 🤫")
            }
        }
    }

    // --- Voting engine ---

    fun setHumanVoteSelection(targetName: String) {
        _humanVoteChoice.value = targetName
    }

    fun executeVotingRound() {
        cancelCountdownTimer()
        _isAILoading.value = true
        viewModelScope.launch {
            // Map to store votes
            val voteTallies = mutableMapOf<String, Int>()
            val playersList = _players.value
            val alivePlayersNames = playersList.filter { it.isAlive }.map { it.name }
            val cluesShared = playersList.map { it.name to it.activeClue }

            val updatedPlayers = playersList.toMutableList()

            // Calculate votes
            for (voter in playersList) {
                if (!voter.isAlive) continue

                val targetSelected: String = if (voter.isBot) {
                    GeminiClient.getAIVote(
                        botName = voter.name,
                        isImposter = voter.isImposter,
                        secretWord = _currentSecretWord.value,
                        playerClues = cluesShared,
                        choices = alivePlayersNames
                    )
                } else {
                    _humanVoteChoice.value.ifBlank {
                        // fallback to a random other player if they tapped nothing
                        alivePlayersNames.filter { it != voter.name }.random()
                    }
                }

                voteTallies[targetSelected] = (voteTallies[targetSelected] ?: 0) + 1
            }

            // Write vote tallies onto players
            var highestVoteCount = -1
            var suspectToVoteOut: Player? = null
            var isTie = false

            val tallyAppliedList = playersList.map { player ->
                val count = voteTallies[player.name] ?: 0
                if (count > highestVoteCount) {
                    highestVoteCount = count
                    suspectToVoteOut = player
                    isTie = false
                } else if (count == highestVoteCount) {
                    isTie = true
                }
                player.copy(voteCount = count)
            }

            _players.value = tallyAppliedList
            _isAILoading.value = false

            // Process suspect elimination
            if (isTie || suspectToVoteOut == null) {
                // It was a tie! No one goes out. Next turn or voting retry? Let's say random fallback from suspects to keep it fast and fun
                val suspects = tallyAppliedList.filter { it.voteCount == highestVoteCount }
                val eliminated = suspects.random()
                eliminatePlayer(eliminated)
            } else {
                eliminatePlayer(suspectToVoteOut!!)
            }
        }
    }

    private fun eliminatePlayer(player: Player) {
        _votedOutPlayer.value = player
        
        // Update alive state in the main players list
        val updated = _players.value.map {
            if (it.name == player.name) it.copy(isAlive = false) else it
        }
        _players.value = updated

        // Determine transition stage:
        if (player.isImposter) {
            // Players found the Imposter! But the Imposter gets one last guess to see if they can identify the word
            _gameStage.value = GameStage.IMPOSTER_GUESS
        } else {
            // Check how many players (especially Civilians) are alive
            val aliveCivilians = updated.filter { it.isAlive && !it.isImposter }.size
            val aliveImposters = updated.filter { it.isAlive && it.isImposter }.size

            if (aliveCivilians <= aliveImposters || aliveCivilians <= 1) {
                // Imposter wins!
                recordWinner("IMPOSTER")
            } else {
                // Keep playing! Go back to Clue stage for another round
                // Wait, to keep game flow super direct, we can just say game over: CIVILIANS lost because they voted out a civil!
                // Let's decide: Imposter immediately wins if they eliminate civilians!
                recordWinner("IMPOSTER")
            }
        }
    }

    fun submitImposterWordGuess(guess: String) {
        _imposterGuessWord.value = guess
        val actualWord = _currentSecretWord.value
        val isCorrect = guess.trim().equals(actualWord.trim(), ignoreCase = true)
        
        if (isCorrect) {
            // Imposter guessed correctly and stole the show!
            recordWinner("IMPOSTER")
        } else {
            // Imposter failed! Civilians survive and win!
            recordWinner("CIVILIANS")
        }
    }

    private fun recordWinner(role: String) {
        _winnerRole.value = role
        _gameStage.value = GameStage.GAME_OVER
        cancelCountdownTimer()

        // Award score points in-memory!
        val updatedList = _players.value.map { player ->
            if (role == "IMPOSTER") {
                if (player.isImposter) {
                    player.copy(score = player.score + 20)
                } else {
                    player
                }
            } else { // Civilians win!
                if (!player.isImposter) {
                    player.copy(score = player.score + 10)
                } else {
                    player
                }
            }
        }
        _players.value = updatedList

        // Persist game summary to database!
        viewModelScope.launch {
            val names = updatedList.joinToString(",") { it.name }
            val imposter = updatedList.firstOrNull { it.isImposter }?.name ?: "Unknown"
            val record = GameHistoryEntity(
                packName = _selectedPack.value?.name ?: "Unknown Pack",
                playerNamesList = names,
                imposterName = imposter,
                winnerRole = role,
                durationSeconds = (15..90).random(), // decorative simulation
                secretWord = _currentSecretWord.value
            )
            repository.addGameRecord(record)
        }
    }

    fun returnToHome() {
        cancelCountdownTimer()
        _gameStage.value = GameStage.HOME
    }

    fun goToLobby() {
        cancelCountdownTimer()
        _gameStage.value = GameStage.LOBBY
    }

    fun resetScores() {
        val resetList = _players.value.map { it.copy(score = 0) }
        _players.value = resetList
    }

    fun generateRoomCode(customCode: String? = null) {
        if (customCode != null && customCode.isNotBlank()) {
            _roomCode.value = customCode.uppercase()
        } else {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val code = (1..6).map { chars.random() }.joinToString("")
            _roomCode.value = code
        }
    }

    fun startCountdownTimer(seconds: Int = 45, onTimeUp: () -> Unit) {
        activeTimerJob?.cancel()
        _timerSeconds.value = seconds
        activeTimerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                kotlinx.coroutines.delay(1000)
                _timerSeconds.value -= 1
            }
            onTimeUp()
        }
    }

    fun cancelCountdownTimer() {
        activeTimerJob?.cancel()
        activeTimerJob = null
    }

    // Conversions
    private fun CustomPackEntity.toPackModel(): Pack {
        return Pack(
            id = "custom_${this.id}",
            name = this.name,
            description = this.description,
            category = "Custom Pack",
            countLabel = "${this.words.split(",").size} Cards",
            iconName = "stars",
            imageUrl = "https://lh3.googleusercontent.com/df-fake-url-for-coil", // fallback
            words = this.words.split(",").map { it.trim() }
        )
    }
}
