package com.muhanad.searchformycar.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    scrim = DarkSurface,
    onTertiaryFixed = actionbar_dark,
    primary = VibrantRed,
    secondary = PrimaryOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    secondary = AmberOrange,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    scrim = PrimaryOrange,
    onTertiaryFixed = PrimaryOrange,
)

@Composable
fun SearchformycarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Smooth color animation when toggling dark mode to eliminate flickering
    val animatedPrimary by animateColorAsState(targetColorScheme.primary, animationSpec = tween(400), label = "primary")
    val animatedBackground by animateColorAsState(targetColorScheme.background, animationSpec = tween(400), label = "background")
    val animatedSurface by animateColorAsState(targetColorScheme.surface, animationSpec = tween(400), label = "surface")
    val animatedOnSurface by animateColorAsState(targetColorScheme.onSurface, animationSpec = tween(400), label = "onSurface")
    val animatedOnBackground by animateColorAsState(targetColorScheme.onBackground, animationSpec = tween(400), label = "onBackground")
    val animatedScrim by animateColorAsState(targetColorScheme.scrim, animationSpec = tween(400), label = "scrim")
    val animatedOnTertiaryFixed by animateColorAsState(targetColorScheme.onTertiaryFixed, animationSpec = tween(400), label = "onTertiaryFixed")

    val animatedColorScheme = targetColorScheme.copy(
        primary = animatedPrimary,
        background = animatedBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        onBackground = animatedOnBackground,
        scrim = animatedScrim,
        onTertiaryFixed = animatedOnTertiaryFixed
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}
