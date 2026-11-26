package com.kazesu.manager.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.kazesu.manager.Natives

/**
 * Single source of truth for DarkNightSu-kernel-only features (DarkTune & DarkNight Toolbox).
 * Uses the exact same condition as Home's "Working" status card (ksuVersion != null <=> Natives.isManager).
 * Root from Magisk or other managers returns false.
 */
object DarkNightAccess {
    fun toolsUnlocked(): Boolean = Natives.isManager
}

@Composable
fun rememberDarkNightToolsUnlocked(): Boolean {
    var unlocked by remember { mutableStateOf(DarkNightAccess.toolsUnlocked()) }
    LifecycleResumeEffect(Unit) {
        unlocked = DarkNightAccess.toolsUnlocked()
        onPauseOrDispose { }
    }
    return unlocked
}
