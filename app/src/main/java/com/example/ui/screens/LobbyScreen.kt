package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val players by viewModel.players.collectAsState()
    val selectedPack by viewModel.selectedPack.collectAsState()
    val roomCode by viewModel.roomCode.collectAsState()
    
    var newPlayerName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Game Setup Lobby",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Room Key details card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD1BCFF).copy(alpha = 0.05f))
                        .border(1.dp, Color(0xFFD1BCFF).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SHARE ROOM KEY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFD1BCFF),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "#$roomCode",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        var keyCopiedState by remember { mutableStateOf(false) }
                        
                        Button(
                            onClick = { keyCopiedState = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2930)),
                            shape = RoundedCornerShape(99.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (keyCopiedState) "COPIED ✔" else "COPY KEY 🔗",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD1BCFF),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Selected Pack details
            item {
                selectedPack?.let { pack ->
                    Text(
                        text = "Active Word Deck",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC9C5D0),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2B2930))
                            .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = Color(0xFFD1BCFF),
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = pack.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = pack.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFFC9C5D0)
                                )
                            }
                        }
                    }
                }
            }

            // Input Row to Add Custom human or bot player
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Invite Players (Humans or AI Bots)",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC9C5D0),
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { newPlayerName = it },
                            placeholder = { Text("Player Name...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD1BCFF),
                                unfocusedBorderColor = Color(0xFF49454F)
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Add Human button
                        IconButton(
                            onClick = {
                                if (newPlayerName.isNotBlank()) {
                                    viewModel.addPlayer(newPlayerName, isBot = false)
                                    newPlayerName = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFD1BCFF))
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add human",
                                tint = Color(0xFF381E72)
                            )
                        }

                        // Add Bot button
                        IconButton(
                            onClick = {
                                if (newPlayerName.isNotBlank()) {
                                    viewModel.addPlayer(newPlayerName, isBot = true)
                                    newPlayerName = ""
                                } else {
                                    // Generate a randomized creative bot name
                                    val fallbackBots = listOf("CyberSleuth", "ZeroAI", "PixelGamer", "RoboDeduce", "BitWhiz", "NexusBot")
                                    viewModel.addPlayer(fallbackBots.random() + "🤖", isBot = true)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFB4F5AD))
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Add Bot",
                                tint = Color(0xFF00390A)
                            )
                        }
                    }
                }
            }

            // Players List Roster with scores reset trigger
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Player Roster (${players.size}/8 in Lobby)",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC9C5D0),
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (players.any { it.score > 0 }) {
                        Text(
                            text = "RESET LEADERBOARD",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFB4AB),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { viewModel.resetScores() }
                                .padding(4.dp)
                        )
                    }
                }
            }

            items(players) { player ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2B2930))
                        .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(player.avatarEmoji, fontSize = 20.sp)
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = player.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    // small score badge matching prototype
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFB4F5AD).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${player.score} PTS",
                                            color = Color(0xFFB4F5AD),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text(
                                    text = if (player.isBot) "AI BOT suspects everyone" else "HUMAN player",
                                    fontSize = 11.sp,
                                    color = if (player.isBot) Color(0xFFB4F5AD) else Color(0xFFD1BCFF)
                                )
                            }
                        }

                        // Allow removing player
                        if (player.name != "You (Host)") {
                            IconButton(onClick = { viewModel.removePlayer(player) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Trigger Button
        val validToStart = players.size >= 3
        Button(
            onClick = { viewModel.startNewGame() },
            enabled = validToStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD1BCFF),
                disabledContainerColor = Color(0xFF313033),
                disabledContentColor = Color(0xFFC9C5D0).copy(alpha = 0.3f),
                contentColor = Color(0xFF381E72)
            ),
            shape = RoundedCornerShape(99.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = if (validToStart) "START DEDUCTION MATCH" else "NEED AT LEAST 3 PLAYERS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
