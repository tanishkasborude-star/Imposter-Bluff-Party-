package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.GameStage
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: GameViewModel = viewModel()
        val stage by viewModel.gameStage.collectAsState()
        val roomCode by viewModel.roomCode.collectAsState()
        
        // Tab states when on Home Screen
        var activeTab by remember { mutableIntStateOf(0) }

        val customPacks by viewModel.customPacks.collectAsState()
        val combinedPacks = remember(customPacks) {
            viewModel.builtInPacks + customPacks
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            if (stage == GameStage.HOME) {
              ImposterTopBar(roomCode = roomCode)
            }
          },
          bottomBar = {
            if (stage == GameStage.HOME) {
              ImposterBottomNav(
                activeIndex = activeTab,
                onTabSelected = { activeTab = it }
              )
            }
          },
          containerColor = Color(0xFF1C1B1F)
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF1C1B1F))
              .padding(innerPadding)
          ) {
            // Routing based on game stage
            when (stage) {
              GameStage.HOME -> {
                when (activeTab) {
                  0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPacks = { activeTab = 1 },
                    onNavigateToProfile = { activeTab = 2 },
                    allPacks = combinedPacks
                  )
                  1 -> PacksScreen(viewModel = viewModel)
                  2 -> ProfileScreen(viewModel = viewModel)
                }
              }
              GameStage.LOBBY -> LobbyScreen(
                viewModel = viewModel,
                onNavigateBack = { viewModel.returnToHome() }
              )
              GameStage.ROLE_REVEAL -> WordRevealScreen(viewModel = viewModel)
              GameStage.CLUE_INPUT,
              GameStage.VOTING,
              GameStage.REVEAL_VOTE,
              GameStage.IMPOSTER_GUESS,
              GameStage.GAME_OVER -> GameScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImposterTopBar(roomCode: String) {
  TopAppBar(
    title = {
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
          text = "IMPOSTER",
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFFD1BCFF),
          letterSpacing = 1.sp
        )
      }
    },
    navigationIcon = {
      IconButton(onClick = {}) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Menu",
          tint = Color(0xFFC9C5D0)
        )
      }
    },
    actions = {
      Box(
        modifier = Modifier
          .padding(end = 12.dp)
          .clip(RoundedCornerShape(99.dp))
          .background(Color(0xFF2B2930))
          .border(1.dp, Color(0xFF49454F), RoundedCornerShape(99.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color(0xFFD1BCFF),
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "#$roomCode",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1BCFF)
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color(0xFF1C1B1F),
      titleContentColor = Color(0xFFD1BCFF)
    )
  )
}

@Composable
fun ImposterBottomNav(
  activeIndex: Int,
  onTabSelected: (Int) -> Unit
) {
  NavigationBar(
    containerColor = Color(0xFF211F26),
    tonalElevation = 8.dp,
    modifier = Modifier.border(1.dp, Color(0xFF49454F).copy(alpha = 0.3f))
  ) {
    NavigationBarItem(
      selected = activeIndex == 0,
      onClick = { onTabSelected(0) },
      icon = { Icon(Icons.Default.Home, contentDescription = "HOME") },
      label = { Text("HOME", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFEADDFF),
        unselectedIconColor = Color(0xFFC9C5D0),
        selectedTextColor = Color(0xFFD1BCFF),
        unselectedTextColor = Color(0xFFC9C5D0),
        indicatorColor = Color(0xFF4A4458)
      )
    )

    NavigationBarItem(
      selected = activeIndex == 1,
      onClick = { onTabSelected(1) },
      icon = { Icon(Icons.Default.Inventory2, contentDescription = "PACKS") },
      label = { Text("PACKS", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFEADDFF),
        unselectedIconColor = Color(0xFFC9C5D0),
        selectedTextColor = Color(0xFFD1BCFF),
        unselectedTextColor = Color(0xFFC9C5D0),
        indicatorColor = Color(0xFF4A4458)
      )
    )

    NavigationBarItem(
      selected = activeIndex == 2,
      onClick = { onTabSelected(2) },
      icon = { Icon(Icons.Default.Person, contentDescription = "PROFILE") },
      label = { Text("PROFILE", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFFEADDFF),
        unselectedIconColor = Color(0xFFC9C5D0),
        selectedTextColor = Color(0xFFD1BCFF),
        unselectedTextColor = Color(0xFFC9C5D0),
        indicatorColor = Color(0xFF4A4458)
      )
    )
  }
}

