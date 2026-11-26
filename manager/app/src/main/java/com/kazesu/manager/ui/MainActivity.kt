package com.kazesu.manager.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.kazesu.manager.ui.component.splash.SplashScreen
import com.kazesu.manager.ui.theme.LocalAppBackgroundBackdrop
import com.kazesu.manager.ui.theme.LocalAppSettings
import com.kazesu.manager.ui.theme.MaterialExpressivePalette
import com.kazesu.manager.ui.theme.LocalBackgroundImagePath
import com.kazesu.manager.ui.theme.LocalCardBackgroundAlpha
import com.kazesu.manager.ui.theme.appearanceImageEffects
import java.io.File
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults.flingBehavior
import androidx.compose.foundation.pager.PagerDefaults.pageNestedScrollConnection
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.kazesu.manager.Natives
import com.kazesu.manager.ui.component.bottombar.BottomBar
import com.kazesu.manager.ui.component.bottombar.MainPagerState
import com.kazesu.manager.ui.component.bottombar.NavigationBadgeState
import com.kazesu.manager.ui.component.bottombar.SideRail
import com.kazesu.manager.ui.component.bottombar.rememberMainPagerState
import com.kazesu.manager.ui.component.bottombar.useNavigationRail
import com.kazesu.manager.ui.kernelFlash.KernelFlashScreen
import com.kazesu.manager.ui.navigation3.IntentDispatcher
import com.kazesu.manager.ui.navigation3.LocalNavigator
import com.kazesu.manager.ui.navigation3.Navigator
import com.kazesu.manager.ui.navigation3.Route
import com.kazesu.manager.ui.navigation3.rememberNavigator
import com.kazesu.manager.ui.screen.about.AboutScreen
import com.kazesu.manager.ai.api.AiFeatureBridge
import com.kazesu.manager.ai.host.ManagerAiTarget
import com.kazesu.manager.ui.screen.appicon.AppIconScreen
import com.kazesu.manager.ui.screen.customtext.CustomTextScreen
import com.kazesu.manager.ui.screen.account.AccountScreen
import com.kazesu.manager.ui.screen.themestore.ThemeStoreScreen
import com.kazesu.manager.ui.screen.themestore.ThemeStoreSourcesScreen
import com.kazesu.manager.ui.util.LocalCustomTexts
import com.kazesu.manager.ui.screen.appprofile.AppProfileScreen
import com.kazesu.manager.ui.screen.backgroundcrop.BackgroundCropScreen
import com.kazesu.manager.ui.screen.colorpalette.AdaptiveThemeBuilderScreen
import com.kazesu.manager.ui.screen.colorpalette.ColorPaletteScreen
import com.kazesu.manager.ui.screen.appearanceoverlay.AppearanceOverlayScreen
import com.kazesu.manager.ui.screen.dynamicmanagers.DynamicManagersScreen
import com.kazesu.manager.ui.screen.extensions.ExtensionsScreen
import com.kazesu.manager.ui.screen.extensions.CpuSpoofScreen
import com.kazesu.manager.ui.screen.extensions.VersionSpoofScreen
import androidx.compose.ui.graphics.toArgb
import com.kazesu.manager.ui.screen.darknight.DarkNightThemeStudioScreen
import com.kazesu.manager.ui.screen.darknight.DarkNightToolboxScreen
import com.kazesu.manager.ui.theme.DarkNightAuraBackdrop
import com.kazesu.manager.ui.theme.DarkNightOnboardingOverlay
import androidx.compose.ui.graphics.Brush
import com.kazesu.manager.ui.theme.DarkNightGlassToastHost
import com.kazesu.manager.ui.theme.DarkNightScreenBackground
import com.kazesu.manager.ui.component.DarkNightGlassSidebarOverlay
import com.kazesu.manager.ui.component.DarkNightPillDock
import com.kazesu.manager.ui.component.DarkNightSideInfoBarOverlay
import com.kazesu.manager.ui.screen.darknight.DarkNightSideInfoBarSettingsScreen
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.LocalDarkNightToggle
import com.kazesu.manager.ui.theme.LocalSideInfoBarPadding
import com.kazesu.manager.ui.theme.rememberDarkNightGlassTheme
import com.kazesu.manager.ui.theme.DarkNightNavStyle
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.LocalNavRailPadding
import com.kazesu.manager.ui.theme.LocalBottomBarPadding
import com.kazesu.manager.ui.screen.tune.TuneScreen
import com.kazesu.manager.ui.screen.executemoduleaction.ExecuteModuleActionScreen
import com.kazesu.manager.ui.screen.flash.FlashScreen
import com.kazesu.manager.ui.screen.home.HomePager
import com.kazesu.manager.ui.screen.install.InstallScreen
import com.kazesu.manager.ui.screen.kernelmodules.KernelModulesScreen
import com.kazesu.manager.ui.screen.kpm.KpmScreen
import com.kazesu.manager.ui.screen.module.ModulePager
import com.kazesu.manager.ui.screen.modulerepo.ModuleRepoDetailScreen
import com.kazesu.manager.ui.screen.modulerepo.ModuleRepoScreen
import com.kazesu.manager.ui.screen.scriptrun.ScriptRunScreen
import com.kazesu.manager.ui.screen.settings.SettingPager
import com.kazesu.manager.ui.screen.settings.tools.ToolsScreen
import com.kazesu.manager.ui.screen.sulog.SulogScreen
import com.kazesu.manager.ui.screen.superuser.SuperUserPager
import com.kazesu.manager.ui.screen.susfs.SuSFSScreen
import com.kazesu.manager.ui.screen.template.AppProfileTemplateScreen
import com.kazesu.manager.ui.screen.templateeditor.TemplateEditorScreen
import com.kazesu.manager.ui.screen.umountmanager.UmountManagerScreen
import com.kazesu.manager.ui.screen.builtinzygisk.BuiltinZygiskScreen
import com.kazesu.manager.ui.theme.AiAssistantMaterialTheme
import com.kazesu.manager.ui.theme.KernelSUTheme
import com.kazesu.manager.ui.theme.LocalColorMode
import com.kazesu.manager.ui.theme.LocalEnableBlur
import com.kazesu.manager.ui.theme.LocalEnableFloatingBottomBar
import com.kazesu.manager.ui.theme.LocalEnableFloatingBottomBarBlur
import com.kazesu.manager.ui.theme.LocalEnableNavigationBadge
import com.kazesu.manager.ui.theme.LocalEnableProgressiveBlur
import com.kazesu.manager.ui.theme.LocalModuleDescriptionMaxLines
import com.kazesu.manager.ui.util.BackgroundNavTransition
import com.kazesu.manager.ui.util.ImageStorage
import com.kazesu.manager.ui.util.LocaleHelper
import com.kazesu.manager.ui.util.ThemeTrialManager
import com.kazesu.manager.ui.util.getSuperuserCount
import com.kazesu.manager.ui.util.install
import com.kazesu.manager.ui.util.rememberBlurBackdrop
import com.kazesu.manager.ui.util.rememberContentReady
import com.kazesu.manager.ui.viewmodel.MainActivityViewModel
import com.kazesu.manager.ui.viewmodel.MainPagerConfig
import com.kazesu.manager.ui.viewmodel.ModuleViewModel
import com.kazesu.manager.ui.viewmodel.SettingsViewModel
import com.kazesu.manager.ui.viewmodel.SuperUserViewModel
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import androidx.core.graphics.drawable.toDrawable
import com.kazesu.manager.R


class MainActivity : ComponentActivity() {
    private val intentChannel = Channel<Intent>(capacity = Channel.BUFFERED)

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (ThemeTrialManager.isActive(this)) {
            ViewModelProvider(this)[SettingsViewModel::class.java]
                .restoreThemeTrial(this, taskId)
        } else {
            ThemeTrialManager.cleanupOrphan(this)
        }

        val isManager = Natives.isManager
        if (isManager && Natives.kernelUAPIVersion == Natives.managerUAPIVersion) install()

        if (savedInstanceState == null) intent?.let { intentChannel.trySend(it) }

        setContent {
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
            val appSettings = uiState.appSettings
            val uiMode = uiState.uiMode
            val darkMode = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            // Pick up launcher wallpaper changes while the "use system wallpaper" option is on.
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSystemWallpaper()
                // Also covers a mode change that happened while the app was in the background.
                viewModel.syncAdaptiveTheme(darkMode)
            }

            // Adaptive theme packages carry a light and a dark half; swap them with the effective
            // light/dark mode (system-following or a forced app mode).
            LaunchedEffect(darkMode) {
                viewModel.syncAdaptiveTheme(darkMode)
            }

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            val navigator = rememberNavigator(Route.Main)
            val context = LocalContext.current
            val uriHandler = LocalUriHandler.current
            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, uiState.pageScale) {
                Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
            }
            // Bars sample the page backdrop, which is transparent wherever the page paints nothing.
            // Blurring that reads as black, so over a custom background they also need the wallpaper:
            // capture it here, where only the background image is drawn, and combine it underneath.
            val appBackgroundBackdrop = rememberLayerBackdrop()
            // The progressive blur covers both shells and works regardless of the plain bar blur.
            val progressiveBlurActive = uiState.enableProgressiveBlur && isRuntimeShaderSupported()
            val captureAppBackground = uiState.customBackgroundUri.isNotEmpty() &&
                ((uiMode == UiMode.Miuix && uiState.enableBlur) || progressiveBlurActive)

            DarkNightThemeStore.init(context)
            val darkNightTheme by DarkNightThemeStore.state.collectAsStateWithLifecycle()
            val darkNightGlass = rememberDarkNightGlassTheme(
                palette = darkNightTheme.palette,
                isDark = darkMode,
                amoled = darkNightTheme.amoledBlack,
                flat = !darkNightTheme.glassOn,
            )
            val sideInfoBarPadding = if (darkNightTheme.infoBarEnabled && darkNightTheme.infoBarPinned) 62.dp else 0.dp
            var sidebarExpanded by remember { mutableStateOf(false) }

            LaunchedEffect(darkNightGlass.base) {
                window.setBackgroundDrawable(
                    android.graphics.drawable.ColorDrawable(darkNightGlass.base.toArgb())
                )
            }

            // B11: One-time cleanup of legacy Toolbox bootloop guard scripts
            LaunchedEffect(Unit) {
                com.kazesu.manager.ui.screen.darknight.DarkNightToolboxRepository.migrateLegacyBootloopGuard(context)
            }

            val themedAppSettings = remember(appSettings, uiState.keyColorOverride, darkNightTheme) {
                val accent = uiState.keyColorOverride
                    ?: if (darkNightTheme.auraEnabled) darkNightTheme.palette.primaryGlow.toArgb() else appSettings.keyColor
                appSettings.copy(keyColor = accent)
            }
            CompositionLocalProvider(
                LocalDarkNightGlass provides darkNightGlass,
                LocalDarkNightToggle provides darkNightTheme.toggleConfig,
                LocalSideInfoBarPadding provides sideInfoBarPadding,
                LocalUriHandler provides object : UriHandler {
                    override fun openUri(uri: String) {
                        try {
                            uriHandler.openUri(uri)
                        } catch (_: IllegalArgumentException) {
                            Toast.makeText(context, R.string.external_link_open_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                LocalNavigator provides navigator,
                LocalDensity provides density,
                LocalColorMode provides appSettings.colorMode.value,
                LocalAppSettings provides themedAppSettings,
                LocalEnableBlur provides uiState.enableBlur,
                LocalEnableProgressiveBlur provides uiState.enableProgressiveBlur,
                LocalEnableFloatingBottomBar provides uiState.enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides uiState.enableFloatingBottomBarBlur,
                LocalEnableNavigationBadge provides uiState.enableNavigationBadge,
                LocalModuleDescriptionMaxLines provides uiState.moduleDescriptionMaxLines,
                LocalBackgroundImagePath provides (uiState.customBackgroundUri.ifEmpty { if (darkNightTheme.auraEnabled) "darknight://aura" else "" }),
                LocalAppBackgroundBackdrop provides appBackgroundBackdrop.takeIf { captureAppBackground || (uiState.customBackgroundUri.isEmpty() && darkNightTheme.auraEnabled) },
                LocalCardBackgroundAlpha provides (if (uiState.customBackgroundUri.isEmpty() && darkNightTheme.auraEnabled) 0.58f else (uiState.cardBackgroundAlphaPercent / 100f)),
                LocalUiMode provides uiMode,
                LocalCustomTexts provides uiState.customTexts,
            ) {
                KernelSUTheme(appSettings = themedAppSettings, uiMode = uiMode) {
                    IntentDispatcher(intentChannel = intentChannel)
                    val swipeDismiss = if (uiState.enableSwipeDismiss) {
                        if (LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) {
                            NavSwipeDirection.RightToLeft
                        } else {
                            NavSwipeDirection.LeftToRight
                        }
                    } else {
                        NavSwipeDirection.None
                    }
                    val flashRoute = navigator.current() as? Route.Flash
                    var flashBackEnabled by remember(flashRoute) { mutableStateOf(false) }
                    val scriptRunRoute = navigator.current() as? Route.ScriptRun
                    var scriptRunBackEnabled by remember(scriptRunRoute) { mutableStateOf(true) }
                    // Read here, not inside the NavDisplay builder: NavDisplay only rebuilds its entries
                    // (and their swipeDismiss) when the builder lambda changes, so it must capture the value.
                    val scriptRunSwipeDismiss = if (scriptRunBackEnabled) swipeDismiss else NavSwipeDirection.None
                    val mainScreenEntry = @Composable {
                        MainScreen(
                            initialPage = selectedMainPage,
                            pagerInterceptionMode = uiState.pagerInterceptionMode,
                            onPageChanged = viewModel::setSelectedMainPage,
                            onSidebarExpandedChange = { sidebarExpanded = it },
                        )
                    }

                    val navDisplay = @Composable {
                        val backgroundActive = uiState.customBackgroundUri.isNotEmpty()
                        NavDisplay(
                            backStack = navigator.backStack,
                            transition = if (backgroundActive) {
                                BackgroundNavTransition
                            } else {
                                NavTransitions.MiuixDefault
                            },
                            effects = NavDisplayEffects(
                                cornerClipRadius = rememberNavSystemCornerRadius(),
                                // The dim scrim sits under the top destination even when idle; an
                                // opaque page hid it, but a transparent one would stay darkened.
                                dimAmount = if (backgroundActive) 0f else 0.5f,
                            ),
                            onBack = {
                                when (val top = navigator.current()) {
                                    is Route.TemplateEditor -> {
                                        if (!top.readOnly) {
                                            navigator.setResult("template_edit", true)
                                        } else {
                                            navigator.pop()
                                        }
                                    }

                                    else -> navigator.pop()
                                }
                            }) {
                            entry<Route.Main>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.About>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { AboutScreen() } }
                            entry<Route.Sulog>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { SulogScreen() } }
                            entry<Route.ColorPalette>(swipeDismiss = swipeDismiss) { key -> DarkNightScreenBackground { ColorPaletteScreen(key.section, key.importUri) } }
                            entry<Route.AppearanceOverlay>(swipeDismiss = swipeDismiss) {
                                DarkNightScreenBackground { AppearanceOverlayScreen(onBack = { navigator.pop() }) }
                            }
                            entry<Route.CustomTexts>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { CustomTextScreen() } }
                            entry<Route.ThemeStore>(swipeDismiss = swipeDismiss) { key ->
                                MaterialExpressivePalette {
                                    DarkNightScreenBackground { ThemeStoreScreen(initialThemeId = key.themeId, initialSourceBaseUrl = key.sourceBaseUrl) }
                                }
                            }
                            entry<Route.ThemeStoreAdaptiveUpload>(swipeDismiss = swipeDismiss) {
                                MaterialExpressivePalette {
                                    DarkNightScreenBackground { ThemeStoreScreen(startAdaptiveUpload = true) }
                                }
                            }
                            entry<Route.ThemeStoreCurrentUpload>(swipeDismiss = swipeDismiss) {
                                MaterialExpressivePalette {
                                    DarkNightScreenBackground { ThemeStoreScreen(startCurrentUpload = true) }
                                }
                            }
                            entry<Route.ThemeStoreSources>(swipeDismiss = swipeDismiss) {
                                DarkNightScreenBackground { MaterialExpressivePalette { ThemeStoreSourcesScreen() } }
                            }
                            entry<Route.ThemeAccount>(swipeDismiss = swipeDismiss) {
                                DarkNightScreenBackground { MaterialExpressivePalette { AccountScreen() } }
                            }
                            entry<Route.AdaptiveThemeBuilder>(swipeDismiss = swipeDismiss) {
                                DarkNightScreenBackground { MaterialExpressivePalette { AdaptiveThemeBuilderScreen() } }
                            }
                            entry<Route.AppIcon>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { AppIconScreen() } }
                            entry<Route.BackgroundCrop>(swipeDismiss = NavSwipeDirection.None) { key ->
                                DarkNightScreenBackground { BackgroundCropScreen(key.sourcePath) }
                            }
                            entry<Route.AppProfileTemplate>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { AppProfileTemplateScreen() } }
                            entry<Route.TemplateEditor>(swipeDismiss = swipeDismiss) { key -> DarkNightScreenBackground { TemplateEditorScreen(key.template, key.readOnly) } }
                            entry<Route.AppProfile>(swipeDismiss = swipeDismiss) { key -> DarkNightScreenBackground { AppProfileScreen(key.uid) } }
                            entry<Route.ModuleRepo>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { ModuleRepoScreen() } }
                            entry<Route.ModuleRepoDetail>(swipeDismiss = swipeDismiss) { key -> DarkNightScreenBackground { ModuleRepoDetailScreen(key.module) } }
                            entry<Route.Install>(swipeDismiss = swipeDismiss) { key -> DarkNightScreenBackground { InstallScreen(preselectedKernelUri = key.preselectedKernelUri) } }
                            entry<Route.Flash>(swipeDismiss = if (flashBackEnabled) swipeDismiss else NavSwipeDirection.None) { key ->
                                DarkNightScreenBackground {
                                    FlashScreen(
                                        key.flashIt,
                                        onBackEnabledChange = { enabled ->
                                            if (navigator.current() == key) flashBackEnabled = enabled
                                        },
                                    )
                                }
                            }
                            entry<Route.ExecuteModuleAction>(swipeDismiss = swipeDismiss) { key ->
                                DarkNightScreenBackground {
                                    ExecuteModuleActionScreen(
                                        key.moduleId,
                                        key.fromShortcut
                                    )
                                }
                            }
                            entry<Route.ScriptRun>(swipeDismiss = scriptRunSwipeDismiss) { key ->
                                DarkNightScreenBackground {
                                    ScriptRunScreen(
                                        key.arg,
                                        onBackEnabledChange = { enabled ->
                                            if (navigator.current() == key) scriptRunBackEnabled = enabled
                                        },
                                    )
                                }
                            }
                            entry<Route.Home>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.SuperUser>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.Module>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.Settings>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.KernelFlash>(swipeDismiss = swipeDismiss)  { key ->
                                DarkNightScreenBackground {
                                    KernelFlashScreen(
                                        key.kernelUri,
                                        key.selectedSlot,
                                        key.kpmPatchEnabled,
                                        key.kpmUndoPatch
                                    )
                                }
                            }
                                entry<Route.Kpm>(swipeDismiss = swipeDismiss)  { DarkNightScreenBackground { KpmScreen() } }
                                entry<Route.SuSFS>(swipeDismiss = swipeDismiss)  { key -> DarkNightScreenBackground { SuSFSScreen(key.tab) } }
                                entry<Route.Tool>(swipeDismiss = swipeDismiss)  { DarkNightScreenBackground { ToolsScreen() } }
                                entry<Route.UmountManager>(swipeDismiss = swipeDismiss)  { DarkNightScreenBackground { UmountManagerScreen() } }
                                entry<Route.KernelModules>(swipeDismiss = swipeDismiss)  { DarkNightScreenBackground { KernelModulesScreen() } }
                                entry<Route.DynamicManagers>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { DynamicManagersScreen() } }
                                entry<Route.Extensions>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { ExtensionsScreen() } }
                                entry<Route.Tune>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { TuneScreen() } }
                                entry<Route.DarkNightToolbox>(swipeDismiss = swipeDismiss) { DarkNightToolboxScreen() }
                                entry<Route.DarkNightThemeStudio>(swipeDismiss = swipeDismiss) { DarkNightThemeStudioScreen() }
                                entry<Route.DarkNightSideInfoBarSettings>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { DarkNightSideInfoBarSettingsScreen() } }
                                entry<Route.CpuSpoof>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { CpuSpoofScreen() } }
                                entry<Route.VersionSpoof>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { VersionSpoofScreen() } }
                                entry<Route.BuiltinZygisk>(swipeDismiss = swipeDismiss) { DarkNightScreenBackground { BuiltinZygiskScreen() } }
                                entry<Route.AiAssistant>(swipeDismiss = swipeDismiss) {
                                    if (!AiFeatureBridge.isAvailable) {
                                        LaunchedEffect(Unit) { navigator.pop() }
                                    } else {
                                        // The assistant is always MD3; inside the Miuix shell it needs
                                        // the MD3 colour scheme of the current appearance settings.
                                        AiAssistantMaterialTheme {
                                            AiFeatureBridge.ChatScreen(
                                                onClose = {
                                                    if (navigator.current() == Route.AiAssistant) navigator.pop()
                                                },
                                                onOpenSettings = { navigator.push(Route.AiSettings) },
                                                onNavigate = { target ->
                                                    when (target) {
                                                        is ManagerAiTarget.Tab -> {
                                                            viewModel.openMainPage(target.pageIndex, target.section)
                                                            navigator.popUntil { it is Route.Main }
                                                        }

                                                        is ManagerAiTarget.Push -> navigator.navigateTo(target.route)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                                entry<Route.AiSettings>(swipeDismiss = swipeDismiss) {
                                    if (!AiFeatureBridge.isAvailable) {
                                        LaunchedEffect(Unit) { navigator.pop() }
                                    } else {
                                        AiAssistantMaterialTheme {
                                            AiFeatureBridge.SettingsScreen(
                                                onBack = { navigator.pop() },
                                            )
                                        }
                                    }
                                }
                        }
                    }

                    var appContentReady by remember { mutableStateOf(false) }
                    var splashDismissed by remember { mutableStateOf(DarkNightThemeStore.coldStartSplashShown) }

                    SideEffect {
                        if (!appContentReady) appContentReady = true
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val hasCustomBackground = uiState.customBackgroundUri.isNotEmpty()
                        val backgroundBlurRadius = (
                            ImageStorage.BACKGROUND_BLUR_MAX_RADIUS_DP * uiState.customBackgroundBlurPercent / 100f
                        ).dp
                        val backgroundEffects = themedAppSettings.appearance?.background
                        Crossfade(
                            modifier = if (captureAppBackground) {
                                Modifier.layerBackdrop(appBackgroundBackdrop)
                            } else {
                                Modifier
                            },
                            targetState = uiState.customBackgroundUri to uiState.customBackgroundUpdatedAt,
                            label = "global_background",
                        ) { (path, _) ->
                            if (path.isNotEmpty() && !path.startsWith("darknight://")) {
                                val context = LocalContext.current
                                val stamp = File(path).lastModified()
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(path))
                                        .crossfade(true)
                                        .memoryCacheKey("$path@$stamp")
                                        .diskCacheKey("$path@$stamp")
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(
                                            if (backgroundBlurRadius > 0.dp) {
                                                Modifier.blur(backgroundBlurRadius)
                                            } else {
                                                Modifier
                                            }
                                        )
                                        // Sharpen after a blur would just fight the blur, so only
                                        // apply it while the background is unblurred.
                                        .appearanceImageEffects(
                                            backgroundEffects,
                                            allowSharpen = backgroundBlurRadius <= 0.dp,
                                        )
                                )
                                val scrimPercent = uiState.customBackgroundScrimPercent
                                if (scrimPercent != 0) {
                                    val scrimColor = if (scrimPercent < 0) {
                                        Color.Black.copy(alpha = -scrimPercent / 100f)
                                    } else {
                                        Color.White.copy(alpha = scrimPercent / 100f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(scrimColor)
                                    )
                                }
                            }
                        }

                        val showDarkNightAura = !hasCustomBackground && darkNightTheme.auraEnabled
                        if (showDarkNightAura) {
                            DarkNightAuraBackdrop(
                                palette = darkNightTheme.palette,
                                animateOnStart = darkNightTheme.startupAnimEnabled,
                                modifier = Modifier.layerBackdrop(appBackgroundBackdrop),
                            )
                        } else if (!hasCustomBackground) {
                            // T7: opaque base even when Aura is OFF (zero ghosting)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(darkNightGlass.base)
                            )
                        }

                        // C4: Smooth 160ms Crossfade on Material <-> Miuix switch over opaque glass.base (zero white flash)
                        Crossfade(
                            targetState = uiMode,
                            animationSpec = tween(durationMillis = 160),
                            label = "ui_mode_crossfade",
                        ) { activeUiMode ->
                            when (activeUiMode) {
                                UiMode.Material -> androidx.compose.material3.Scaffold(
                                    containerColor = if (hasCustomBackground || showDarkNightAura) Color.Transparent else darkNightGlass.base
                                ) { navDisplay() }

                                UiMode.Miuix -> Scaffold(
                                    containerColor = if (hasCustomBackground || showDarkNightAura) Color.Transparent else darkNightGlass.base
                                ) { navDisplay() }
                            }
                        }

                        // PART 3: Right-edge Side Info Bar (hidden during splash, onboarding, or open drawer)
                        if (splashDismissed && darkNightTheme.onboardingCompleted && !sidebarExpanded) {
                            DarkNightSideInfoBarOverlay(
                                backdrop = appBackgroundBackdrop.takeIf { showDarkNightAura || captureAppBackground },
                                onOpenFullSettings = { navigator.push(Route.DarkNightSideInfoBarSettings) },
                            )
                        }

                        DarkNightGlassToastHost(palette = darkNightTheme.palette)

                        com.kazesu.manager.ui.screen.onboarding.DarkNightFirstRunOnboarding(
                            visible = !darkNightTheme.onboardingCompleted,
                            onFinished = { DarkNightThemeStore.setOnboardingCompleted(context, true) },
                        )

                        if (!splashDismissed) {
                            SplashScreen(
                                isReady = appContentReady,
                                onDismissed = {
                                    DarkNightThemeStore.coldStartSplashShown = true
                                    splashDismissed = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentChannel.trySend(intent)
    }
}

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    initialPage: Int = 0,
    pagerInterceptionMode: Int = PagerInterceptionMode.CrossAxisInterceptor.ordinal,
    onPageChanged: (Int) -> Unit = {},
    onSidebarExpandedChange: (Boolean) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val enableBlur = LocalEnableBlur.current
    val enableProgressiveBlur = LocalEnableProgressiveBlur.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val useNavigationRail = useNavigationRail(enableFloatingBottomBar)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { MainPagerConfig.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(
        pagerState = pagerState,
        animatePageChanges = !useNavigationRail,
    )
    // The AI assistant can request a tab from outside MainScreen; follow the
    // selected page when it changes without a pager gesture.
    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPage) {
            mainPagerState.animateToPage(initialPage)
        }
    }
    var isFullFeatured by remember { mutableStateOf(Natives.isFullFeatured()) }
    LifecycleResumeEffect(Unit) {
        isFullFeatured = Natives.isFullFeatured()
        onPauseOrDispose { }
    }
    LaunchedEffect(isFullFeatured, mainPagerState.selectedPage) {
        if (!isFullFeatured && mainPagerState.selectedPage in 1..2) {
            mainPagerState.animateToPage(0)
        }
    }
    val pagerMode = PagerInterceptionMode.entries.getOrElse(pagerInterceptionMode) {
        PagerInterceptionMode.Native
    }
    val interceptPagerGestures = pagerMode == PagerInterceptionMode.CrossAxisInterceptor
    var userScrollEnabled by remember(isFullFeatured) { mutableStateOf(isFullFeatured) }

    val enableNavigationBadge = LocalEnableNavigationBadge.current
    val badgeEnabled = enableNavigationBadge && isFullFeatured
    val moduleViewModel = viewModel<ModuleViewModel>()
    val moduleUiState by moduleViewModel.uiState.collectAsStateWithLifecycle()

    val superUserViewModel = viewModel<SuperUserViewModel>()
    val grantedUidCount by remember(superUserViewModel) {
        superUserViewModel.uiState
            .map { state -> state.groupedApps.count { it.anyAllowSu } }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(0)

    var startupPreloadStarted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isFullFeatured) {
        if (!isFullFeatured || startupPreloadStarted) {
            return@LaunchedEffect
        }

        moduleViewModel.initializePreferences()
        val moduleState = moduleViewModel.uiState.value
        if (!moduleState.hasLoaded) {
            if (!moduleState.isRefreshing) moduleViewModel.fetchModuleList()
            moduleViewModel.uiState.first { it.hasLoaded }
        }
        moduleViewModel.syncModuleUpdateInfo(moduleViewModel.uiState.value.modules)

        val superUserState = superUserViewModel.uiState.value
        if (!superUserState.hasLoaded) {
            superUserViewModel.initializePreferences()
            if (superUserState.isRefreshing) {
                superUserViewModel.uiState.first { it.hasLoaded }
            } else {
                superUserViewModel.loadAppList().join()
            }
        }

        startupPreloadStarted = true
    }

    // Loading the app list just for a badge is too expensive; read the kernel allowlist instead.
    var superuserCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(badgeEnabled, grantedUidCount) {
        superuserCount = if (badgeEnabled) withContext(Dispatchers.IO) { getSuperuserCount() } else 0
    }

    val navigationBadge = if (badgeEnabled) {
        NavigationBadgeState(
            superuserCount = superuserCount,
            moduleEnabledCount = moduleUiState.modules.count { it.enabled },
            moduleUpdatableCount = moduleUiState.updateInfo.count { it.value.downloadUrl.isNotBlank() },
        )
    } else {
        NavigationBadgeState()
    }
    val uiMode = LocalUiMode.current
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val cardAlpha = LocalCardBackgroundAlpha.current
    val surfaceColor = when (uiMode) {
        UiMode.Material -> MaterialTheme.colorScheme.surfaceContainer // Matches the Material page background behind the glass bar
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
    // This backdrop only feeds the standard Miuix navigation bar. The floating bars use
    // `backdrop` for their liquid glass or progressive band, so the pager is only recorded into
    // a full-screen layer when one of those readers is active.
    val needsStandardMiuixBarBackdrop = uiMode == UiMode.Miuix && !enableFloatingBottomBar &&
        !useNavigationRail
    val blurBackdrop = rememberBlurBackdrop(
        enableBlur = enableBlur && needsStandardMiuixBarBackdrop,
        allowProgressive = needsStandardMiuixBarBackdrop,
    )
    // The floating pills (Material bar, Miuix floating bar) sample this pager layer for their
    // liquid glass and/or progressive blur. Rail layouts show no bottom bar at all.
    val needsBottomBarBackdrop = !useNavigationRail &&
        (uiMode == UiMode.Material || enableFloatingBottomBar) &&
        (enableFloatingBottomBarBlur || enableProgressiveBlur)

    val backdrop = rememberLayerBackdrop {
        if (!hasCustomBackground) {
            drawRect(surfaceColor)
        } else {
            drawRect(surfaceColor.copy(alpha = cardAlpha))
        }
        drawContent()
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    MainScreenBackHandler(mainPagerState, navController)

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState
    ) {
        val contentReady = rememberContentReady()
        val pagerContent = @Composable { bottomInnerPadding: Dp ->
            Box(modifier = if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier) {
                HorizontalPager(
                    modifier = Modifier
                        .pagerGestureOverride(
                            pagerState = mainPagerState.pagerState,
                            mode = pagerMode,
                            enabled = userScrollEnabled,
                        )
                        // Material always floats its bar, so only the glass/progressive band matters there.
                        .then(if (needsBottomBarBackdrop) Modifier.layerBackdrop(backdrop) else Modifier),
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = if (contentReady) 3 else 0,
                    overscrollEffect = null,
                    userScrollEnabled = userScrollEnabled && !interceptPagerGestures,
                    pageNestedScrollConnection = if (interceptPagerGestures) {
                        PagerGestureNestedScrollConnection
                    } else {
                        pageNestedScrollConnection(
                            state = mainPagerState.pagerState,
                            orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                        )
                    },
                    flingBehavior = flingBehavior(
                        state = mainPagerState.pagerState,
                        snapAnimationSpec = PagerNavigationSpringSpec,
                    ),
                ) { page ->
                    val isCurrentPage = page == settledPage
                    when (page) {
                        0 -> if (contentReady || isCurrentPage) HomePager(navController, bottomInnerPadding, isCurrentPage)
                        1 -> if (isFullFeatured && (contentReady || isCurrentPage)) SuperUserPager(navController, bottomInnerPadding, isCurrentPage)
                        2 -> if (isFullFeatured && (contentReady || isCurrentPage)) ModulePager(bottomInnerPadding, isCurrentPage)
                        3 -> if (contentReady || isCurrentPage) SettingPager(navController, bottomInnerPadding, isCurrentPage)
                    }
                }
            }
        }

        val sidebarThemeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
        val useDarkNightGlassSidebar = sidebarThemeState.navStyle == DarkNightNavStyle.GLASS_SIDEBAR

        if (useDarkNightGlassSidebar) {
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
            val sidebarBackdrop = rememberLayerBackdrop {
                drawRect(sidebarThemeState.palette.deepBase)
                drawContent()
            }
            val sidebarBottomPad = navBarBottomPadding + 28.dp
            CompositionLocalProvider(
                LocalNavRailPadding provides 80.dp,
                LocalBottomBarPadding provides sidebarBottomPad,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(sidebarBackdrop)
                            .padding(start = 80.dp)
                    ) {
                        // Pure 140ms Crossfade tab switch (zero half-slide ghosting!)
                        Crossfade(
                            targetState = mainPagerState.selectedPage,
                            animationSpec = tween(durationMillis = 140),
                            label = "darknight_tab_crossfade",
                        ) { page ->
                            when (page) {
                                0 -> HomePager(navController, sidebarBottomPad, true)
                                1 -> if (isFullFeatured) SuperUserPager(navController, sidebarBottomPad, true)
                                2 -> if (isFullFeatured) ModulePager(sidebarBottomPad, true)
                                3 -> SettingPager(navController, sidebarBottomPad, true)
                            }
                        }
                    }

                    // Bottom 3-button system nav gradient scrim (FIX-D1)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(navBarBottomPadding + 24.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        LocalDarkNightGlass.current.base.copy(alpha = 0.85f),
                                    )
                                )
                            )
                    )

                    DarkNightGlassSidebarOverlay(
                        palette = sidebarThemeState.palette,
                        selectedPage = mainPagerState.selectedPage,
                        moduleBadgeCount = navigationBadge.moduleEnabledCount,
                        backdrop = sidebarBackdrop,
                        animationsEnabled = sidebarThemeState.startupAnimEnabled,
                        rootTabsVisible = isFullFeatured,
                        onSelectPage = { page -> mainPagerState.animateToPage(page) },
                        onOpenDarkTune = { navController.push(Route.Tune) },
                        onOpenToolbox = { navController.push(Route.DarkNightToolbox) },
                        onOpenThemeStudio = { navController.push(Route.ColorPalette()) },
                        onOpenSendLogs = { navController.push(Route.DarkNightToolbox) },
                        onOpenAbout = { navController.push(Route.About) },
                        onExpandedChange = onSidebarExpandedChange,
                    )
                }
            }
        } else if (useNavigationRail) {
            val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Start)
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(
                    containerColor = if (hasCustomBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row {
                        SideRail(navigationBadge)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }

                UiMode.Miuix -> Scaffold(
                    containerColor = if (hasCustomBackground) Color.Transparent else MiuixTheme.colorScheme.surface
                ) { _ ->
                    Row {
                        SideRail(navigationBadge)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }
            }
        } else {
            // PART 2: Floating Pill Dock (Glass Dark / Dark Solid / auto Light Glass) + T7 140ms Crossfade
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
            val dockClearance = navBarBottomPadding + 92.dp
            val sideBarEndPad = LocalSideInfoBarPadding.current
            val glass = LocalDarkNightGlass.current
            val dockBackdrop = rememberLayerBackdrop {
                drawRect(glass.base)
                drawContent()
            }

            CompositionLocalProvider(LocalBottomBarPadding provides dockClearance) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(dockBackdrop)
                            .padding(end = sideBarEndPad)
                    ) {
                        Crossfade(
                            targetState = mainPagerState.selectedPage,
                            animationSpec = tween(durationMillis = 140),
                            label = "darknight_dock_tab_crossfade",
                        ) { page ->
                            when (page) {
                                0 -> HomePager(navController, dockClearance, true)
                                1 -> if (isFullFeatured) SuperUserPager(navController, dockClearance, true)
                                2 -> if (isFullFeatured) ModulePager(dockClearance, true)
                                3 -> SettingPager(navController, dockClearance, true)
                            }
                        }
                    }

                    DarkNightPillDock(
                        selectedPage = mainPagerState.selectedPage,
                        moduleBadgeCount = navigationBadge.moduleEnabledCount,
                        dockStyle = sidebarThemeState.dockStyle,
                        blurQuality = sidebarThemeState.blurQuality,
                        animationsEnabled = sidebarThemeState.startupAnimEnabled,
                        backdrop = LocalAppBackgroundBackdrop.current ?: dockBackdrop,
                        onSelectPage = { page -> mainPagerState.animateToPage(page) },
                        rootTabsVisible = isFullFeatured,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.current() is Route.Main && navController.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}
