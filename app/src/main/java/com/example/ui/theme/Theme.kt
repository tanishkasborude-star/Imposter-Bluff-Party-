package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryNeonPink,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerPink,
    onPrimaryContainer = OnPrimaryContainerPink,
    secondary = SecondaryNeonGreen,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerGreen,
    tertiary = TertiaryNeonTeal,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerTeal,
    background = BgVoid,
    onBackground = OnSurfaceText,
    surface = SurfaceDark,
    onSurface = OnSurfaceText,
    surfaceVariant = SurfaceContainerNormal,
    onSurfaceVariant = OnSurfaceVariantText,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
  )

private val LightColorScheme = DarkColorScheme // Force dark theme for the high-end Neon Imposter vibe

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode
  dynamicColor: Boolean = false, // Disable dynamic colors to keep original Neon colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
