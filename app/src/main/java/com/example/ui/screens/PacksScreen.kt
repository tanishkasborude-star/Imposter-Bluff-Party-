package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Pack
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacksScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPack by viewModel.selectedPack.collectAsState()
    val customPacks by viewModel.customPacks.collectAsState()
    
    // Custom pack creator states
    var isFormExpanded by remember { mutableStateOf(false) }
    var newPackName by remember { mutableStateOf("") }
    var newPackDesc by remember { mutableStateOf("") }
    var newPackCategory by remember { mutableStateOf("Custom Pack") }
    var newPackWords by remember { mutableStateOf("") }

    val allBuiltInPacks = viewModel.builtInPacks

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form trigger card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { isFormExpanded = !isFormExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1BCFF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFFD1BCFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Create Custom Pack",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Add your own custom secret word pairs.",
                                fontSize = 11.sp,
                                color = Color(0xFFC9C5D0)
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isFormExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFFDFBDCC),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Expanded Custom Pack Creator State
        item {
            AnimatedVisibility(visible = isFormExpanded) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "New Word Pack Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1BCFF)
                        )

                        // Input fields
                        OutlinedTextField(
                            value = newPackName,
                            onValueChange = { newPackName = it },
                            label = { Text("Pack Name") },
                            placeholder = { Text("e.g. My College Crew") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD1BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedLabelColor = Color(0xFFD1BCFF),
                                unfocusedLabelColor = Color(0xFFC9C5D0)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPackDesc,
                            onValueChange = { newPackDesc = it },
                            label = { Text("Description") },
                            placeholder = { Text("Fun words about our group.") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD1BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedLabelColor = Color(0xFFD1BCFF),
                                unfocusedLabelColor = Color(0xFFC9C5D0)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPackWords,
                            onValueChange = { newPackWords = it },
                            label = { Text("Words (comma separated)") },
                            placeholder = { Text("Laptop, Coffee, Exam, Exam Hall, Professor, Library") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD1BCFF),
                                unfocusedBorderColor = Color(0xFF49454F),
                                focusedLabelColor = Color(0xFFD1BCFF),
                                unfocusedLabelColor = Color(0xFFC9C5D0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                        )

                        Button(
                            onClick = {
                                if (newPackName.isNotBlank() && newPackWords.isNotBlank()) {
                                    viewModel.addCustomPack(
                                        name = newPackName,
                                        desc = newPackDesc,
                                        category = newPackCategory,
                                        wordsString = newPackWords
                                    )
                                    // Reset fields
                                    newPackName = ""
                                    newPackDesc = ""
                                    newPackWords = ""
                                    isFormExpanded = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1BCFF)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(99.dp)
                        ) {
                            Text(
                                "SAVE CUSTOM DECK",
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

        // Custom Pack Section Header
        if (customPacks.isNotEmpty()) {
            item {
                Text(
                    text = "Your Custom Packs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(customPacks) { pack ->
                PackSelectionCard(
                    pack = pack,
                    isSelected = selectedPack?.id == pack.id,
                    onSelect = { viewModel.setPack(pack) },
                    onDelete = {
                        val dbId = pack.id.removePrefix("custom_").toIntOrNull()
                        if (dbId != null) {
                            viewModel.removeCustomPack(dbId)
                        }
                    }
                )
            }
        }

        // Built-In Packs Section Header
        item {
            Text(
                text = "Official Word Decks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        items(allBuiltInPacks) { pack ->
            PackSelectionCard(
                pack = pack,
                isSelected = selectedPack?.id == pack.id,
                onSelect = { viewModel.setPack(pack) },
                onDelete = null // Can't delete built-in official decks
            )
        }
    }
}

@Composable
fun PackSelectionCard(
    pack: Pack,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val themeColor = when(pack.category.lowercase()) {
        "adults only" -> Color(0xFFD1BCFF)
        "sci-fi" -> Color(0xFFB4F5AD)
        else -> Color(0xFFA8EFF2)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) themeColor.copy(alpha = 0.08f) 
                else Color(0xFF2B2930)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) themeColor else Color(0xFF49454F).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored icon box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1C1B1F))
                        .border(1.dp, themeColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when(pack.iconName.lowercase()) {
                        "celebration" -> Icons.Default.Stars
                        "family_restroom" -> Icons.Default.FolderOpen
                        else -> Icons.Default.Stars
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = pack.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(themeColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "ACTIVE",
                                    color = Color(0xFF381E72),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Text(
                        text = pack.description,
                        fontSize = 12.sp,
                        color = Color(0xFFC9C5D0)
                    )

                    Text(
                        text = "${pack.category} • ${pack.countLabel}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = themeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD1BCFF).copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
