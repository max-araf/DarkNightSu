package com.kazesu.manager.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Single source of truth for bottom system navigation bar inset across both
 * 3-button navigation and gesture navigation.
 */
@Composable
fun rememberNavBottom(): Dp {
    val navInset = WindowInsets.navigationBars.union(WindowInsets.tappableElement)
    return with(LocalDensity.current) { navInset.getBottom(this).toDp() }
}
