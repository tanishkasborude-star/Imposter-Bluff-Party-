package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameStage
import com.example.data.model.Player
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val stage by viewModel.gameStage.collectAsState()
    val players by viewModel.players.collectAsState()
    val secretWord by viewModel.currentSecretWord.collectAsState()
    val isAiThinking by viewModel.isAILoading.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    
    val clueIndex by viewModel.clueInputPlayerIndex.collectAsState()
    val activeCluePlayer = players.getOrNull(clueIndex)

    val humanVoteChoice by viewModel.humanVoteChoice.collectAsState()
    val votedOutPlayer by viewModel.votedOutPlayer.collectAsState()
    val winnerRole by viewModel.winnerRole.collectAsState()
    val imposterGuessWord by viewModel.imposterGuessWord.collectAsState()

    var textClueInput by remember { mutableStateOf("") }
    var impGuessInput by remember { mutableStateOf("") }

    val formattedTimer = remember(timerSeconds) {
        val mins = timerSeconds / 60
        val secs = timerSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    // Clear local inputs on index changes
    LaunchedEffect(clueIndex) {
        textClueInput = ""
    }

    // --- Elegant Stage Transition Interceptor overlay ---
    var showOverlay by remember { mutableStateOf(false) }
    var countdownNumber by remember { mutableStateOf(3) }
    var lastObservedStage by remember { mutableStateOf<GameStage?>(null) }
    
    LaunchedEffect(stage) {
        if (lastObservedStage == GameStage.VOTING && (stage == GameStage.IMPOSTER_GUESS || stage == GameStage.GAME_OVER)) {
            showOverlay = true
            countdownNumber = 3
            kotlinx.coroutines.delay(900)
            countdownNumber = 2
            kotlinx.coroutines.delay(900)
            countdownNumber = 1
            kotlinx.coroutines.delay(900)
            showOverlay = false
        }
        lastObservedStage = stage
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B1E)) // Rich deep cyberpunk background from mockup
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        // --- STAGE 1: CLUE INPUT ---
        if (stage == GameStage.CLUE_INPUT) {
            Text(
                text = "CLUE SELECTION ROUND",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFD1BCFF),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isAiThinking) {
                // AI thinking screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFD1BCFF),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "${activeCluePlayer?.name ?: "AI Agent"} is calculating clues...",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Analyzing player vocabulary constraints...",
                            fontSize = 12.sp,
                            color = Color(0xFFC9C5D0),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (activeCluePlayer != null) {
                // Human input screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(Color(0xFF2B2930))
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${activeCluePlayer.name}'s TURN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            // Live timer capsule for clue input turn
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(Color(0xFFFFB4AB).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.4f), RoundedCornerShape(99.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB4AB),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = formattedTimer,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB4AB)
                                    )
                                }
                            }
                        }

                        // Inform relative hints
                        Text(
                            text = "A secret word has been provided to everyone except the Imposter.",
                            fontSize = 13.sp,
                            color = Color(0xFFC9C5D0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "WRITE YOUR CLUE (1-3 WORDS)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFD1BCFF),
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = textClueInput,
                                    onValueChange = { textClueInput = it },
                                    placeholder = { Text("e.g. Liquid gold") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD1BCFF),
                                        unfocusedBorderColor = Color(0xFF49454F)
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (textClueInput.isNotBlank()) {
                                            viewModel.submitHumanClue(textClueInput)
                                        }
                                    }),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (textClueInput.isNotBlank()) {
                                            viewModel.submitHumanClue(textClueInput)
                                        }
                                    },
                                    enabled = textClueInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1BCFF)),
                                    shape = RoundedCornerShape(99.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "SUBMIT MY CLUE",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF381E72),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- STAGE 2: VOTING SUSPECTS ---
        else if (stage == GameStage.VOTING) {
            // Elegant badge row matching prototype
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFFFFB4AB).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.4f), RoundedCornerShape(99.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFB4AB),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "CRITICAL PHASE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB4AB),
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "WHO IS THE IMPOSTER?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Discuss descriptions. Spot the imposter with the fake clue!",
                fontSize = 12.sp,
                color = Color(0xFFC9C5D0),
                textAlign = TextAlign.Center
            )

            // Tension active timer capsule matching prototype
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFFD1BCFF),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = formattedTimer,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFFD1BCFF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(players) { p ->
                    val roleName = when {
                        p.name == "You (Host)" -> "Deceiver"
                        p.isBot && p.name.contains("Nova", true) -> "Operator"
                        p.isBot && p.name.contains("Cypher", true) -> "Scout"
                        p.isBot && p.name.contains("Glitch", true) -> "Hacker"
                        p.isBot && p.name.contains("Echo", true) -> "Medic"
                        p.isBot && p.name.contains("Zero", true) -> "Engineer"
                        p.isBot -> "Agent"
                        else -> "Player"
                    }
                    val pLevel = 12 + kotlin.math.abs(p.name.hashCode() % 78)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (p.isAlive) Color(0xFF2B2930) else Color(0xFF1C1B1F).copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = if (p.isAlive) Color(0xFF49454F).copy(alpha = 0.5f) else Color(0xFF49454F).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (p.isImposter && !p.isAlive) Color(0xFFFFB4AB).copy(alpha = 0.2f) else Color(0xFFD1BCFF).copy(alpha = 0.15f))
                                    .border(1.5.dp, if (p.isAlive) Color(0xFFD1BCFF) else Color(0xFF49454F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p.avatarEmoji, fontSize = 20.sp)
                            }
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = p.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (p.isAlive) Color.White else Color.White.copy(alpha = 0.5f)
                                    )
                                    if (p.name == "You (Host)") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFD1BCFF).copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("YOU", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1BCFF))
                                        }
                                    }
                                }
                                
                                Text(
                                    text = "Lv. $pLevel $roleName",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC9C5D0)
                                )
                                
                                Text(
                                    text = if (p.isAlive) "ALIVE" else "ELIMINATED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (p.isAlive) Color(0xFFB4F5AD) else Color(0xFFFFB4AB)
                                )
                            }
                        }

                        // Player clue
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1C1B1F))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "“ ${p.activeClue.ifBlank { "..." }} ”",
                                fontSize = 13.sp,
                                color = if (p.isAlive) Color(0xFFD1BCFF) else Color(0xFFC9C5D0).copy(alpha = 0.6f),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (isAiThinking) {
                // Computing automated AI votes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD1BCFF), strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                        Text(
                            text = "AI agents casting secret votes...",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFD1BCFF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Interactive vote casting
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "YOUR SUSPECT VOTE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFD1BCFF),
                        fontWeight = FontWeight.Bold
                    )

                    // Choose suspects grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        items(players.filter { it.isAlive && it.name != "You (Host)" }) { suspect ->
                            val isSelected = suspect.name == humanVoteChoice
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Color(0xFFD1BCFF).copy(alpha = 0.1f)
                                        else Color(0xFF2B2930)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFD1BCFF) else Color(0xFF49454F).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setHumanVoteSelection(suspect.name) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(suspect.avatarEmoji, fontSize = 16.sp)
                                    Text(
                                        text = suspect.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.executeVotingRound() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1BCFF)),
                        shape = RoundedCornerShape(99.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            text = "CAST SECRET VOTE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF381E72),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- STAGE 3: IMPOSTER GUESS SECRET WORD ---
        else if (stage == GameStage.IMPOSTER_GUESS) {
            var stampTriggered by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(350)
                stampTriggered = true
            }
            val stampScale by animateFloatAsState(
                targetValue = if (stampTriggered) 1.0f else 4.0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 100f)
            )
            val stampAlpha by animateFloatAsState(
                targetValue = if (stampTriggered) 1.0f else 0.0f,
                animationSpec = tween(durationMillis = 250)
            )

            Text(
                text = "IMPOSTER CAUGHT!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFB4AB)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Background red alert blur
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .blur(50.dp)
                        .background(Color(0xFFFFB4AB).copy(alpha = 0.15f), CircleShape)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "WAIT! ${votedOutPlayer?.name} was the secret Imposter!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "But wait... The Imposter has ONE last final opportunity to guess the secret word. If they guess correctly, they win!",
                        fontSize = 13.sp,
                        color = Color(0xFFC9C5D0),
                        textAlign = TextAlign.Center
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "IMPOSTER'S FINAL WORD GUESS",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFD1BCFF),
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = impGuessInput,
                                onValueChange = { impGuessInput = it },
                                placeholder = { Text("e.g. Vacation") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD1BCFF)
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = { viewModel.submitImposterWordGuess(impGuessInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1BCFF)),
                                shape = RoundedCornerShape(99.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "SUBMIT GUESS",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF381E72)
                                )
                            }
                        }
                    }
                }

                // High fidelity stamp Slam Overlay
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = stampScale,
                            scaleY = stampScale,
                            rotationZ = 10f, // Tilted exactly like stamp-slam in CSS
                            alpha = stampAlpha
                        )
                        .border(4.dp, Color(0xFFFFB4AB), RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B0B1E).copy(alpha = 0.92f))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "IMPOSTER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFB4AB),
                        letterSpacing = 4.sp
                    )
                }
            }
        }

        // --- STAGE 4: GAME OVER SUMMARIES ---
        else if (stage == GameStage.GAME_OVER) {
            val imposterWon = winnerRole == "IMPOSTER"
            val winGlow = if (imposterWon) Color(0xFFD1BCFF) else Color(0xFFB4F5AD)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .blur(60.dp)
                        .background(winGlow.copy(alpha = 0.2f), CircleShape)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = if (imposterWon) Icons.Default.Dangerous else Icons.Default.Stars,
                        contentDescription = null,
                        tint = winGlow,
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = if (imposterWon) "IMPOSTER WON!" else "CIVILIANS WON!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = winGlow,
                        textAlign = TextAlign.Center
                    )

                    val actualImposter = players.firstOrNull { it.isImposter }?.name ?: "Unknown"

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MATCH SUMMARY RESULTS",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFC9C5D0)
                            )

                            // Word info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Secret Word", color = Color(0xFFC9C5D0), fontSize = 13.sp)
                                Text(secretWord, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            HorizontalDivider(color = Color(0xFF49454F).copy(alpha = 0.3f))

                            // Secret imposter info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Secret Imposter", color = Color(0xFFC9C5D0), fontSize = 13.sp)
                                Text(actualImposter, color = Color(0xFFD1BCFF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            HorizontalDivider(color = Color(0xFF49454F).copy(alpha = 0.3f))

                            // Eliminated player info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Voted suspect", color = Color(0xFFC9C5D0), fontSize = 13.sp)
                                Text(votedOutPlayer?.name ?: "No one", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            if (imposterWon && imposterGuessWord.isNotBlank()) {
                                HorizontalDivider(color = Color(0xFF49454F).copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Guess submitted", color = Color(0xFFC9C5D0), fontSize = 13.sp)
                                    Text("“$imposterGuessWord”", color = Color(0xFFB4F5AD), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Game over actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.returnToHome() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313033)),
                    shape = RoundedCornerShape(99.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        "MAIN HOME",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC9C5D0)
                    )
                }

                Button(
                    onClick = { viewModel.startNewGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = winGlow),
                    shape = RoundedCornerShape(99.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        "PLAY AGAIN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (imposterWon) Color(0xFF381E72) else Color(0xFF00390A)
                    )
                }
            }
        }
    } // Closes Column
    } // Closes Box

    // --- Tension Countdown Overlay ---
    if (showOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0B1E).copy(alpha = 0.96f))
                .clickable(enabled = false) {}, // Intercept touch events
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Glow badge tracking state
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFE81123).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFE81123).copy(alpha = 0.40f), RoundedCornerShape(99.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "REVEALING IDENTITIES...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFF2B8B5),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Giant suspense Countdown number
                Text(
                    text = "$countdownNumber",
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFD1BCFF),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "[ SUSPENSEFUL STATIC SURGE ]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFC9C5D0).copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

