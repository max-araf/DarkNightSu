package com.kazesu.manager.ui.screen.about

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import com.kazesu.manager.ui.theme.DarkNightLinks
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.component.DarkNightMaxLogo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazesu.manager.Natives
import com.kazesu.manager.R
import com.kazesu.manager.ui.component.material.ExpressiveScaffold
import com.kazesu.manager.ui.component.material.SegmentedColumn
import com.kazesu.manager.ui.component.material.SegmentedListItem
import com.kazesu.manager.ui.component.material.SegmentedSwitchItem
import com.kazesu.manager.ui.component.material.TonalCard
import com.kazesu.manager.ui.component.material.TopBarBackButton
import com.kazesu.manager.ui.component.material.expressiveTopAppBarColors

@Composable
fun AboutScreenMaterial(
    state: AboutUiState,
    actions: AboutScreenActions,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val contributors = rememberContributors()
    val isKsuValid = remember { Natives.isManager }

    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    TopBarBackButton(onClick = actions.onBack)
                },
                colors = expressiveTopAppBarColors(),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 240.dp)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // The hero shows the KazeSU leaf mark itself. The launcher icon depends on
                        // the selected app-icon pack, which made the about page advertise whichever
                        // icon was active instead of the brand.
                        com.kazesu.manager.ui.component.DarkNightLogo(size = 96.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = state.appName,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text = state.versionName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    item {
                        SegmentedSwitchItem(
                            icon = Icons.Filled.SystemUpdate,
                            title = stringResource(id = R.string.settings_check_update),
                            summary = stringResource(id = R.string.settings_check_update_summary),
                            checked = state.checkUpdate,
                            onCheckedChange = actions.onSetCheckUpdate,
                        )
                    }
                    item(visible = state.checkUpdate) {
                        SegmentedSwitchItem(
                            icon = Icons.Filled.Science,
                            title = stringResource(id = R.string.settings_check_ci_update),
                            summary = stringResource(id = R.string.settings_check_ci_update_summary),
                            checked = state.checkCiUpdate,
                            onCheckedChange = actions.onSetCheckCiUpdate,
                        )
                    }
                    item(visible = isKsuValid) {
                        SegmentedSwitchItem(
                            icon = Icons.Filled.SystemUpdateAlt,
                            title = stringResource(id = R.string.settings_module_check_update),
                            summary = stringResource(id = R.string.settings_check_update_summary),
                            checked = state.checkModuleUpdate,
                            onCheckedChange = actions.onSetCheckModuleUpdate,
                        )
                    }
                }
            }
            item {
                val iconLinks = state.links.filter { it.url.isNotBlank() && it.iconRes != 0 }
                if (iconLinks.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        iconLinks.forEach { linkInfo ->
                            AboutRoundGlassIconButton(
                                iconRes = linkInfo.iconRes,
                                contentDescription = if (linkInfo.contentDescRes != 0) {
                                    stringResource(linkInfo.contentDescRes)
                                } else {
                                    linkInfo.fullText
                                },
                                onClick = { actions.onOpenLink(linkInfo.url) },
                            )
                        }
                    }
                }
            }
            if (contributors.isNotEmpty()) {
                item {
                    val glass = LocalDarkNightGlass.current
                    val themeState by DarkNightThemeStore.state.collectAsStateWithLifecycle()
                    Row(
                        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.about_contributors),
                            style = MaterialTheme.typography.titleSmall,
                            color = glass.accentText,
                        )
                        Text(
                            text = contributors.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    contributors.forEach { contributor ->
                        val avatar = rememberContributorAvatar(contributor.login)
                        TonalCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            onClick = { actions.onOpenLink(contributor.profileUrl) },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                if (contributor.login.equals("max-araf", ignoreCase = true)) {
                                    DarkNightMaxLogo(
                                        accent = themeState.palette.primaryGlow,
                                        modifier = Modifier.width(132.dp),
                                        cornerRadius = 16.dp,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(glass.surfaceHi)
                                            .border(1.dp, glass.outline, RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (avatar != null) {
                                            Image(
                                                bitmap = avatar,
                                                contentDescription = contributor.login,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp),
                                            )
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = contributor.login.replace('-', ' ').uppercase(),
                                        color = glass.content,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp,
                                    )
                                    Text(
                                        text = "@${contributor.login}",
                                        color = glass.accentText,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                            }
                        }
                    }
                    TonalCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        ContributorNameWordmark(
                            names = contributors.map { it.login },
                            modifier = Modifier.padding(18.dp),
                        )
                    }
                }
            }
            item {
                Spacer(
                    Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@Composable
private fun AboutRoundGlassIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val glass = LocalDarkNightGlass.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "about_icon_scale",
    )
    val fillBrush = if (glass.isDark) {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.06f))
        )
    } else {
        Brush.verticalGradient(
            listOf(glass.surface, glass.surfaceHi)
        )
    }
    val borderColor = if (glass.isDark) Color.White.copy(alpha = 0.28f) else glass.outline

    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(fillBrush)
            .border(1.dp, borderColor, CircleShape)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = glass.content,
            modifier = Modifier.size(26.dp),
        )
    }
}
