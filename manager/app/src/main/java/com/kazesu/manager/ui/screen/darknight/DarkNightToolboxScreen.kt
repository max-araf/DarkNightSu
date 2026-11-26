package com.kazesu.manager.ui.screen.darknight

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import com.kazesu.manager.ui.component.DarkNightSwitch
import androidx.compose.material3.Text
import com.kazesu.manager.ui.theme.LocalDarkNightGlass
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazesu.manager.R
import androidx.compose.ui.unit.sp
import com.kazesu.manager.ui.navigation3.LocalNavigator
import com.kazesu.manager.ui.navigation3.Route
import com.kazesu.manager.ui.theme.DarkNightAuraBackdrop
import com.kazesu.manager.ui.theme.DarkNightGlassCard
import com.kazesu.manager.ui.theme.DarkNightGradientButton
import com.kazesu.manager.ui.theme.DarkNightThemeStore
import com.kazesu.manager.ui.theme.DarkNightToastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DarkNightToolboxScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    DarkNightThemeStore.init(context)
    val themeState by DarkNightThemeStore.state.collectAsState()
    val palette = themeState.palette

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Performance", "Charging", "Integrity", "Monitor", "Rescue", "Tweaks")

    var activeMode by remember { mutableStateOf(PerformanceMode.BALANCED) }
    var appRulesText by remember { mutableStateOf("com.dts.freefireth=gaming\ncom.dts.freefiremax=gaming") }
    var profileDaemonEnabled by remember { mutableStateOf(false) }

    var battery by remember { mutableStateOf(BatteryTelemetry()) }
    var integrity by remember { mutableStateOf(IntegrityStatus()) }
    var trickyTargets by remember { mutableStateOf("") }
    var telemetry by remember { mutableStateOf(LiveTelemetry()) }
    var bootloopGuard by remember { mutableStateOf(false) }
    var partitions by remember { mutableStateOf<List<PartitionEntry>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var keyboxResult by remember { mutableStateOf(KeyboxValidationResult()) }
    var kernelTweaks by remember { mutableStateOf(KernelTweaksSnapshot()) }
    var pendingTweakAction by remember { mutableStateOf<Pair<String, suspend () -> Pair<Boolean, String>>?>(null) }

    fun toast(msg: String) {
        DarkNightToastManager.show(msg)
    }

    suspend fun refreshAll() {
        activeMode = DarkNightToolboxRepository.getActivePerformanceMode()
        val existingRules = DarkNightToolboxRepository.readAppProfileRules()
        if (existingRules.isNotEmpty()) appRulesText = existingRules
        battery = DarkNightToolboxRepository.readBatteryTelemetry()
        integrity = DarkNightToolboxRepository.readIntegrityStatus()
        trickyTargets = DarkNightToolboxRepository.readTrickyTargets()
        telemetry = DarkNightToolboxRepository.readLiveTelemetry()
        bootloopGuard = DarkNightToolboxRepository.isBootloopGuardEnabled()
        partitions = DarkNightToolboxRepository.detectPartitions()
        keyboxResult = DarkNightToolboxRepository.validateKeyboxXml()
        kernelTweaks = DarkNightToolboxRepository.loadKernelTweaks()
    }

    LaunchedEffect(Unit) { refreshAll() }

    LaunchedEffect(selectedTab) {
        while (selectedTab == 3) {
            telemetry = DarkNightToolboxRepository.readLiveTelemetry()
            delay(2500)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DarkNightAuraBackdrop(
            palette = palette,
            animateOnStart = themeState.startupAnimEnabled,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar matching Zentra clean header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(painter = painterResource(R.drawable.ic_dn_back), contentDescription = "Back", tint = LocalDarkNightGlass.current.content)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("DarkNight Toolbox", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text("Kernel Control & Automation", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                }
                IconButton(onClick = { navigator.push(Route.DarkNightThemeStudio) }) {
                    Icon(Icons.Filled.Star, contentDescription = "Themes", tint = palette.secondaryGlow)
                }
                IconButton(onClick = { scope.launch { refreshAll(); toast("Refreshed") } }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = LocalDarkNightGlass.current.content)
                }
            }

            // Pill-shaped segmented tab bar with auto-scroll & zero edge clipping
            val tabScrollState = rememberScrollState()
            LaunchedEffect(selectedTab) {
                val target = if (tabs.size <= 1) 0 else (tabScrollState.maxValue * selectedTab) / (tabs.size - 1)
                tabScrollState.animateScrollTo(target)
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(999.dp),
                color = Color(0xCC0D1017),
                border = BorderStroke(1.dp, palette.secondaryGlow.copy(alpha = 0.28f)),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(tabScrollState)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tabs.forEachIndexed { index, title ->
                        val active = selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .then(
                                    if (active) {
                                        Modifier.background(
                                            Brush.horizontalGradient(listOf(palette.buttonStart, palette.buttonEnd))
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedTab = index }
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = title,
                                color = if (active) Color.White else Color.White.copy(alpha = 0.72f),
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
                // Left & right 16dp fade-edge masks (FIX-D9)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(16.dp)
                        .height(42.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xEE0D1017), Color.Transparent)))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(16.dp)
                        .height(42.dp)
                        .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xEE0D1017))))
                )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Memory, contentDescription = null, tint = palette.secondaryGlow)
                                Text("Global Kernel Profile", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                            Text(
                                "Switch CPU/GPU governors & ZRAM swappiness instantly.",
                                color = LocalDarkNightGlass.current.contentSecondary,
                                fontSize = 12.sp,
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PerformanceMode.entries.forEach { mode ->
                                    FilterChip(
                                        selected = activeMode == mode,
                                        onClick = {
                                            scope.launch {
                                                val (ok, msg) = DarkNightToolboxRepository.applyPerformanceMode(mode)
                                                if (ok) activeMode = mode
                                                toast(msg)
                                            }
                                        },
                                        label = { Text(mode.label) }
                                    )
                                }
                            }
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Per-App Auto Profile Daemon", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("Format: package.name=gaming|balanced|battery", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                                }
                                DarkNightSwitch(checked = profileDaemonEnabled, onCheckedChange = { profileDaemonEnabled = it })
                            }
                            OutlinedTextField(
                                value = appRulesText,
                                onValueChange = { appRulesText = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6,
                                label = { Text("App Rules") }
                            )
                            DarkNightGradientButton(
                                text = "Save Rules & Update Daemon",
                                palette = palette,
                                onClick = {
                                    scope.launch {
                                        val (_, msg) = DarkNightToolboxRepository.saveAppProfileRules(appRulesText, profileDaemonEnabled)
                                        toast(msg)
                                    }
                                },
                            )
                        }
                    }

                    1 -> {
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("Battery Telemetry", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("Level: ${battery.level}%  •  Status: ${battery.status}", color = LocalDarkNightGlass.current.content)
                            Text("Temp: ${"%.1f".format(battery.tempC)}°C  •  Current: ${battery.currentMa} mA  •  Voltage: ${battery.voltageMv} mV", color = LocalDarkNightGlass.current.content.copy(alpha = 0.86f), fontSize = 13.sp)
                            Text(
                                if (battery.bypassSupported) "Control node: ${battery.bypassNode}" else "No kernel charging control node found on this device",
                                color = LocalDarkNightGlass.current.contentSecondary,
                                fontSize = 12.sp,
                            )
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bypass Charging (Gaming Mode)", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("Pause battery charging while plugged in to keep thermals low.", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                                }
                                DarkNightSwitch(
                                    checked = battery.bypassActive,
                                    enabled = battery.bypassSupported,
                                    onCheckedChange = { enable ->
                                        scope.launch {
                                            val (ok, msg) = DarkNightToolboxRepository.setBypassCharging(battery.bypassNode, enable)
                                            if (ok) battery = DarkNightToolboxRepository.readBatteryTelemetry()
                                            toast(msg)
                                        }
                                    }
                                )
                            }

                            Text("Smart Charge Limit", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(0 to "Off", 75 to "75%", 80 to "80%", 85 to "85%", 90 to "90%").forEach { (pct, label) ->
                                    FilterChip(
                                        selected = battery.chargeLimit == pct,
                                        enabled = battery.bypassSupported,
                                        onClick = {
                                            scope.launch {
                                                val (ok, msg) = DarkNightToolboxRepository.setChargeLimit(battery.bypassNode, pct)
                                                if (ok) battery = battery.copy(chargeLimit = pct)
                                                toast(msg)
                                            }
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Security, contentDescription = null, tint = palette.secondaryGlow)
                                Text("Integrity & Stealth Status", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                            Text("SELinux: ${integrity.selinuxMode}", color = LocalDarkNightGlass.current.content)
                            Text("Verified Boot State: ${integrity.verifiedBootState}", color = LocalDarkNightGlass.current.content.copy(alpha = 0.86f))
                            Text("SuSFS Detected: ${if (integrity.susfsDetected) "Active" else "Not found"}", color = LocalDarkNightGlass.current.content.copy(alpha = 0.86f))
                            Text("TrickyStore: ${if (integrity.trickyStoreInstalled) "Installed" else "Not installed"}  •  Keybox: ${if (integrity.keyboxPresent) "Present" else "Missing"}", color = LocalDarkNightGlass.current.content.copy(alpha = 0.86f))
                            Text("PIF Config: ${integrity.pifPath.ifEmpty { "Not found" }}", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("TrickyStore target.txt Editor", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            OutlinedTextField(
                                value = trickyTargets,
                                onValueChange = { trickyTargets = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                maxLines = 8,
                                label = { Text("/data/adb/tricky_store/target.txt") }
                            )
                            DarkNightGradientButton(
                                text = "Save target.txt",
                                palette = palette,
                                onClick = {
                                    scope.launch {
                                        val (_, msg) = DarkNightToolboxRepository.saveTrickyTargets(trickyTargets)
                                        toast(msg)
                                    }
                                },
                            )
                        }
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("Keybox X.509 Certificate Validator", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                text = keyboxResult.summary,
                                color = if (keyboxResult.validXml && keyboxResult.expiredCount == 0) if (LocalDarkNightGlass.current.isDark) Color(0xFF2EE59D) else Color(0xFF0A7D4F) else if (LocalDarkNightGlass.current.isDark) Color(0xFFFFD60A) else Color(0xFF9A6700),
                                fontSize = 12.sp,
                            )
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        keyboxResult = DarkNightToolboxRepository.validateKeyboxXml()
                                        toast(keyboxResult.summary)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, palette.secondaryGlow.copy(alpha = 0.5f)),
                            ) {
                                Text("Re-validate keybox.xml", color = LocalDarkNightGlass.current.content)
                            }
                        }
                    }

                    3 -> {
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("Live Kernel Telemetry", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("Kernel: ${telemetry.kernelRelease}", color = LocalDarkNightGlass.current.content.copy(alpha = 0.86f), fontSize = 12.sp)
                            Text("Uptime: ${telemetry.uptime}", color = LocalDarkNightGlass.current.content)
                            Text("RAM: ${telemetry.memUsedMb} MB / ${telemetry.memTotalMb} MB  •  Peak Temp: ${"%.1f".format(telemetry.maxTempC)}°C", color = LocalDarkNightGlass.current.content)
                            Spacer(modifier = Modifier.height(2.dp))
                            telemetry.clusters.forEach { c ->
                                Text("${c.name}: ${c.curFreqMhz} MHz (${c.governor})", color = if (LocalDarkNightGlass.current.isDark) palette.secondaryGlow else LocalDarkNightGlass.current.accentDark, fontSize = 12.sp)
                            }
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("1-Click Kernel & System Log Dumper", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                "Bundles dmesg, last_kmsg / console-ramoops, logcat, and getprop into /sdcard/Download/DarkNightSu/.",
                                color = LocalDarkNightGlass.current.contentSecondary,
                                fontSize = 12.sp,
                            )
                            DarkNightGradientButton(
                                text = if (busy) "Exporting Logs..." else "Export Full Log Archive (.tar.gz)",
                                palette = palette,
                                enabled = !busy,
                                onClick = {
                                    scope.launch {
                                        busy = true
                                        val (_, msg) = DarkNightToolboxRepository.dumpKernelLogs()
                                        busy = false
                                        toast(msg)
                                    }
                                },
                            )
                        }
                    }

                    4 -> {
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navigator.push(Route.Tool) },
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.dn_bootguard_moved_title),
                                    color = LocalDarkNightGlass.current.content,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                )
                                Text(
                                    text = stringResource(R.string.dn_bootguard_moved_summary),
                                    color = LocalDarkNightGlass.current.contentSecondary,
                                    fontSize = 12.sp,
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val (_, msg) = DarkNightToolboxRepository.createModuleSnapshot()
                                            toast(msg)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, palette.primaryGlow),
                                ) {
                                    Icon(Icons.Rounded.Backup, contentDescription = null, tint = LocalDarkNightGlass.current.content)
                                    Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                                    Text("Snapshot", color = LocalDarkNightGlass.current.content)
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val (_, msg) = DarkNightToolboxRepository.restoreModuleSnapshot()
                                            toast(msg)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, LocalDarkNightGlass.current.content.copy(alpha = 0.22f)),
                                ) {
                                    Icon(Icons.Rounded.Restore, contentDescription = null, tint = LocalDarkNightGlass.current.content)
                                    Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                                    Text("Restore", color = LocalDarkNightGlass.current.content)
                                }
                            }
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("Partition Backup (Boot / DTBO / VBMeta)", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                "Dumps raw partition images + SHA-256 to /sdcard/Download/DarkNightSu/backups/.",
                                color = LocalDarkNightGlass.current.contentSecondary,
                                fontSize = 12.sp,
                            )
                            if (partitions.isEmpty()) {
                                Text("No standard boot partitions detected (root required).", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                            } else {
                                partitions.forEach { entry ->
                                    OutlinedButton(
                                        enabled = !busy,
                                        onClick = {
                                            scope.launch {
                                                busy = true
                                                val (_, msg) = DarkNightToolboxRepository.backupPartition(entry)
                                                busy = false
                                                toast(msg)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, LocalDarkNightGlass.current.content.copy(alpha = 0.22f)),
                                    ) {
                                        Text("Backup ${entry.name}", color = LocalDarkNightGlass.current.content)
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("ZRAM & Memory", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                "ZRAM: ${kernelTweaks.zramSizeMb} MB (${kernelTweaks.zramAlgo})  •  Swappiness: ${kernelTweaks.swappiness}",
                                color = LocalDarkNightGlass.current.content.copy(alpha = 0.86f),
                                fontSize = 13.sp,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(10, 40, 60, 100).forEach { swapVal ->
                                    val active = kernelTweaks.swappiness == swapVal
                                    OutlinedButton(
                                        onClick = {
                                            pendingTweakAction = "Set vm.swappiness to $swapVal?" to {
                                                DarkNightToolboxRepository.applySwappiness(swapVal)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, if (active) palette.primaryGlow else LocalDarkNightGlass.current.content.copy(alpha = 0.22f)),
                                    ) {
                                        Text("$swapVal", color = LocalDarkNightGlass.current.content, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("I/O Scheduler (${kernelTweaks.ioBlockDev.ifBlank { "none" }})", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("Active: ${kernelTweaks.ioCurrent}", color = if (LocalDarkNightGlass.current.isDark) palette.secondaryGlow else LocalDarkNightGlass.current.accentDark, fontSize = 13.sp)
                            if (kernelTweaks.ioAvailable.isEmpty()) {
                                Text("No kernel I/O schedulers readable (root required).", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                            } else {
                                kernelTweaks.ioAvailable.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        rowItems.forEach { sched ->
                                            val active = sched == kernelTweaks.ioCurrent
                                            OutlinedButton(
                                                onClick = {
                                                    pendingTweakAction = "Switch I/O scheduler on ${kernelTweaks.ioBlockDev} to '$sched'?" to {
                                                        DarkNightToolboxRepository.applyIoScheduler(kernelTweaks.ioBlockDev, sched)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, if (active) palette.primaryGlow else LocalDarkNightGlass.current.content.copy(alpha = 0.22f)),
                                            ) {
                                                Text(sched, color = LocalDarkNightGlass.current.content, fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        DarkNightGlassCard(palette = palette, useBlur = false) {
                            Text("TCP Congestion Control", color = LocalDarkNightGlass.current.content, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("Active: ${kernelTweaks.tcpCurrent}", color = if (LocalDarkNightGlass.current.isDark) palette.secondaryGlow else LocalDarkNightGlass.current.accentDark, fontSize = 13.sp)
                            if (kernelTweaks.tcpAvailable.isEmpty()) {
                                Text("No TCP algorithms readable.", color = LocalDarkNightGlass.current.contentSecondary, fontSize = 12.sp)
                            } else {
                                kernelTweaks.tcpAvailable.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        rowItems.forEach { algo ->
                                            val active = algo == kernelTweaks.tcpCurrent
                                            OutlinedButton(
                                                onClick = {
                                                    pendingTweakAction = "Switch TCP congestion control to '$algo'?" to {
                                                        DarkNightToolboxRepository.applyTcpCongestion(algo)
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, if (active) palette.primaryGlow else LocalDarkNightGlass.current.content.copy(alpha = 0.22f)),
                                            ) {
                                                Text(algo, color = LocalDarkNightGlass.current.content, fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        pendingTweakAction?.let { (prompt, action) ->
            val glass = LocalDarkNightGlass.current
            AlertDialog(
                onDismissRequest = { pendingTweakAction = null },
                containerColor = if (glass.isDark) Color(0xEE0B101D) else Color(0xF5FFFFFF),
                title = { Text("Confirm Kernel Tweak", color = glass.content, fontWeight = FontWeight.Bold) },
                text = { Text(prompt, color = glass.contentSecondary, fontSize = 13.sp) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingTweakAction = null
                            scope.launch {
                                val (_, msg) = action()
                                kernelTweaks = DarkNightToolboxRepository.loadKernelTweaks()
                                toast(msg)
                            }
                        }
                    ) {
                        Text("Apply", color = palette.primaryGlow, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingTweakAction = null }) {
                        Text("Cancel", color = glass.contentSecondary)
                    }
                },
            )
        }
    }
}
