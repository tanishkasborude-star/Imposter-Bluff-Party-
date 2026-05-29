package com.example.ui.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Pack
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToPacks: () -> Unit,
    onNavigateToProfile: () -> Unit,
    allPacks: List<Pack>
) {
    val scrollState = rememberScrollState()
    var enterCodeText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Infinite Anim for play button pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        // --- Hero Section: Play Button ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background glowing blur
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .blur(50.dp)
                    .background(Color(0xFFD1BCFF).copy(alpha = glowAlpha * 0.3f), CircleShape)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsating circle button
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .graphicsLayer(
                            scaleX = pulseScale,
                            scaleY = pulseScale
                        )
                        .clip(CircleShape)
                        .background(Color(0xFFD1BCFF))
                        .border(4.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable {
                            viewModel.startNewGame()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "PLAY",
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PLAY",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF381E72),
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "TAP TO QUICK MATCH",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC9C5D0),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Room Actions (Bento Box glassmorphic cards) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Join Room
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                // Side glow accent
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .blur(30.dp)
                        .background(Color(0xFFD1BCFF).copy(alpha = 0.08f), CircleShape)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1C1B1F))
                                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = Color(0xFFD1BCFF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Join Room",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Enter a 6-digit code to join a private game.",
                                fontSize = 12.sp,
                                color = Color(0xFFC9C5D0)
                            )
                        }
                    }

                    // Input Field Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF1C1B1F))
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(999.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = enterCodeText,
                            onValueChange = {
                                if (it.length <= 6) enterCodeText = it.uppercase()
                            },
                            placeholder = {
                                Text(
                                    text = "ENTER CODE...",
                                    color = Color(0xFFC9C5D0).copy(alpha = 0.4f),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFFD1BCFF)
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFFE6E1E5),
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp,
                                letterSpacing = 4.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                if (enterCodeText.length >= 4) {
                                    viewModel.generateRoomCode(enterCodeText)
                                    viewModel.goToLobby()
                                }
                            })
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD1BCFF))
                                .clickable {
                                    if (enterCodeText.length >= 4) {
                                        viewModel.generateRoomCode(enterCodeText)
                                        viewModel.goToLobby()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Join",
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Card 2: Create Room
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2B2930))
                    .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                // Side glow accent
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopEnd)
                        .blur(30.dp)
                        .background(Color(0xFFB4F5AD).copy(alpha = 0.05f), CircleShape)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1C1B1F))
                                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = Color(0xFFB4F5AD),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Create Room",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Host a game and invite your friends to play.",
                                fontSize = 12.sp,
                                color = Color(0xFFC9C5D0)
                            )
                        }
                    }

                    // Host Game Button
                    Button(
                        onClick = {
                            viewModel.generateRoomCode()
                            viewModel.goToLobby()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF313033),
                            contentColor = Color(0xFFD1BCFF)
                        ),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, Color(0xFF49454F).copy(alpha = 0.5f), RoundedCornerShape(999.dp)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFFD1BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HOST GAME",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // --- Topic Packs horizontally scrolling Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Packs",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "VIEW ALL >",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD1BCFF),
                    modifier = Modifier
                        .clickable { onNavigateToPacks() }
                        .padding(4.dp)
                )
            }

            // Carousel Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                allPacks.forEach { pack ->
                    PackCarouselItem(
                        pack = pack,
                        onClick = { viewModel.setPack(pack) }
                    )
                }
            }
        }
    }
}

@Composable
fun PackCarouselItem(
    pack: Pack,
    onClick: () -> Unit
) {
    // Determine gradient depending on pack category
    val outlineColor = when(pack.category.lowercase()) {
        "adults only" -> Color(0xFFD1BCFF)
        "sci-fi" -> Color(0xFFB4F5AD)
        else -> Color(0xFFA8EFF2)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B2930)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(230.dp)
            .border(1.dp, outlineColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pack Cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF25232A)),
                contentAlignment = Alignment.Center
            ) {
                // Background image loading via Coil
                AsyncImage(
                    model = pack.imageUrl,
                    contentDescription = pack.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        // Fallback in case of failure: rich gradient!
                        Log.w("HomeScreen", "Coil image failed to load, utilizing custom gradient background for ${pack.name}")
                    }
                )

                // Overlay colored gradient for neon aesthetics and text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF1C1B1F).copy(alpha = 0.6f))
                            )
                        )
                )

                // Render badge icon nicely
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1C1B1F).copy(alpha = 0.75f), CircleShape)
                        .border(1.dp, outlineColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when(pack.iconName.lowercase()) {
                        "celebration" -> Icons.Default.Celebration
                        "family_restroom" -> Icons.Default.FamilyRestroom
                        "rocket_launch" -> Icons.Default.RocketLaunch
                        else -> Icons.Default.Stars
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = outlineColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (pack.isFree) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFD1BCFF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FREE",
                            color = Color(0xFF381E72),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Title list
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pack.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${pack.category} • ${pack.countLabel}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFC9C5D0)
                )
            }
        }
    }
}
