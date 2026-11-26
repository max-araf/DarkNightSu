package com.kazesu.manager.ui.theme

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import com.kazesu.manager.ui.component.DarkNightSlider
import com.kazesu.manager.ui.component.DarkNightSwitch
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import kotlin.math.roundToInt

data class DarkNightPalette(
    val id: String,
    val name: String,
    val subtitle: String,
    val primaryGlow: Color,
    val secondaryGlow: Color,
    val deepBase: Color,
    val cardSurface: Color = Color(0xCC121620),
    val buttonStart: Color = primaryGlow,
    val buttonEnd: Color = secondaryGlow,
    val hexTop: String,
    val hexBottom: String,
)

object DarkNightPalettes {
    val DarkNightAura = DarkNightPalette(
        id = "darknight_aura",
        name = "DarkNight Aura",
        subtitle = "Electric royal blue & ice glow",
        primaryGlow = Color(0xFF2563FF),
        secondaryGlow = Color(0xFF8BB4FF),
        deepBase = Color(0xFF07090F),
        buttonStart = Color(0xFF1D6BFF),
        buttonEnd = Color(0xFF5D9BFF),
        hexTop = "#2563FF",
        hexBottom = "#07090F",
    )
    val RoyalViolet = DarkNightPalette(
        id = "royal_violet",
        name = "Royal Violet",
        subtitle = "Deep obsidian to electric violet",
        primaryGlow = Color(0xFF9303C5),
        secondaryGlow = Color(0xFFC855F5),
        deepBase = Color(0xFF02060E),
        buttonStart = Color(0xFF7B02A8),
        buttonEnd = Color(0xFFB43BE3),
        hexTop = "#9303C5",
        hexBottom = "#02060E",
    )
    val CrimsonNoir = DarkNightPalette(
        id = "crimson_noir",
        name = "Crimson Noir",
        subtitle = "Obsidian crimson stealth glow",
        primaryGlow = Color(0xFFC50337),
        secondaryGlow = Color(0xFFFF4D7D),
        deepBase = Color(0xFF02060E),
        buttonStart = Color(0xFFA8022E),
        buttonEnd = Color(0xFFE62E5C),
        hexTop = "#C50337",
        hexBottom = "#02060E",
    )
    val ForestNoir = DarkNightPalette(
        id = "forest_noir",
        name = "Forest Noir",
        subtitle = "Deep emerald night gradient",
        primaryGlow = Color(0xFF007D10),
        secondaryGlow = Color(0xFF26C948),
        deepBase = Color(0xFF02060E),
        buttonStart = Color(0xFF006B0E),
        buttonEnd = Color(0xFF1AA636),
        hexTop = "#007D10",
        hexBottom = "#02060E",
    )
    val GoldenBronze = DarkNightPalette(
        id = "golden_bronze",
        name = "Golden Bronze",
        subtitle = "Warm champagne gold & dark bronze",
        primaryGlow = Color(0xFFFFEB97),
        secondaryGlow = Color(0xFFC49A45),
        deepBase = Color(0xFF171008),
        cardSurface = Color(0xCC1E160D),
        buttonStart = Color(0xFF8C5B20),
        buttonEnd = Color(0xFFE5C158),
        hexTop = "#FFEB97",
        hexBottom = "#583714",
    )
    val CyberOcean = DarkNightPalette(
        id = "cyber_ocean",
        name = "Cyber Ocean",
        subtitle = "Midnight navy to neon cyan",
        primaryGlow = Color(0xFF00BBFF),
        secondaryGlow = Color(0xFF66E0FF),
        deepBase = Color(0xFF000221),
        buttonStart = Color(0xFF0088CC),
        buttonEnd = Color(0xFF00CCFF),
        hexTop = "#00BBFF",
        hexBottom = "#000221",
    )
    val AmberGlow = DarkNightPalette(
        id = "amber_glow",
        name = "Amber Glow",
        subtitle = "Solar amber & soft cream shimmer",
        primaryGlow = Color(0xFFFEB42F),
        secondaryGlow = Color(0xFFFFFDCD),
        deepBase = Color(0xFF0E0B06),
        cardSurface = Color(0xCC1C160D),
        buttonStart = Color(0xFFD98B0F),
        buttonEnd = Color(0xFFFEB42F),
        hexTop = "#FEB42F",
        hexBottom = "#FFFDCD",
    )
    val RoyalEmber = DarkNightPalette(
        id = "royal_ember",
        name = "Royal Ember",
        subtitle = "Teal emerald & deep abyssal pine",
        primaryGlow = Color(0xFF068562),
        secondaryGlow = Color(0xFF2CC49A),
        deepBase = Color(0xFF013F4A),
        cardSurface = Color(0xCC06262E),
        buttonStart = Color(0xFF04664B),
        buttonEnd = Color(0xFF0CA67B),
        hexTop = "#068562",
        hexBottom = "#013F4A",
    )
    val MintAurora = DarkNightPalette(
        id = "mint_aurora",
        name = "Mint Aurora",
        subtitle = "Vibrant mint emerald & ice pastel",
        primaryGlow = Color(0xFF07D0A4),
        secondaryGlow = Color(0xFFC2FCD4),
        deepBase = Color(0xFF04120F),
        cardSurface = Color(0xCC0C1E1A),
        buttonStart = Color(0xFF059E7C),
        buttonEnd = Color(0xFF07D0A4),
        hexTop = "#07D0A4",
        hexBottom = "#C2FCD4",
    )
    val RoyalCharcoal = DarkNightPalette(
        id = "royal_charcoal",
        name = "Royal Charcoal",
        subtitle = "Monochrome graphite & slate metal",
        primaryGlow = Color(0xFF4C4E51),
        secondaryGlow = Color(0xFF7B7E84),
        deepBase = Color(0xFF1E1F22),
        cardSurface = Color(0xCC24262A),
        buttonStart = Color(0xFF3A3C40),
        buttonEnd = Color(0xFF5D6066),
        hexTop = "#4C4E51",
        hexBottom = "#1E2023",
    )

    val all: List<DarkNightPalette> = listOf(
        DarkNightAura,
        RoyalViolet,
        CrimsonNoir,
        ForestNoir,
        GoldenBronze,
        CyberOcean,
        AmberGlow,
        RoyalEmber,
        MintAurora,
        RoyalCharcoal,
    )

    fun byId(id: String?): DarkNightPalette = all.firstOrNull { it.id == id } ?: DarkNightAura
}

enum class DarkNightBlurQuality(val id: String, val label: String, val radius: Float) {
    HIGH("high", "High (18dp)", 18f),
    LOW("low", "Low (10dp)", 10f),
    OFF("off", "Off", 0f);

    companion object {
        fun byId(id: String?): DarkNightBlurQuality = entries.firstOrNull { it.id == id } ?: HIGH
    }
}

enum class DarkNightNavStyle(val id: String) {
    BOTTOM_BAR("bottom_bar"),
    GLASS_SIDEBAR("glass_sidebar");

    companion object {
        fun byId(id: String?): DarkNightNavStyle = entries.firstOrNull { it.id == id } ?: BOTTOM_BAR
    }
}

enum class DarkNightDockStyle(val id: String, val label: String) {
    GLASS("glass", "Glass"),
    DARK_SOLID("dark_solid", "Solid");

    companion object {
        fun byId(id: String?): DarkNightDockStyle = entries.firstOrNull { it.id == id } ?: GLASS
    }
}

/**
 * Unified Dark / Light Glass visual recipe (v4.0.0 T1).
 * Every surface, top bar, pill dock, rail, drawer, card, and text color derives from this single object.
 */
enum class ToggleStyle(val id: Int, val displayName: String, val ratio: Float, val originalColor: Color) {
    NEON(0, "Neon Glass", 2.6f, Color(0xFFFF6A1A)),
    TACTILE(1, "Soft Tactile", 1.9f, Color(0xFF53D24B)),
    HARDWARE(2, "Hardware LED", 2.6f, Color(0xFFB8F20A)),
    CLASSIC(3, "Classic ON/OFF", 2.0f, Color(0xFF5BCB6B)),
    RING(4, "Minimal Ring", 2.0f, Color(0xFFFFFFFF)),
    ORB(5, "Glow Orb", 1.9f, Color(0xFFFFD60A)),
    DOTS(6, "Glow Dots", 2.4f, Color(0xFF1FA2FF)),
    LENS(7, "Glass Lens", 2.5f, Color(0xFF8B3DFF));

    companion object {
        fun byId(id: Int): ToggleStyle = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

enum class ToggleColorMode(val id: Int) {
    FOLLOW_PALETTE(0),
    ORIGINAL(1),
    CUSTOM(2);

    companion object {
        fun byId(id: Int): ToggleColorMode = entries.firstOrNull { it.id == id } ?: FOLLOW_PALETTE
    }
}

data class DarkNightToggleConfig(
    val style: ToggleStyle = ToggleStyle.CLASSIC,
    val colorMode: ToggleColorMode = ToggleColorMode.FOLLOW_PALETTE,
    val customColor: Color = Color(0xFF2563FF),
    val size: Int = 1, // 0 = Small, 1 = Medium, 2 = Large
)

val LocalDarkNightToggle = compositionLocalOf { DarkNightToggleConfig() }

/**
 * PART 2: Flat Palette mode tokens when Frosted Glass / Aura is OFF.
 * Palette tint flows through every surface without blur, blobs, or neutral gray fallback.
 */
data class DarkNightFlatTokens(
    val base: Color,
    val surface: Color,
    val surfaceHi: Color,
    val outline: Color,
)

fun flatTokens(a: Color, dark: Boolean, amoled: Boolean): DarkNightFlatTokens =
    if (dark) {
        DarkNightFlatTokens(
            base = if (amoled) Color.Black else lerp(a, Color.Black, 0.84f),
            surface = lerp(a, Color.Black, if (amoled) 0.86f else 0.70f),
            surfaceHi = lerp(a, Color.Black, 0.60f),
            outline = a.copy(alpha = if (amoled) 0.45f else 0.40f),
        )
    } else {
        DarkNightFlatTokens(
            base = lerp(a, Color.White, 0.92f),
            surface = lerp(a, Color.White, 0.84f),
            surfaceHi = lerp(a, Color.White, 0.76f),
            outline = a.copy(alpha = 0.32f),
        )
    }

data class DarkNightGlassTheme(
    val isDark: Boolean,
    val flat: Boolean,
    val base: Color,
    val surface: Color,
    val surfaceHi: Color,
    val outline: Color,
    val accent: Color,
    val accentDark: Color,
    val accentText: Color,
    val lite: Color,
    val fillA: Color,
    val fillB: Color,
    val borderLite: Color,
    val shadow: Color,
    val highlight: Color,
    val content: Color,
    val contentSecondary: Color,
    val systemBarIconsLight: Boolean,
)

fun buildDarkNightGlassTheme(
    palette: DarkNightPalette,
    isDark: Boolean,
    amoled: Boolean,
    flat: Boolean = false,
): DarkNightGlassTheme {
    val a = palette.primaryGlow
    val accentDark = lerp(a, Color(0xFF0B1020), if (a.luminance() > 0.55f) 0.50f else 0.30f)
    val accentText = if (isDark) {
        if (a.luminance() < 0.35f) lerp(a, Color.White, 0.38f) else a
    } else {
        accentDark
    }
    val tokens = flatTokens(a, isDark, amoled)
    if (flat) {
        val content = if (isDark) Color.White.copy(alpha = 0.94f) else Color(0xFF0B1020)
        return DarkNightGlassTheme(
            isDark = isDark,
            flat = true,
            base = tokens.base,
            surface = tokens.surface,
            surfaceHi = tokens.surfaceHi,
            outline = tokens.outline,
            accent = a,
            accentDark = accentDark,
            accentText = accentText,
            lite = lerp(a, Color.White, 0.38f),
            fillA = tokens.surface,
            fillB = tokens.surface,
            borderLite = tokens.outline,
            shadow = Color.Black.copy(alpha = if (isDark) 0.22f else 0.08f),
            highlight = Color.Transparent,
            content = content,
            contentSecondary = content.copy(alpha = if (isDark) 0.74f else 0.70f),
            systemBarIconsLight = !isDark,
        )
    }
    return if (isDark) {
        DarkNightGlassTheme(
            isDark = true,
            flat = false,
            base = if (amoled) Color.Black else palette.deepBase,
            surface = tokens.surface,
            surfaceHi = tokens.surfaceHi,
            outline = tokens.outline,
            accent = a,
            accentDark = accentDark,
            accentText = accentText,
            lite = palette.secondaryGlow,
            fillA = Color.White.copy(alpha = 0.13f),
            fillB = Color.White.copy(alpha = 0.04f),
            borderLite = palette.secondaryGlow,
            shadow = Color.Black.copy(alpha = 0.16f),
            highlight = Color.White.copy(alpha = 0.12f),
            content = Color.White,
            contentSecondary = Color.White.copy(alpha = 0.74f),
            systemBarIconsLight = false,
        )
    } else {
        DarkNightGlassTheme(
            isDark = false,
            flat = false,
            base = Color(0xFFF3F5FB),
            surface = tokens.surface,
            surfaceHi = tokens.surfaceHi,
            outline = tokens.outline,
            accent = a,
            accentDark = accentDark,
            accentText = accentText,
            lite = palette.secondaryGlow,
            fillA = Color.White.copy(alpha = 0.72f),
            fillB = Color.White.copy(alpha = 0.34f),
            borderLite = accentDark,
            shadow = accentDark.copy(alpha = 0.14f),
            highlight = Color.White.copy(alpha = 0.95f),
            content = Color(0xFF0B1020),
            contentSecondary = Color(0xFF0B1020).copy(alpha = 0.70f),
            systemBarIconsLight = true,
        )
    }
}

@Composable
fun rememberDarkNightGlassTheme(
    palette: DarkNightPalette,
    isDark: Boolean,
    amoled: Boolean,
    flat: Boolean = false,
): DarkNightGlassTheme = remember(palette.id, isDark, amoled, flat) {
    buildDarkNightGlassTheme(palette, isDark, amoled, flat)
}

/**
 * C1: Propagates LocalContentColor and LocalTextStyle so child Text/Icon inside any glass or flat card
 * automatically receives high-contrast glass.content.
 */
/**
 * E4 (v8): Applies a crisp 1dp palette-tinted outline in Flat mode (when Frosted Glass is OFF).
 */
@Composable
fun Modifier.darkNightFlatBorder(shape: Shape): Modifier {
    val g = LocalDarkNightGlass.current
    return if (g.flat) this.border(1.dp, g.outline, shape) else this
}

@Composable
fun ProvideDarkNightContent(content: @Composable () -> Unit) {
    val g = LocalDarkNightGlass.current
    CompositionLocalProvider(
        LocalContentColor provides g.content,
        LocalTextStyle provides LocalTextStyle.current.copy(color = g.content),
    ) {
        content()
    }
}

val LocalDarkNightGlass = staticCompositionLocalOf {
    buildDarkNightGlassTheme(DarkNightPalettes.DarkNightAura, isDark = true, amoled = false)
}

/** Shared left clearance when Glass Sidebar Rail is active (80dp). */
val LocalNavRailPadding = staticCompositionLocalOf<Dp> { 0.dp }

/** Shared right clearance when Side Info Bar is pinned open (62dp). */
val LocalSideInfoBarPadding = staticCompositionLocalOf<Dp> { 0.dp }

fun contrastOn(bg: Color): Color = if (bg.luminance() > 0.45f) Color(0xFF0B1020) else Color.White

data class DarkNightThemeState(
    val palette: DarkNightPalette = DarkNightPalettes.DarkNightAura,
    val auraEnabled: Boolean = true,
    val startupAnimEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val navStyle: DarkNightNavStyle = DarkNightNavStyle.BOTTOM_BAR,
    val dockStyle: DarkNightDockStyle = DarkNightDockStyle.GLASS,
    val blurQuality: DarkNightBlurQuality = DarkNightBlurQuality.HIGH,
    val amoledBlack: Boolean = false,
    // PART 3: Side Info Bar preferences
    val infoBarEnabled: Boolean = true,
    val infoBarPinned: Boolean = false,
    val infoBarShowCpu: Boolean = true,
    val infoBarShowBattery: Boolean = true,
    val infoBarShowTemp: Boolean = true,
    val infoBarTempSourceCpu: Boolean = false,
    val infoBarUnitF: Boolean = false,
    val infoBarOpacityPercent: Int = 100,
    val infoBarVerticalPercent: Int = 30,
    // PART 4: Appearance sliders
    val cardCornerRadiusDp: Int = 20,
    val borderHighlightEnabled: Boolean = true,
    val blurRadiusDp: Float = 18f,
    val glassStrength: Float = 1.0f,
    // PART 4: 8-style Toggle preferences
    val toggleStyle: ToggleStyle = ToggleStyle.CLASSIC,
    val toggleColorMode: ToggleColorMode = ToggleColorMode.FOLLOW_PALETTE,
    val toggleCustomColorArgb: Int = 0xFF2563FF.toInt(),
    val toggleSize: Int = 1,
) {
    val glassOn: Boolean get() = auraEnabled && blurQuality != DarkNightBlurQuality.OFF
    val toggleConfig: DarkNightToggleConfig
        get() = DarkNightToggleConfig(
            style = toggleStyle,
            colorMode = toggleColorMode,
            customColor = Color(toggleCustomColorArgb),
            size = toggleSize,
        )
}

object DarkNightThemeStore {
    private const val PREFS_NAME = "darknight_theme_prefs"
    private const val KEY_PALETTE = "palette_id"
    private const val KEY_AURA = "aura_enabled"
    private const val KEY_STARTUP_ANIM = "startup_anim_enabled"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_NAV_STYLE = "nav_style"
    private const val KEY_DOCK_STYLE = "dock_style"
    private const val KEY_BLUR_QUALITY = "blur_quality"
    private const val KEY_AMOLED_BLACK = "amoled_black"
    private const val KEY_INFOBAR_ENABLED = "infobar_enabled"
    private const val KEY_INFOBAR_PINNED = "infobar_pinned"
    private const val KEY_INFOBAR_CPU = "infobar_show_cpu"
    private const val KEY_INFOBAR_BATTERY = "infobar_show_battery"
    private const val KEY_INFOBAR_TEMP = "infobar_show_temp"
    private const val KEY_INFOBAR_TEMP_CPU = "infobar_temp_source_cpu"
    private const val KEY_INFOBAR_UNIT_F = "infobar_unit_f"
    private const val KEY_INFOBAR_OPACITY = "infobar_opacity_pct"
    private const val KEY_INFOBAR_VPOS = "infobar_vertical_pct"
    private const val KEY_CARD_CORNER_DP = "card_corner_dp"
    private const val KEY_BORDER_HIGHLIGHT = "border_highlight_enabled"
    private const val KEY_BLUR_RADIUS_DP = "blur_radius_dp"
    private const val KEY_GLASS_STRENGTH = "glass_strength"
    private const val KEY_TOGGLE_STYLE = "toggle_style"
    private const val KEY_TOGGLE_COLOR_MODE = "toggle_color_mode"
    private const val KEY_TOGGLE_CUSTOM_COLOR = "toggle_custom_color"
    private const val KEY_TOGGLE_SIZE = "toggle_size"

    /** Process-level flag so SplashScreen only plays on cold start, never on theme/UI-style change (T3). */
    @Volatile
    var coldStartSplashShown: Boolean = false

    private val _state = MutableStateFlow(DarkNightThemeState())
    val state: StateFlow<DarkNightThemeState> = _state.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _state.value = DarkNightThemeState(
                palette = DarkNightPalettes.byId(prefs.getString(KEY_PALETTE, DarkNightPalettes.DarkNightAura.id)),
                auraEnabled = prefs.getBoolean(KEY_AURA, true),
                startupAnimEnabled = prefs.getBoolean(KEY_STARTUP_ANIM, true),
                onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_DONE, false),
                navStyle = DarkNightNavStyle.byId(prefs.getString(KEY_NAV_STYLE, DarkNightNavStyle.BOTTOM_BAR.id)),
                dockStyle = DarkNightDockStyle.byId(prefs.getString(KEY_DOCK_STYLE, DarkNightDockStyle.GLASS.id)),
                blurQuality = DarkNightBlurQuality.byId(prefs.getString(KEY_BLUR_QUALITY, DarkNightBlurQuality.HIGH.id)),
                amoledBlack = prefs.getBoolean(KEY_AMOLED_BLACK, false),
                infoBarEnabled = prefs.getBoolean(KEY_INFOBAR_ENABLED, true),
                infoBarPinned = prefs.getBoolean(KEY_INFOBAR_PINNED, false),
                infoBarShowCpu = prefs.getBoolean(KEY_INFOBAR_CPU, true),
                infoBarShowBattery = prefs.getBoolean(KEY_INFOBAR_BATTERY, true),
                infoBarShowTemp = prefs.getBoolean(KEY_INFOBAR_TEMP, true),
                infoBarTempSourceCpu = prefs.getBoolean(KEY_INFOBAR_TEMP_CPU, false),
                infoBarUnitF = prefs.getBoolean(KEY_INFOBAR_UNIT_F, false),
                infoBarOpacityPercent = prefs.getInt(KEY_INFOBAR_OPACITY, 100).coerceIn(50, 100),
                infoBarVerticalPercent = prefs.getInt(KEY_INFOBAR_VPOS, 30).coerceIn(15, 70),
                cardCornerRadiusDp = prefs.getInt(KEY_CARD_CORNER_DP, 20).coerceIn(12, 28),
                borderHighlightEnabled = prefs.getBoolean(KEY_BORDER_HIGHLIGHT, true),
                blurRadiusDp = prefs.getFloat(KEY_BLUR_RADIUS_DP, 18f).coerceIn(0f, 40f),
                glassStrength = prefs.getFloat(KEY_GLASS_STRENGTH, 1.0f).coerceIn(0.3f, 2.0f),
                toggleStyle = ToggleStyle.byId(prefs.getInt(KEY_TOGGLE_STYLE, ToggleStyle.CLASSIC.id)),
                toggleColorMode = ToggleColorMode.byId(prefs.getInt(KEY_TOGGLE_COLOR_MODE, ToggleColorMode.FOLLOW_PALETTE.id)),
                toggleCustomColorArgb = prefs.getInt(KEY_TOGGLE_CUSTOM_COLOR, 0xFF2563FF.toInt()),
                toggleSize = prefs.getInt(KEY_TOGGLE_SIZE, 1).coerceIn(0, 2),
            )
            initialized = true
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun selectPalette(context: Context, palette: DarkNightPalette) {
        init(context)
        prefs(context).edit().putString(KEY_PALETTE, palette.id).apply()
        _state.value = _state.value.copy(palette = palette)
    }

    fun setAuraEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_AURA, enabled).apply()
        _state.value = _state.value.copy(auraEnabled = enabled)
    }

    fun setStartupAnimEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_STARTUP_ANIM, enabled).apply()
        _state.value = _state.value.copy(startupAnimEnabled = enabled)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        _state.value = _state.value.copy(onboardingCompleted = completed)
    }

    fun setNavStyle(context: Context, style: DarkNightNavStyle) {
        init(context)
        prefs(context).edit().putString(KEY_NAV_STYLE, style.id).apply()
        _state.value = _state.value.copy(navStyle = style)
    }

    fun setDockStyle(context: Context, style: DarkNightDockStyle) {
        init(context)
        prefs(context).edit().putString(KEY_DOCK_STYLE, style.id).apply()
        _state.value = _state.value.copy(dockStyle = style)
    }

    fun setBlurQuality(context: Context, quality: DarkNightBlurQuality) {
        init(context)
        prefs(context).edit().putString(KEY_BLUR_QUALITY, quality.id).apply()
        _state.value = _state.value.copy(blurQuality = quality)
    }

    fun setAmoledBlack(context: Context, enabled: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_AMOLED_BLACK, enabled).apply()
        _state.value = _state.value.copy(amoledBlack = enabled)
    }

    fun setInfoBarEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_INFOBAR_ENABLED, enabled).apply()
        _state.value = _state.value.copy(infoBarEnabled = enabled)
    }

    fun setInfoBarPinned(context: Context, pinned: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_INFOBAR_PINNED, pinned).apply()
        _state.value = _state.value.copy(infoBarPinned = pinned)
    }

    fun setInfoBarRing(context: Context, key: String, enabled: Boolean) {
        init(context)
        val cur = _state.value
        val next = when (key) {
            "cpu" -> cur.copy(infoBarShowCpu = enabled)
            "battery" -> cur.copy(infoBarShowBattery = enabled)
            "temp" -> cur.copy(infoBarShowTemp = enabled)
            else -> cur
        }
        // Keep at least one ring visible so the bar is never empty
        if (!next.infoBarShowCpu && !next.infoBarShowBattery && !next.infoBarShowTemp) return
        prefs(context).edit()
            .putBoolean(KEY_INFOBAR_CPU, next.infoBarShowCpu)
            .putBoolean(KEY_INFOBAR_BATTERY, next.infoBarShowBattery)
            .putBoolean(KEY_INFOBAR_TEMP, next.infoBarShowTemp)
            .apply()
        _state.value = next
    }

    fun setInfoBarTempSourceCpu(context: Context, useCpu: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_INFOBAR_TEMP_CPU, useCpu).apply()
        _state.value = _state.value.copy(infoBarTempSourceCpu = useCpu)
    }

    fun setInfoBarUnitF(context: Context, useFahrenheit: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_INFOBAR_UNIT_F, useFahrenheit).apply()
        _state.value = _state.value.copy(infoBarUnitF = useFahrenheit)
    }

    fun setInfoBarOpacity(context: Context, percent: Int) {
        init(context)
        val clamped = percent.coerceIn(50, 100)
        prefs(context).edit().putInt(KEY_INFOBAR_OPACITY, clamped).apply()
        _state.value = _state.value.copy(infoBarOpacityPercent = clamped)
    }

    fun setInfoBarVerticalPercent(context: Context, percent: Int) {
        init(context)
        val clamped = percent.coerceIn(15, 70)
        prefs(context).edit().putInt(KEY_INFOBAR_VPOS, clamped).apply()
        _state.value = _state.value.copy(infoBarVerticalPercent = clamped)
    }

    fun resetInfoBarDefaults(context: Context) {
        init(context)
        prefs(context).edit()
            .putBoolean(KEY_INFOBAR_ENABLED, true)
            .putBoolean(KEY_INFOBAR_PINNED, false)
            .putBoolean(KEY_INFOBAR_CPU, true)
            .putBoolean(KEY_INFOBAR_BATTERY, true)
            .putBoolean(KEY_INFOBAR_TEMP, true)
            .putBoolean(KEY_INFOBAR_TEMP_CPU, false)
            .putBoolean(KEY_INFOBAR_UNIT_F, false)
            .putInt(KEY_INFOBAR_OPACITY, 100)
            .putInt(KEY_INFOBAR_VPOS, 30)
            .apply()
        _state.value = _state.value.copy(
            infoBarEnabled = true,
            infoBarPinned = false,
            infoBarShowCpu = true,
            infoBarShowBattery = true,
            infoBarShowTemp = true,
            infoBarTempSourceCpu = false,
            infoBarUnitF = false,
            infoBarOpacityPercent = 100,
            infoBarVerticalPercent = 30,
        )
    }

    fun setCardCornerRadius(context: Context, radiusDp: Int) {
        init(context)
        val clamped = radiusDp.coerceIn(12, 28)
        prefs(context).edit().putInt(KEY_CARD_CORNER_DP, clamped).apply()
        _state.value = _state.value.copy(cardCornerRadiusDp = clamped)
    }

    fun setBorderHighlight(context: Context, enabled: Boolean) {
        init(context)
        prefs(context).edit().putBoolean(KEY_BORDER_HIGHLIGHT, enabled).apply()
        _state.value = _state.value.copy(borderHighlightEnabled = enabled)
    }

    fun setBlurRadiusDp(context: Context, radiusDp: Float) {
        init(context)
        val clamped = radiusDp.coerceIn(0f, 40f)
        prefs(context).edit().putFloat(KEY_BLUR_RADIUS_DP, clamped).apply()
        _state.value = _state.value.copy(blurRadiusDp = clamped)
    }

    fun setGlassStrength(context: Context, strength: Float) {
        init(context)
        val clamped = strength.coerceIn(0.3f, 2.0f)
        prefs(context).edit().putFloat(KEY_GLASS_STRENGTH, clamped).apply()
        _state.value = _state.value.copy(glassStrength = clamped)
    }

    fun setToggleStyle(context: Context, style: ToggleStyle) {
        init(context)
        prefs(context).edit().putInt(KEY_TOGGLE_STYLE, style.id).apply()
        _state.value = _state.value.copy(toggleStyle = style)
    }

    fun setToggleColorMode(context: Context, mode: ToggleColorMode) {
        init(context)
        prefs(context).edit().putInt(KEY_TOGGLE_COLOR_MODE, mode.id).apply()
        _state.value = _state.value.copy(toggleColorMode = mode)
    }

    fun setToggleCustomColor(context: Context, argb: Int) {
        init(context)
        prefs(context).edit().putInt(KEY_TOGGLE_CUSTOM_COLOR, argb).apply()
        _state.value = _state.value.copy(toggleCustomColorArgb = argb)
    }

    fun setToggleSize(context: Context, size: Int) {
        init(context)
        val clamped = size.coerceIn(0, 2)
        prefs(context).edit().putInt(KEY_TOGGLE_SIZE, clamped).apply()
        _state.value = _state.value.copy(toggleSize = clamped)
    }

    fun activeKeyColorArgb(): Int = _state.value.palette.primaryGlow.toArgb()
}

/**
 * Luminous multi-blob Aura backdrop (supports both Dark Glass and Light Glass recipes, T1/T3).
 * Smooth 300ms color transitions on palette or Light/Dark mode change — zero harsh flash.
 */
@Composable
fun DarkNightAuraBackdrop(
    palette: DarkNightPalette,
    animateOnStart: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val progress = remember(animateOnStart) {
        Animatable(if (animateOnStart) 0f else 1f)
    }
    LaunchedEffect(animateOnStart) {
        if (animateOnStart && progress.value < 1f) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
            )
        }
    }
    val p = progress.value

    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val amoled = themeState.amoledBlack && glass.isDark

    val baseBg by animateColorAsState(
        targetValue = glass.base,
        animationSpec = tween(durationMillis = 300),
        label = "aura_base_bg",
    )
    val accent by animateColorAsState(
        targetValue = palette.primaryGlow,
        animationSpec = tween(durationMillis = 300),
        label = "aura_accent",
    )
    val lite by animateColorAsState(
        targetValue = palette.secondaryGlow,
        animationSpec = tween(durationMillis = 300),
        label = "aura_lite",
    )

    val blobMul = if (amoled) 0.70f else 1.0f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        val w = size.width
        val h = size.height

        if (glass.isDark) {
            // 1. Top-left primary glow (accent @ 0.62)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.62f * p * blobMul),
                        accent.copy(alpha = 0.26f * p * blobMul),
                        Color.Transparent,
                    ),
                    center = Offset(w * (0.16f + 0.10f * p), h * (0.04f + 0.06f * p)),
                    radius = w * (0.90f + 0.18f * p),
                )
            )
            // 2. Upper-right secondary highlight mist
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lite.copy(alpha = 0.42f * p * blobMul),
                        accent.copy(alpha = 0.14f * p * blobMul),
                        Color.Transparent,
                    ),
                    center = Offset(w * (0.88f - 0.12f * p), h * (0.05f + 0.06f * p)),
                    radius = w * 0.65f,
                )
            )
            // 3. Right-mid blob (accent @ 0.34)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.34f * p * blobMul),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.92f, h * 0.46f),
                    radius = w * 0.72f,
                )
            )
            // 4. Left-lower blob (lite @ 0.20)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lite.copy(alpha = 0.20f * p * blobMul),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.12f, h * 0.68f),
                    radius = w * 0.62f,
                )
            )
            // 5. Bottom-right blob (accent @ 0.45)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.45f * p * blobMul),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.85f, h * 0.92f),
                    radius = w * 0.82f,
                )
            )
        } else {
            // Light Glass pastel blobs (+10% saturation/alpha for rich Light glass & dock refraction, B7)
            val softViolet = Color(0xFF7C5CFF)
            val softSky = Color(0xFF4EA8FF)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.44f * p),
                        accent.copy(alpha = 0.18f * p),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.22f, h * 0.08f),
                    radius = w * 0.95f,
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        softSky.copy(alpha = 0.34f * p),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.85f, h * 0.12f),
                    radius = w * 0.72f,
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        softViolet.copy(alpha = 0.26f * p),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.88f, h * 0.50f),
                    radius = w * 0.75f,
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.36f * p),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.18f, h * 0.86f),
                    radius = w * 0.80f,
                )
            )
        }
    }
}

/**
 * Global Liquid Frosted Glass modifier (Dark Glass & Light Glass recipes, T1/T8):
 * - Pass `useBlur = false` on long scrollable lists (DarkTune, Toolbox) to prevent GPU text garbling / jank.
 */
@Composable
fun Modifier.darkNightGlassCardSurface(
    shape: Shape = RoundedCornerShape(20.dp),
    isMonospaceDark: Boolean = false,
    useBlur: Boolean = true,
): Modifier {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val palette = themeState.palette
    val glass = LocalDarkNightGlass.current
    val backdrop = LocalAppBackgroundBackdrop.current
    val blurQuality = themeState.blurQuality
    val effectiveBlurRadius = when (blurQuality) {
        DarkNightBlurQuality.OFF -> 0f
        DarkNightBlurQuality.LOW -> (themeState.blurRadiusDp * 0.55f).coerceAtLeast(6f)
        DarkNightBlurQuality.HIGH -> themeState.blurRadiusDp
    }
    val hasRealBlur = useBlur &&
        themeState.auraEnabled &&
        effectiveBlurRadius > 0.5f &&
        backdrop != null &&
        isRenderEffectSupported()
    val strength = themeState.glassStrength

    val isNeutral = palette.id == DarkNightPalettes.RoyalCharcoal.id
    val isBright = palette.primaryGlow.luminance() > 0.45f
    val borderTint = if (glass.isDark) {
        if (isNeutral) lerp(palette.secondaryGlow, palette.primaryGlow, 0.4f) else palette.secondaryGlow
    } else {
        glass.accentDark
    }
    val maxBorderAlpha = if (glass.isDark) {
        if (isNeutral) 0.30f else 0.40f
    } else {
        0.30f
    }
    val midBorderAlpha = if (glass.isDark) 0.07f else 0.06f
    val endBorderAlpha = if (glass.isDark) 0.16f else 0.14f

    // PART 2: When Frosted Glass / Aura is OFF, render palette-tinted flat surface (zero neutral gray / blur / sheen)
    if (!themeState.glassOn && !isMonospaceDark) {
        return this
            .clip(shape)
            .background(glass.surface)
            .border(1.dp, glass.outline, shape)
    }

    val blurMod = if (hasRealBlur && backdrop != null) {
        this.textureBlur(
            backdrop = backdrop,
            shape = shape,
            blurRadius = effectiveBlurRadius,
            colors = BlurColors(
                blendColors = listOf(
                    BlendColorEntry(
                        color = if (glass.isDark) Color.White.copy(alpha = (0.04f * strength).coerceAtMost(0.18f))
                        else Color.White.copy(alpha = (0.22f * strength).coerceAtMost(0.42f))
                    ),
                ),
            ),
        )
    } else {
        this
    }

    return blurMod
        .clip(shape)
        .drawBehind {
            if (isMonospaceDark) {
                drawRect(if (glass.isDark) Color.Black.copy(alpha = 0.40f) else Color(0xDD111622))
            } else if (!hasRealBlur) {
                if (glass.isDark) {
                    drawRect(glass.base.copy(alpha = 0.62f))
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = (0.14f * strength).coerceAtMost(0.30f)),
                                Color.White.copy(alpha = (0.05f * strength).coerceAtMost(0.14f)),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = (0.82f * strength).coerceIn(0.55f, 0.92f)),
                                Color.White.copy(alpha = (0.55f * strength).coerceIn(0.35f, 0.78f)),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                        )
                    )
                }
            } else {
                if (glass.isDark && isBright) {
                    drawRect(Color.Black.copy(alpha = 0.28f))
                }
                val a1 = (glass.fillA.alpha * strength).coerceIn(0.05f, 0.90f)
                val a2 = (glass.fillB.alpha * strength).coerceIn(0.02f, 0.70f)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(glass.fillA.copy(alpha = a1), glass.fillB.copy(alpha = a2)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    )
                )
            }
        }
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to borderTint.copy(alpha = maxBorderAlpha),
                    0.55f to borderTint.copy(alpha = midBorderAlpha),
                    1.00f to borderTint.copy(alpha = endBorderAlpha),
                ),
                start = Offset(0f, 0f),
                end = Offset(480f, 480f),
            ),
            shape = shape,
        )
        .drawWithContent {
            drawContent()
            if (themeState.borderHighlightEnabled) {
                drawLine(
                    color = glass.highlight,
                    start = Offset(size.width * 0.08f, 1.dp.toPx()),
                    end = Offset(size.width * 0.92f, 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
}

@Composable
fun DarkNightGlassCard(
    palette: DarkNightPalette,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    isMonospaceDark: Boolean = false,
    useBlur: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val resolvedShape = shape ?: RoundedCornerShape(themeState.cardCornerRadiusDp.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressedOverlay = if (isPressed && onClick != null) {
        if (glass.isDark) palette.secondaryGlow.copy(alpha = 0.18f)
        else palette.primaryGlow.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .darkNightGlassCardSurface(
                shape = resolvedShape,
                isMonospaceDark = isMonospaceDark,
                useBlur = useBlur,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .drawWithContent {
                drawContent()
                if (pressedOverlay != Color.Transparent) {
                    drawRect(pressedOverlay)
                }
            },
    ) {
        ProvideDarkNightContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

/**
 * Pill-shaped luminous gradient CTA button.
 */
@Composable
fun DarkNightGradientButton(
    text: String,
    palette: DarkNightPalette,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val brush = Brush.horizontalGradient(
        colors = listOf(
            palette.buttonStart.copy(alpha = if (enabled) 1f else 0.45f),
            palette.buttonEnd.copy(alpha = if (enabled) 1f else 0.45f),
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contrastOn(palette.buttonStart),
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
    }
}

/**
 * First-launch Zentra-inspired animated onboarding overlay.
 */
@Composable
fun DarkNightOnboardingOverlay(
    visible: Boolean,
    palette: DarkNightPalette,
    onFinish: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)),
        exit = fadeOut(tween(400)),
    ) {
        var step by remember { mutableIntStateOf(0) }
        val titles = listOf(
            "Own Your Kernel,\nShape " to "Your Device.",
            "Stealth Root &\nFull " to "Control.",
            "Smart Profiles &\nCustom " to "Themes.",
        )
        val subtitles = listOf(
            "GKI LKM & Non-GKI kernel root with SuSFS, KPM, and built-in Zygisk — all inside DarkNightSu.",
            "Game performance profiles, bypass charging, TrickyStore hub, and 3-strike bootloop rescue.",
            "Switch between 10 aura gradient palettes or explore & upload themes in the online Theme Store.",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.deepBase.copy(alpha = 0.98f))
                .pointerInput(Unit) {}
        ) {
            DarkNightAuraBackdrop(palette = palette, animateOnStart = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    com.kazesu.manager.ui.component.DarkNightLogo(size = 30.dp)
                    Text(
                        text = "DarkNightSu",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val (lead, accent) = titles[step]
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append(lead)
                        }
                        withStyle(SpanStyle(color = palette.secondaryGlow, fontWeight = FontWeight.Bold)) {
                            append(accent)
                        }
                    },
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subtitles[step],
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(titles.size) { idx ->
                        val active = idx == step
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(if (active) 24.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) palette.primaryGlow else Color.White.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                DarkNightGradientButton(
                    text = if (step < titles.lastIndex) "Next" else "Get Started",
                    palette = palette,
                    onClick = {
                        if (step < titles.lastIndex) step++ else onFinish()
                    },
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onFinish)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

/**
 * Embedded 10-Gradient Palette Picker + Aura, Blur, Dock Style & Appearance controls
 * shown directly inside Settings -> Theme -> Colors screen. Fully Light & Dark readable (T1).
 */
@Composable
fun DarkNightGradientPaletteSection(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val active = themeState.palette
    val glass = LocalDarkNightGlass.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DarkNightGlassCard(palette = active, useBlur = false) {
            Text(
                text = "DarkNight Aura & Gradient Palettes",
                color = glass.content,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = "Active: ${active.name} (${active.hexTop}) — applies across every screen",
                color = glass.contentSecondary,
                fontSize = 12.sp,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Global Aura & Frosted Glass", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("Luminous multi-blob gradient & translucent cards", color = glass.contentSecondary, fontSize = 11.sp)
                }
                DarkNightSwitch(
                    checked = themeState.auraEnabled,
                    onCheckedChange = { DarkNightThemeStore.setAuraEnabled(context, it) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Startup Visual Animation", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("Animates smoothly on launch, then settles static", color = glass.contentSecondary, fontSize = 11.sp)
                }
                DarkNightSwitch(
                    checked = themeState.startupAnimEnabled,
                    onCheckedChange = { DarkNightThemeStore.setStartupAnimEnabled(context, it) },
                )
            }

            // Glass Blur Quality
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Glass Blur Quality", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DarkNightBlurQuality.entries.forEach { quality ->
                        val activeQ = themeState.blurQuality == quality
                        val pillShape = RoundedCornerShape(99.dp)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .clickable {
                                    DarkNightThemeStore.setBlurQuality(context, quality)
                                    DarkNightToastManager.show("Blur: ${quality.label}")
                                },
                            shape = pillShape,
                            color = if (activeQ) active.primaryGlow.copy(alpha = if (glass.isDark) 0.28f else 0.18f)
                            else if (glass.isDark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (activeQ) active.primaryGlow.copy(alpha = 0.65f) else glass.borderLite.copy(alpha = 0.20f),
                            ),
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = quality.label,
                                    color = glass.content,
                                    fontSize = 11.sp,
                                    fontWeight = if (activeQ) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AMOLED Pure Black Base", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("Use #000000 backdrop in Dark mode with softer blobs", color = glass.contentSecondary, fontSize = 11.sp)
                }
                DarkNightSwitch(
                    checked = themeState.amoledBlack,
                    onCheckedChange = { DarkNightThemeStore.setAmoledBlack(context, it) },
                )
            }

            // PART 4: Card Corner Radius & Border Highlight
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Card Corner Radius", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("${themeState.cardCornerRadiusDp}dp", color = glass.accent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                DarkNightSlider(
                    value = themeState.cardCornerRadiusDp.toFloat(),
                    onValueChange = { DarkNightThemeStore.setCardCornerRadius(context, it.roundToInt()) },
                    valueRange = 12f..28f,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Blur Radius", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("${themeState.blurRadiusDp.roundToInt()}dp", color = glass.accent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                DarkNightSlider(
                    value = themeState.blurRadiusDp,
                    onValueChange = { DarkNightThemeStore.setBlurRadiusDp(context, it) },
                    valueRange = 0f..40f,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Card Glass Strength", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("${"%.1f".format(themeState.glassStrength)}x", color = glass.accent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                DarkNightSlider(
                    value = themeState.glassStrength,
                    onValueChange = { DarkNightThemeStore.setGlassStrength(context, it) },
                    valueRange = 0.3f..2.0f,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Glass Border Highlight", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("Top 1dp specular edge glint on glass cards", color = glass.contentSecondary, fontSize = 11.sp)
                }
                DarkNightSwitch(
                    checked = themeState.borderHighlightEnabled,
                    onCheckedChange = { DarkNightThemeStore.setBorderHighlight(context, it) },
                )
            }

            // Navigation Style (Pill Dock vs Glass Sidebar)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Navigation Style", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        DarkNightNavStyle.BOTTOM_BAR to "Bottom Pill Dock",
                        DarkNightNavStyle.GLASS_SIDEBAR to "Glass Sidebar",
                    ).forEach { (style, label) ->
                        val activeStyle = themeState.navStyle == style
                        val pillShape = RoundedCornerShape(14.dp)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .clickable {
                                    DarkNightThemeStore.setNavStyle(context, style)
                                    DarkNightToastManager.show("Navigation: $label")
                                },
                            shape = pillShape,
                            color = if (activeStyle) active.primaryGlow.copy(alpha = if (glass.isDark) 0.28f else 0.18f)
                            else if (glass.isDark) Color(0x880D111A) else Color.White.copy(alpha = 0.62f),
                            border = BorderStroke(
                                width = if (activeStyle) 1.5.dp else 1.dp,
                                color = if (activeStyle) active.primaryGlow else glass.borderLite.copy(alpha = 0.22f),
                            ),
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    color = glass.content,
                                    fontSize = 12.sp,
                                    fontWeight = if (activeStyle) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            // PART 2: Pill Dock Style (Glass vs Dark Solid; Light mode auto uses Light Glass)
            if (themeState.navStyle == DarkNightNavStyle.BOTTOM_BAR) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Dock Style", color = glass.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DarkNightDockStyle.entries.forEach { dStyle ->
                            val activeD = themeState.dockStyle == dStyle
                            val pillShape = RoundedCornerShape(14.dp)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(pillShape)
                                    .clickable {
                                        DarkNightThemeStore.setDockStyle(context, dStyle)
                                        DarkNightToastManager.show("Dock: ${dStyle.label}")
                                    },
                                shape = pillShape,
                                color = if (activeD) active.primaryGlow.copy(alpha = if (glass.isDark) 0.28f else 0.18f)
                                else if (glass.isDark) Color(0x880D111A) else Color.White.copy(alpha = 0.62f),
                                border = BorderStroke(
                                    width = if (activeD) 1.5.dp else 1.dp,
                                    color = if (activeD) active.primaryGlow else glass.borderLite.copy(alpha = 0.22f),
                                ),
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dStyle.label,
                                        color = glass.content,
                                        fontSize = 12.sp,
                                        fontWeight = if (activeD) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 10 Gradient Palettes Grid
            DarkNightPalettes.all.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    pair.forEach { pal ->
                        val isSelected = pal.id == active.id
                        val shape = RoundedCornerShape(16.dp)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .clickable {
                                    DarkNightThemeStore.selectPalette(context, pal)
                                    DarkNightToastManager.show("Applied ${pal.name}")
                                },
                            shape = shape,
                            color = if (glass.isDark) Color(0xBB0D111A) else Color.White.copy(alpha = 0.72f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) pal.primaryGlow else glass.borderLite.copy(alpha = 0.18f),
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.verticalGradient(listOf(pal.deepBase, pal.primaryGlow)))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.verticalGradient(listOf(pal.primaryGlow, pal.deepBase)))
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pal.name,
                                            color = glass.content,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                        )
                                        Text(
                                            text = pal.hexTop,
                                            color = glass.contentSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = pal.primaryGlow,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(pal.primaryGlow)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            OutlinedButton(
                onClick = {
                    DarkNightThemeStore.setOnboardingCompleted(context, false)
                    DarkNightToastManager.show("Startup intro will show on next launch")
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, glass.borderLite.copy(alpha = 0.30f)),
            ) {
                Text("Replay Startup Intro Screen", color = glass.content)
            }
        }

        com.kazesu.manager.ui.component.DarkNightToggleStyleSection()
    }
}

/** Shared bottom bar clearance (bottom dock height + system navigation bar + margin). */
val LocalBottomBarPadding = staticCompositionLocalOf<Dp> { 0.dp }

/**
 * Opaque screen wrapper for detail destinations so previous screens in the NavDisplay
 * back stack never ghost/bleed behind Theme, Theme Store, Toolbox, DarkTune, etc. (T7).
 */
@Composable
fun DarkNightScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    DarkNightThemeStore.init(context)
    val state by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.base)
    ) {
        if (state.auraEnabled) {
            DarkNightAuraBackdrop(
                palette = state.palette,
                animateOnStart = false,
            )
        }
        content()
    }
}

/**
 * Single-instance Glass Toast manager: new messages immediately replace the old one (no stacking).
 */
data class DarkNightToastMessage(val id: Long, val text: String)

object DarkNightToastManager {
    private val _current = MutableStateFlow<DarkNightToastMessage?>(null)
    val current: StateFlow<DarkNightToastMessage?> = _current.asStateFlow()

    fun show(text: String) {
        _current.value = DarkNightToastMessage(System.nanoTime(), text)
    }

    fun dismiss(id: Long) {
        if (_current.value?.id == id) {
            _current.value = null
        }
    }
}

/**
 * Glass Toast positioned cleanly 12dp above Pill Dock / bottom nav bar (T9).
 */
@Composable
fun DarkNightGlassToastHost(
    palette: DarkNightPalette,
    modifier: Modifier = Modifier,
) {
    val toast by DarkNightToastManager.current.collectAsStateWithLifecycle()
    val glass = LocalDarkNightGlass.current
    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
    LaunchedEffect(toast?.id) {
        val id = toast?.id ?: return@LaunchedEffect
        delay(1800)
        DarkNightToastManager.dismiss(id)
    }

    val bottomClearance = if (themeState.navStyle == DarkNightNavStyle.BOTTOM_BAR) 104.dp else 28.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = bottomClearance, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(220)),
        ) {
            toast?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (glass.isDark) Color(0xE80A0E17) else Color(0xF2FFFFFF),
                    border = BorderStroke(1.dp, palette.primaryGlow.copy(alpha = 0.65f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(palette.primaryGlow)
                        )
                        Text(
                            text = msg.text,
                            color = glass.content,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
