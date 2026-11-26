package com.kazesu.manager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.kazesu.manager.ui.webui.MonetColorsProvider
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Composable
fun MiuixKernelSUTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val colorStyle = appSettings.paletteStyle
    val colorSpec = appSettings.colorSpec

    val miuixPaletteStyle = try {
        ThemePaletteStyle.valueOf(colorStyle.name)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }

    val miuixColorSpec = if (colorSpec.effectiveFor(colorStyle) == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }

    val resolvedKeyColor: Color? = when {
        appSettings.keyColor != 0 -> Color(appSettings.keyColor)
        appSettings.colorMode.isMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context).primary
            else dynamicLightColorScheme(context).primary
        appSettings.colorMode.isMonet -> FallbackSeedColor
        else -> null
    }

    val isDarkNightAura = LocalBackgroundImagePath.current == "darknight://aura"
    val schemeMode = when {
        isDarkNightAura && darkTheme -> ColorSchemeMode.MonetDark
        isDarkNightAura && !darkTheme -> ColorSchemeMode.MonetLight
        else -> when (appSettings.colorMode) {
            ColorMode.SYSTEM -> ColorSchemeMode.System
            ColorMode.LIGHT -> ColorSchemeMode.Light
            ColorMode.DARK -> ColorSchemeMode.Dark
            ColorMode.MONET_SYSTEM -> ColorSchemeMode.MonetSystem
            ColorMode.MONET_LIGHT -> ColorSchemeMode.MonetLight
            ColorMode.MONET_DARK, ColorMode.DARK_AMOLED -> ColorSchemeMode.MonetDark
        }
    }

    // Re-create ThemeController whenever palette keyColor or darkTheme changes (B3)
    val controller = remember(schemeMode, resolvedKeyColor, darkTheme, miuixPaletteStyle, miuixColorSpec) {
        ThemeController(
            schemeMode,
            keyColor = resolvedKeyColor,
            isDark = darkTheme,
            paletteStyle = miuixPaletteStyle,
            colorSpec = miuixColorSpec,
        )
    }

    MiuixTheme(
        controller = controller,
        content = {
            val glass = LocalDarkNightGlass.current
            LaunchedEffect(glass.systemBarIconsLight) {
                val window = (context as? Activity)?.window ?: return@LaunchedEffect
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = glass.systemBarIconsLight
                    isAppearanceLightNavigationBars = glass.systemBarIconsLight
                }
            }
            val baseColors = MiuixTheme.colorScheme
            val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
            val effectiveAccent = if (!glass.isDark && glass.accent.luminance() > 0.55f) glass.accentDark else glass.accent
            val onAccent = contrastOn(effectiveAccent)

            // Override Miuix primary, containers, and text colors from LocalDarkNightGlass (B2 + B3)
            val readableColors = if (isDarkNightAura) {
                val glassCardFill = if (glass.isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.64f)
                baseColors.copy(
                    primary = effectiveAccent,
                    primaryVariant = effectiveAccent,
                    onPrimary = onAccent,
                    onPrimaryVariant = onAccent,
                    surfaceContainer = glassCardFill,
                    surfaceContainerHigh = glassCardFill,
                    surfaceContainerHighest = glassCardFill,
                    onSurface = glass.content,
                    onBackground = glass.content,
                    onBackgroundVariant = glass.contentSecondary,
                    onSurfaceSecondary = glass.contentSecondary,
                    onSurfaceContainer = glass.content,
                    onSurfaceContainerVariant = glass.contentSecondary,
                    onSurfaceContainerHigh = glass.content,
                    onSurfaceContainerHighest = glass.content,
                    onSurfaceVariantSummary = glass.contentSecondary,
                    onSurfaceVariantActions = glass.contentSecondary,
                    disabledOnSurface = glass.content.copy(alpha = 0.65f),
                    disabledOnSecondary = glass.content.copy(alpha = 0.65f),
                    disabledOnSecondaryVariant = glass.content.copy(alpha = 0.65f),
                )
            } else if (hasCustomBackground) {
                baseColors.copy(
                    disabledOnSurface = baseColors.onSurface.copy(alpha = 0.85f),
                    disabledOnSecondary = baseColors.onSecondary.copy(alpha = 0.85f),
                    disabledOnSecondaryVariant = baseColors.onSurface.copy(alpha = 0.85f),
                )
            } else {
                // PART 2: Flat Palette mode (palette-tinted base/surface/surfaceHi, zero neutral gray)
                baseColors.copy(
                    primary = effectiveAccent,
                    primaryVariant = effectiveAccent,
                    onPrimary = onAccent,
                    onPrimaryVariant = onAccent,
                    background = glass.base,
                    surface = glass.surface,
                    surfaceContainer = glass.surface,
                    surfaceContainerHigh = glass.surfaceHi,
                    surfaceContainerHighest = glass.surfaceHi,
                    onSurface = glass.content,
                    onBackground = glass.content,
                    onBackgroundVariant = glass.contentSecondary,
                    onSurfaceSecondary = glass.contentSecondary,
                    onSurfaceContainer = glass.content,
                    onSurfaceContainerVariant = glass.contentSecondary,
                    onSurfaceContainerHigh = glass.content,
                    onSurfaceContainerHighest = glass.content,
                    onSurfaceVariantSummary = glass.contentSecondary,
                    onSurfaceVariantActions = glass.contentSecondary,
                    disabledOnSurface = glass.content.copy(alpha = 0.55f),
                )
            }
            val overlaidColors = readableColors.withAppearance(appSettings.appearance?.colorsFor(darkTheme))
            MiuixTheme(overlaidColors) {
                MonetColorsProvider.UpdateCss()
                CompositionLocalProvider(
                    LocalContentColor provides MiuixTheme.colorScheme.onBackground,
                ) {
                    content()
                }
            }
        }
    )
}
