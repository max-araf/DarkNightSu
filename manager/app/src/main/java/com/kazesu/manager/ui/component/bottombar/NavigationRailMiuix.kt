package com.kazesu.manager.ui.component.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kazesu.manager.Natives
import com.kazesu.manager.R
import com.kazesu.manager.data.repository.SettingsRepositoryImpl
import com.kazesu.manager.ui.LocalMainPagerState
import com.kazesu.manager.ui.theme.backgroundAwareColor
import com.kazesu.manager.ui.util.customText
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun NavigationRailMiuix(
    navigationBadge: NavigationBadgeState,
    modifier: Modifier = Modifier,
) {
    val fullFeatured = Natives.isFullFeatured()

    val mainState = LocalMainPagerState.current

    val items = visibleBottomBarDestinations(fullFeatured).map { destination ->
        Triple(destination, customText(destination.textKey), destination.icon)
    }
    val settingsRepo = remember { SettingsRepositoryImpl() }
    val state = rememberNavigationRailState(
        initialValue = if (settingsRepo.navigationRailExpanded) {
            NavigationRailValue.Expanded
        } else {
            NavigationRailValue.Collapsed
        },
    )
    LaunchedEffect(state.currentValue) {
        settingsRepo.navigationRailExpanded = state.isExpanded
    }

    NavigationRail(
        modifier = modifier,
        state = state,
        color = backgroundAwareColor(MiuixTheme.colorScheme.surface),
        expandContentDescription = stringResource(R.string.nav_rail_expand),
        collapseContentDescription = stringResource(R.string.nav_rail_collapse),
    ) {
        items.forEach { (destination, label, icon) ->
            val pageIndex = destination.ordinal
            NavigationRailItem(
                selected = mainState.selectedPage == pageIndex,
                onClick = {
                    mainState.animateToPage(pageIndex)
                },
                icon = icon,
                label = label,
                badge = navigationBadgeFor(pageIndex, navigationBadge),
            )
        }
    }
}
