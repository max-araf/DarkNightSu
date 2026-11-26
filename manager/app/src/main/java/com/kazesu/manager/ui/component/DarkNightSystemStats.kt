package com.kazesu.manager.ui.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.kazesu.manager.ui.screen.darknight.DarkNightToolboxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.File

data class SideInfoBarStats(
    val cpuCurKhz: Long = -1L,
    val cpuMaxKhz: Long = -1L,
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false,
    val batteryTempC: Float = 0f,
    val cpuPeakTempC: Float = 0f,
) {
    val cpuRatio: Float
        get() = if (cpuCurKhz > 0 && cpuMaxKhz > 0) {
            (cpuCurKhz.toFloat() / cpuMaxKhz.toFloat()).coerceIn(0.05f, 1f)
        } else 0f

    val cpuLabel: String
        get() = if (cpuCurKhz > 0) {
            String.format(java.util.Locale.US, "%.1fG", cpuCurKhz / 1_000_000f)
        } else "N/A"

    fun tempC(useCpuPeak: Boolean): Float =
        if (useCpuPeak && cpuPeakTempC > 0f) cpuPeakTempC else batteryTempC

    fun tempRatio(useCpuPeak: Boolean): Float {
        val c = tempC(useCpuPeak)
        return ((c - 20f) / 40f).coerceIn(0.05f, 1f)
    }

    fun tempLabel(useCpuPeak: Boolean, useFahrenheit: Boolean): String {
        val c = tempC(useCpuPeak)
        if (c <= 0f) return "N/A"
        return if (useFahrenheit) {
            "${((c * 9f / 5f) + 32f).toInt()}°F"
        } else {
            "${c.toInt()}°C"
        }
    }
}

object DarkNightSystemStats {

    private data class BatterySnapshot(
        val level: Int = -1,
        val charging: Boolean = false,
        val tempC: Float = 0f,
    )

    private data class CpuSnapshot(
        val curKhz: Long = -1L,
        val maxKhz: Long = -1L,
        val peakTempC: Float = 0f,
    )

    private fun parseBatteryIntent(intent: Intent?): BatterySnapshot {
        if (intent == null) return BatterySnapshot()
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val pct = if (level >= 0) (level * 100 / scale).coerceIn(0, 100) else -1
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL ||
            plugged > 0
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        return BatterySnapshot(pct, charging, tempTenths / 10f)
    }

    private fun batteryFlow(context: Context): Flow<BatterySnapshot> = callbackFlow {
        val appCtx = context.applicationContext
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = runCatching { appCtx.registerReceiver(null, filter) }.getOrNull()
        trySend(parseBatteryIntent(sticky))

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(parseBatteryIntent(intent))
            }
        }
        runCatching { appCtx.registerReceiver(receiver, filter) }
        awaitClose {
            runCatching { appCtx.unregisterReceiver(receiver) }
        }
    }

    private suspend fun readCpuSnapshot(needCpuPeakTemp: Boolean): CpuSnapshot {
        // 1. Try direct sysfs read without root first
        var bestCur = -1L
        var bestMax = -1L
        runCatching {
            val base = File("/sys/devices/system/cpu/cpufreq")
            val policies = base.listFiles { f -> f.isDirectory && f.name.startsWith("policy") } ?: emptyArray()
            for (p in policies) {
                val cur = File(p, "scaling_cur_freq").takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull() ?: -1L
                val max = File(p, "cpuinfo_max_freq").takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull() ?: -1L
                if (cur > bestCur) {
                    bestCur = cur
                    if (max > 0) bestMax = max
                }
            }
        }

        var peakTemp = 0f
        if (bestCur <= 0L || needCpuPeakTemp) {
            // Fallback to existing root-based Toolbox telemetry reader on background IO thread
            runCatching {
                val tel = DarkNightToolboxRepository.readLiveTelemetry()
                if (bestCur <= 0L) {
                    val maxMhz = tel.clusters.maxOfOrNull { it.curFreqMhz } ?: 0
                    if (maxMhz > 0) {
                        bestCur = maxMhz * 1000L
                        bestMax = maxOf(bestMax, bestCur, 2000_000L)
                    }
                }
                peakTemp = tel.maxTempC
            }
        }
        return CpuSnapshot(bestCur, bestMax, peakTemp)
    }

    private fun cpuPollFlow(needCpuPeakTemp: Boolean): Flow<CpuSnapshot> = flow {
        while (currentCoroutineContext().isActive) {
            emit(readCpuSnapshot(needCpuPeakTemp))
            delay(1500L)
        }
    }.flowOn(Dispatchers.IO)

    fun observeStats(context: Context, needCpuPeakTemp: Boolean): Flow<SideInfoBarStats> {
        return combine(
            batteryFlow(context),
            cpuPollFlow(needCpuPeakTemp),
        ) { bat, cpu ->
            SideInfoBarStats(
                cpuCurKhz = cpu.curKhz,
                cpuMaxKhz = cpu.maxKhz,
                batteryLevel = bat.level,
                isCharging = bat.charging,
                batteryTempC = bat.tempC,
                cpuPeakTempC = cpu.peakTempC,
            )
        }
    }
}
