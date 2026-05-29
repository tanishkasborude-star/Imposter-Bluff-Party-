package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.GameHistoryEntity
import com.example.ui.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.fullHistory.collectAsState()

    // Aggregate stats from historyList
    val totalMatches = historyList.size
    val civilWins = historyList.count { it.winnerRole == "CIVILIANS" }
    val imposterWins = historyList.count { it.winnerRole == "IMPOSTER" }
    
    val civilPercentage = if (totalMatches > 0) (civilWins * 100) / totalMatches else 0
    val imposterPercentage = if (totalMatches > 0) (imposterWins * 100) / totalMatches else 0

    // Virtual Level calculation based on stats
    val xp = 150 + (totalMatches * 110)
    val userLevelName = "#B3F9"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Header Profile Card ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background ambient glow
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .blur(40.dp)
                        .background(Color(0xFFD1BCFF).copy(alpha = 0.15f), CircleShape)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Big Premium Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1B1F))
                            .border(2.dp, Color(0xFFD1BCFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 42.sp)
                    }

                    Text(
                        text = "Imposter Host",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFF211F26))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "VXP $xp",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFD1BCFF),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFFD1BCFF).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                userLevelName,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFD1BCFF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Grid of quick metrics ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Metrics 1: Total Played
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = Color(0xFFD1BCFF),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Matches",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFC9C5D0)
                        )
                        Text(
                            text = "$totalMatches",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Metrics 2: Civilian win percentage
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = Color(0xFFD1BCFF),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Civilian Win %",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFC9C5D0)
                        )
                        Text(
                            text = "$civilPercentage%",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- Core History Card List ---
        item {
            Text(
                text = "Match History logs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (historyList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matches played yet! Tap PLAY on home to start a deduction card session.",
                        color = Color(0xFFDFBDCC).copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        items(historyList) { record ->
            HistoryLogItem(record = record)
        }
    }
}

@Composable
fun HistoryLogItem(record: GameHistoryEntity) {
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(record.dateTimestamp) { formatter.format(Date(record.dateTimestamp)) }

    val winTint = if (record.winnerRole == "CIVILIANS") Color(0xFFB4F5AD) else Color(0xFFD1BCFF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF49454F).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.packName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFC9C5D0).copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(color = Color(0xFF49454F).copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Secret Word: \"${record.secretWord}\"",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Imposter suspected: ${record.imposterName}",
                        fontSize = 11.sp,
                        color = Color(0xFFC9C5D0)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(winTint.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${record.winnerRole} WON",
                        color = winTint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
