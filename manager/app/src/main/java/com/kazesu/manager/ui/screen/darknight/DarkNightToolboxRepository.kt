package com.kazesu.manager.ui.screen.darknight

import com.kazesu.manager.ui.util.getRootShell
import com.kazesu.manager.ui.util.shellQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Locale

enum class PerformanceMode(val id: String, val label: String, val governor: String, val swappiness: Int) {
    BALANCED("balanced", "Balanced", "schedutil", 60),
    GAMING("gaming", "Gaming / Max", "performance", 20),
    BATTERY("battery", "Battery Saver", "powersave", 100),
    THERMAL("thermal", "Thermal Cool", "schedutil", 80),
}

data class CpuClusterTelemetry(
    val name: String,
    val curFreqMhz: Int,
    val governor: String,
)

data class LiveTelemetry(
    val kernelRelease: String = "Unknown",
    val uptime: String = "--",
    val memUsedMb: Long = 0,
    val memTotalMb: Long = 0,
    val maxTempC: Float = 0f,
    val clusters: List<CpuClusterTelemetry> = emptyList(),
)

data class BatteryTelemetry(
    val level: Int = -1,
    val tempC: Float = 0f,
    val status: String = "Unknown",
    val currentMa: Int = 0,
    val voltageMv: Int = 0,
    val bypassSupported: Boolean = false,
    val bypassNode: String = "",
    val bypassActive: Boolean = false,
    val chargeLimit: Int = 0,
)

data class IntegrityStatus(
    val selinuxMode: String = "Unknown",
    val verifiedBootState: String = "Unknown",
    val susfsDetected: Boolean = false,
    val trickyStoreInstalled: Boolean = false,
    val keyboxPresent: Boolean = false,
    val pifPath: String = "",
)

data class PartitionEntry(
    val name: String,
    val blockPath: String,
)

data class KeyboxValidationResult(
    val exists: Boolean = false,
    val validXml: Boolean = false,
    val certCount: Int = 0,
    val expiredCount: Int = 0,
    val earliestExpiry: String = "Unknown",
    val summary: String = "Not checked",
)

data class KernelTweaksSnapshot(
    val zramSizeMb: Int = 0,
    val zramAlgo: String = "unknown",
    val swappiness: Int = 60,
    val ioBlockDev: String = "",
    val ioCurrent: String = "unknown",
    val ioAvailable: List<String> = emptyList(),
    val tcpCurrent: String = "unknown",
    val tcpAvailable: List<String> = emptyList(),
)

object DarkNightToolboxRepository {
    private const val BASE_DIR = "/data/adb/darknightsu"
    private const val PROFILES_CONF = "$BASE_DIR/app_profiles.conf"
    private const val CHARGE_LIMIT_FILE = "$BASE_DIR/charge_limit"
    private const val PROFILE_DAEMON = "/data/adb/service.d/darknight_profiles.sh"
    private const val CHARGE_DAEMON = "/data/adb/service.d/darknight_charge.sh"
    private const val BOOTGUARD_SCRIPT = "/data/adb/post-fs-data.d/darknight_bootguard.sh"
    private const val SNAPSHOT_DIR = "$BASE_DIR/snapshots"
    private const val EXPORT_DIR = "/sdcard/Download/DarkNightSu"

    private fun readOut(cmd: String): String {
        val res = getRootShell().newJob().add(cmd).to(ArrayList()).exec()
        return res.out.joinToString("\n").trim()
    }

    private fun runRoot(cmd: String): Pair<Boolean, String> {
        val out = ArrayList<String>()
        val err = ArrayList<String>()
        val res = getRootShell().newJob().add(cmd).to(out, err).exec()
        val msg = (out + err).joinToString("\n").trim()
        return res.isSuccess to msg
    }

    // 1. Per-App Game & Performance Profile Engine
    suspend fun applyPerformanceMode(mode: PerformanceMode): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val script = """
            for p in /sys/devices/system/cpu/cpufreq/policy*; do
              [ -f "${'$'}p/scaling_governor" ] || continue
              if grep -qw ${mode.governor} "${'$'}p/scaling_available_governors" 2>/dev/null; then
                echo ${mode.governor} > "${'$'}p/scaling_governor" 2>/dev/null || true
              fi
            done
            if [ -f /sys/class/kgsl/kgsl-3d0/devfreq/governor ]; then
              GPU_GOV="msm-adreno-tz"
              [ "${mode.id}" = "gaming" ] && GPU_GOV="performance"
              [ "${mode.id}" = "battery" ] && GPU_GOV="powersave"
              echo "${'$'}GPU_GOV" > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null || true
            fi
            if [ -f /proc/sys/vm/swappiness ]; then
              echo ${mode.swappiness} > /proc/sys/vm/swappiness 2>/dev/null || true
            fi
            mkdir -p $BASE_DIR
            echo ${mode.id} > $BASE_DIR/active_mode
        """.trimIndent()
        val (ok, msg) = runRoot(script)
        ok to (if (ok) "Applied ${mode.label} profile" else msg.ifEmpty { "Failed to apply profile" })
    }

    suspend fun getActivePerformanceMode(): PerformanceMode = withContext(Dispatchers.IO) {
        val saved = readOut("cat $BASE_DIR/active_mode 2>/dev/null")
        PerformanceMode.entries.firstOrNull { it.id == saved } ?: PerformanceMode.BALANCED
    }

    suspend fun readAppProfileRules(): String = withContext(Dispatchers.IO) {
        readOut("cat $PROFILES_CONF 2>/dev/null")
    }

    suspend fun saveAppProfileRules(rulesText: String, enableDaemon: Boolean): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val quoted = rulesText.shellQuote()
        val daemonCmd = if (enableDaemon) {
            """
            mkdir -p /data/adb/service.d
            cat << 'DN_EOF' > $PROFILE_DAEMON
            #!/system/bin/sh
            # DarkNightSu Per-App Performance Profile Watcher
            CONF="$PROFILES_CONF"
            LAST_MODE=""
            while true; do
              [ -f "${'$'}CONF" ] || { sleep 5; continue; }
              TOP_APP=${'$'}(dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 1)
              TARGET="balanced"
              while IFS='=' read -r pkg mode; do
                [ -z "${'$'}pkg" ] && continue
                case "${'$'}TOP_APP" in
                  *"${'$'}pkg"*) TARGET="${'$'}mode"; break ;;
                esac
              done < "${'$'}CONF"
              if [ "${'$'}TARGET" != "${'$'}LAST_MODE" ]; then
                GOV="schedutil"
                [ "${'$'}TARGET" = "gaming" ] && GOV="performance"
                [ "${'$'}TARGET" = "battery" ] && GOV="powersave"
                for p in /sys/devices/system/cpu/cpufreq/policy*; do
                  [ -f "${'$'}p/scaling_governor" ] && echo "${'$'}GOV" > "${'$'}p/scaling_governor" 2>/dev/null || true
                done
                LAST_MODE="${'$'}TARGET"
              fi
              sleep 3
            done &
            DN_EOF
            chmod 755 $PROFILE_DAEMON
            """.trimIndent()
        } else {
            "rm -f $PROFILE_DAEMON"
        }
        val (ok, msg) = runRoot("mkdir -p $BASE_DIR && printf '%s\n' $quoted > $PROFILES_CONF && $daemonCmd")
        ok to (if (ok) "Saved per-app rules" else msg)
    }

    // 2. Smart Charging & Bypass Toggle
    suspend fun readBatteryTelemetry(): BatteryTelemetry = withContext(Dispatchers.IO) {
        val raw = readOut(
            """
            CAP=${'$'}(cat /sys/class/power_supply/battery/capacity 2>/dev/null || echo -1)
            TEMP=${'$'}(cat /sys/class/power_supply/battery/temp 2>/dev/null || echo 0)
            STAT=${'$'}(cat /sys/class/power_supply/battery/status 2>/dev/null || echo Unknown)
            CUR=${'$'}(cat /sys/class/power_supply/battery/current_now 2>/dev/null || echo 0)
            VOLT=${'$'}(cat /sys/class/power_supply/battery/voltage_now 2>/dev/null || echo 0)
            NODE=""
            ACTIVE="0"
            for n in \
              /sys/class/power_supply/battery/input_suspend \
              /sys/class/power_supply/battery/batt_slate_mode \
              /sys/class/power_supply/battery/store_mode \
              /sys/class/power_supply/battery/charging_enabled; do
              if [ -f "${'$'}n" ]; then
                NODE="${'$'}n"
                VAL=${'$'}(cat "${'$'}n" 2>/dev/null || echo 0)
                case "${'$'}n" in
                  *charging_enabled) [ "${'$'}VAL" = "0" ] && ACTIVE="1" ;;
                  *) [ "${'$'}VAL" = "1" ] && ACTIVE="1" ;;
                esac
                break
              fi
            done
            LIM=${'$'}(cat $CHARGE_LIMIT_FILE 2>/dev/null || echo 0)
            echo "${'$'}CAP|${'$'}TEMP|${'$'}STAT|${'$'}CUR|${'$'}VOLT|${'$'}NODE|${'$'}ACTIVE|${'$'}LIM"
            """.trimIndent()
        )
        val parts = raw.split("|")
        if (parts.size < 8) return@withContext BatteryTelemetry()
        val curRaw = parts[3].toLongOrNull() ?: 0L
        val curMa = if (kotlin.math.abs(curRaw) > 10000) (curRaw / 1000).toInt() else curRaw.toInt()
        val voltRaw = parts[4].toLongOrNull() ?: 0L
        val voltMv = if (voltRaw > 100000) (voltRaw / 1000).toInt() else voltRaw.toInt()
        BatteryTelemetry(
            level = parts[0].toIntOrNull() ?: -1,
            tempC = (parts[1].toFloatOrNull() ?: 0f) / 10f,
            status = parts[2].ifEmpty { "Unknown" },
            currentMa = curMa,
            voltageMv = voltMv,
            bypassSupported = parts[5].isNotEmpty(),
            bypassNode = parts[5],
            bypassActive = parts[6] == "1",
            chargeLimit = parts[7].toIntOrNull() ?: 0,
        )
    }

    suspend fun setBypassCharging(node: String, enable: Boolean): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (node.isEmpty()) return@withContext false to "Kernel does not expose a charging control node"
        val writeVal = if (node.endsWith("charging_enabled")) {
            if (enable) "0" else "1"
        } else {
            if (enable) "1" else "0"
        }
        val (ok, msg) = runRoot("echo $writeVal > ${node.shellQuote()}")
        ok to (if (ok) (if (enable) "Bypass charging enabled" else "Normal charging restored") else msg)
    }

    suspend fun setChargeLimit(node: String, limitPercent: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (limitPercent <= 0) {
            runRoot("rm -f $CHARGE_LIMIT_FILE $CHARGE_DAEMON")
            return@withContext true to "Charge limit disabled"
        }
        if (node.isEmpty()) return@withContext false to "No supported charging control node detected"
        val stopVal = if (node.endsWith("charging_enabled")) "0" else "1"
        val startVal = if (node.endsWith("charging_enabled")) "1" else "0"
        val script = """
            mkdir -p $BASE_DIR /data/adb/service.d
            echo $limitPercent > $CHARGE_LIMIT_FILE
            cat << 'DN_EOF' > $CHARGE_DAEMON
            #!/system/bin/sh
            NODE="$node"
            while true; do
              LIM=${'$'}(cat $CHARGE_LIMIT_FILE 2>/dev/null || echo 0)
              [ "${'$'}LIM" -le 0 ] && exit 0
              CAP=${'$'}(cat /sys/class/power_supply/battery/capacity 2>/dev/null || echo 0)
              if [ "${'$'}CAP" -ge "${'$'}LIM" ]; then
                echo $stopVal > "${'$'}NODE" 2>/dev/null || true
              elif [ "${'$'}CAP" -le ${'$'}((LIM - 3)) ]; then
                echo $startVal > "${'$'}NODE" 2>/dev/null || true
              fi
              sleep 30
            done &
            DN_EOF
            chmod 755 $CHARGE_DAEMON
        """.trimIndent()
        val (ok, msg) = runRoot(script)
        ok to (if (ok) "Charge limit set to $limitPercent%" else msg)
    }

    // 3. Built-in Integrity & Spoof Hub
    suspend fun readIntegrityStatus(): IntegrityStatus = withContext(Dispatchers.IO) {
        val raw = readOut(
            """
            SE=${'$'}(getenforce 2>/dev/null || echo Unknown)
            VB=${'$'}(getprop ro.boot.verifiedbootstate 2>/dev/null || echo unknown)
            SUS=0
            ([ -x /data/adb/ksu/bin/ksu_susfs ] || [ -d /data/adb/modules/susfs4ksu ]) && SUS=1
            TS=0
            [ -d /data/adb/tricky_store ] && TS=1
            KB=0
            [ -f /data/adb/tricky_store/keybox.xml ] && KB=1
            PIF=""
            for p in /data/adb/pif.json /data/adb/modules/playintegrityfix/pif.json /data/adb/modules/playintegrityfix/custom.pif.json; do
              [ -f "${'$'}p" ] && { PIF="${'$'}p"; break; }
            done
            echo "${'$'}SE|${'$'}VB|${'$'}SUS|${'$'}TS|${'$'}KB|${'$'}PIF"
            """.trimIndent()
        )
        val p = raw.split("|")
        if (p.size < 6) return@withContext IntegrityStatus()
        IntegrityStatus(
            selinuxMode = p[0],
            verifiedBootState = p[1],
            susfsDetected = p[2] == "1",
            trickyStoreInstalled = p[3] == "1",
            keyboxPresent = p[4] == "1",
            pifPath = p[5],
        )
    }

    suspend fun readTrickyTargets(): String = withContext(Dispatchers.IO) {
        readOut("cat /data/adb/tricky_store/target.txt 2>/dev/null")
    }

    suspend fun saveTrickyTargets(content: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val (ok, msg) = runRoot("mkdir -p /data/adb/tricky_store && printf '%s\n' ${content.shellQuote()} > /data/adb/tricky_store/target.txt")
        ok to (if (ok) "Updated TrickyStore target.txt" else msg)
    }

    // 4. Live Kernel Monitor & 1-Click Log Dumper
    suspend fun readLiveTelemetry(): LiveTelemetry = withContext(Dispatchers.IO) {
        val uname = readOut("uname -r")
        val uptime = readOut("uptime -p 2>/dev/null || cut -d' ' -f1 /proc/uptime")
        val memRaw = readOut("awk '/MemTotal|MemAvailable/ {print \$2}' /proc/meminfo")
        val memLines = memRaw.lines().mapNotNull { it.trim().toLongOrNull() }
        val totalMb = (memLines.getOrNull(0) ?: 0L) / 1024L
        val availMb = (memLines.getOrNull(1) ?: 0L) / 1024L
        val usedMb = (totalMb - availMb).coerceAtLeast(0L)

        val tempRaw = readOut(
            """
            MAX=0
            for t in /sys/class/thermal/thermal_zone*/temp; do
              [ -f "${'$'}t" ] || continue
              v=${'$'}(cat "${'$'}t" 2>/dev/null || echo 0)
              [ "${'$'}v" -gt 1000 ] && v=${'$'}((v / 1000))
              [ "${'$'}v" -gt "${'$'}MAX" ] && [ "${'$'}v" -lt 120 ] && MAX="${'$'}v"
            done
            echo "${'$'}MAX"
            """.trimIndent()
        )
        val clusterRaw = readOut(
            """
            for p in /sys/devices/system/cpu/cpufreq/policy*; do
              [ -d "${'$'}p" ] || continue
              N=${'$'}(basename "${'$'}p")
              F=${'$'}(cat "${'$'}p/scaling_cur_freq" 2>/dev/null || echo 0)
              G=${'$'}(cat "${'$'}p/scaling_governor" 2>/dev/null || echo unknown)
              echo "${'$'}N|${'$'}((F / 1000))|${'$'}G"
            done
            """.trimIndent()
        )
        val clusters = clusterRaw.lines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                CpuClusterTelemetry(parts[0], parts[1].toIntOrNull() ?: 0, parts[2])
            } else null
        }
        LiveTelemetry(
            kernelRelease = uname.ifEmpty { "Unknown" },
            uptime = uptime.ifEmpty { "--" },
            memUsedMb = usedMb,
            memTotalMb = totalMb,
            maxTempC = tempRaw.toFloatOrNull() ?: 0f,
            clusters = clusters,
        )
    }

    suspend fun dumpKernelLogs(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = "$EXPORT_DIR/darknight_logs_$ts.tar.gz"
        val script = """
            TMP="$BASE_DIR/tmp_logs_$ts"
            mkdir -p "${'$'}TMP" "$EXPORT_DIR"
            dmesg > "${'$'}TMP/dmesg.txt" 2>&1 || true
            logcat -d -t 500 > "${'$'}TMP/logcat.txt" 2>&1 || true
            getprop > "${'$'}TMP/props.txt" 2>&1 || true
            uname -a > "${'$'}TMP/uname.txt" 2>&1 || true
            [ -f /sys/fs/pstore/console-ramoops-0 ] && cp /sys/fs/pstore/console-ramoops-0 "${'$'}TMP/" 2>/dev/null || true
            [ -f /proc/last_kmsg ] && cp /proc/last_kmsg "${'$'}TMP/" 2>/dev/null || true
            tar -czf ${outFile.shellQuote()} -C "${'$'}TMP" . && rm -rf "${'$'}TMP"
        """.trimIndent()
        val (ok, msg) = runRoot(script)
        ok to (if (ok) "Saved to $outFile" else msg.ifEmpty { "Log dump failed" })
    }

    // 5. Bootloop Rescue & Module Snapshot
    suspend fun isBootloopGuardEnabled(): Boolean = withContext(Dispatchers.IO) {
        readOut("[ -f $BOOTGUARD_SCRIPT ] && echo 1 || echo 0") == "1"
    }

    suspend fun setBootloopGuard(enabled: Boolean): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val cmd = if (enabled) {
            """
            mkdir -p /data/adb/post-fs-data.d /data/adb/service.d $BASE_DIR
            cat << 'DN_EOF' > $BOOTGUARD_SCRIPT
            #!/system/bin/sh
            # DarkNightSu 3-Strike Bootloop Rescue Guard
            CNT_FILE="$BASE_DIR/boot_fail_count"
            CNT=${'$'}(cat "${'$'}CNT_FILE" 2>/dev/null || echo 0)
            CNT=${'$'}((CNT + 1))
            echo "${'$'}CNT" > "${'$'}CNT_FILE"
            if [ "${'$'}CNT" -ge 3 ]; then
              for m in /data/adb/modules/*; do
                [ -d "${'$'}m" ] && touch "${'$'}m/disable"
              done
              echo 0 > "${'$'}CNT_FILE"
            fi
            DN_EOF
            chmod 755 $BOOTGUARD_SCRIPT
            cat << 'DN_EOF' > /data/adb/service.d/darknight_bootguard_reset.sh
            #!/system/bin/sh
            while [ "${'$'}(getprop sys.boot_completed)" != "1" ]; do sleep 2; done
            echo 0 > "$BASE_DIR/boot_fail_count"
            DN_EOF
            chmod 755 /data/adb/service.d/darknight_bootguard_reset.sh
            """.trimIndent()
        } else {
            "rm -f $BOOTGUARD_SCRIPT /data/adb/service.d/darknight_bootguard_reset.sh $BASE_DIR/boot_fail_count"
        }
        val (ok, msg) = runRoot(cmd)
        ok to (if (ok) (if (enabled) "3-Strike Bootloop Guard active" else "Bootloop Guard disabled") else msg)
    }

    suspend fun createModuleSnapshot(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val script = """
            mkdir -p $SNAPSHOT_DIR
            OUT="$SNAPSHOT_DIR/latest_modules.state"
            : > "${'$'}OUT"
            for m in /data/adb/modules/*; do
              [ -d "${'$'}m" ] || continue
              ID=${'$'}(basename "${'$'}m")
              STATE="enabled"
              [ -f "${'$'}m/disable" ] && STATE="disabled"
              echo "${'$'}ID=${'$'}STATE" >> "${'$'}OUT"
            done
        """.trimIndent()
        val (ok, msg) = runRoot(script)
        ok to (if (ok) "Module snapshot saved" else msg)
    }

    suspend fun restoreModuleSnapshot(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val script = """
            SNAP="$SNAPSHOT_DIR/latest_modules.state"
            [ -f "${'$'}SNAP" ] || { echo "No snapshot found"; exit 1; }
            while IFS='=' read -r id state; do
              [ -d "/data/adb/modules/${'$'}id" ] || continue
              if [ "${'$'}state" = "disabled" ]; then
                touch "/data/adb/modules/${'$'}id/disable"
              else
                rm -f "/data/adb/modules/${'$'}id/disable"
              fi
            done < "${'$'}SNAP"
        """.trimIndent()
        val (ok, msg) = runRoot(script)
        ok to (if (ok) "Restored module states (reboot to apply)" else msg)
    }

    // 6. Boot / Partition Backup Tool
    suspend fun detectPartitions(): List<PartitionEntry> = withContext(Dispatchers.IO) {
        val raw = readOut(
            """
            SLOT=${'$'}(getprop ro.boot.slot_suffix 2>/dev/null || echo "")
            for p in boot init_boot vendor_boot dtbo vbmeta; do
              for cand in "/dev/block/by-name/${'$'}{p}${'$'}{SLOT}" "/dev/block/by-name/${'$'}p"; do
                if [ -e "${'$'}cand" ]; then
                  echo "${'$'}{p}${'$'}{SLOT}|${'$'}cand"
                  break
                fi
              done
            done
            """.trimIndent()
        )
        raw.lines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size == 2) PartitionEntry(parts[0], parts[1]) else null
        }
    }

    suspend fun backupPartition(entry: PartitionEntry): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outImg = "$EXPORT_DIR/backups/${entry.name}_$ts.img"
        val script = """
            mkdir -p "$EXPORT_DIR/backups"
            dd if=${entry.blockPath.shellQuote()} of=${outImg.shellQuote()} bs=4096 2>/dev/null
            sha256sum ${outImg.shellQuote()} > ${"$outImg.sha256".shellQuote()}
        """.trimIndent()
        val (ok, msg) = runRoot(script)
        ok to (if (ok) "Backed up to $outImg" else msg.ifEmpty { "Partition backup failed" })
    }

    // 7. TrickyStore Keybox local XML & X.509 certificate expiry validator (PART 5)
    suspend fun validateKeyboxXml(): KeyboxValidationResult = withContext(Dispatchers.IO) {
        val raw = readOut("cat /data/adb/tricky_store/keybox.xml 2>/dev/null")
        if (raw.isBlank()) {
            return@withContext KeyboxValidationResult(
                exists = false,
                summary = "keybox.xml not found in /data/adb/tricky_store/",
            )
        }
        val hasTags = raw.contains("<AndroidAttestation") && raw.contains("<Keybox") && raw.contains("<Certificate")
        val pemRegex = Regex(
            "-----BEGIN CERTIFICATE-----[\\s\\S]*?-----END CERTIFICATE-----",
            RegexOption.MULTILINE,
        )
        val matches = pemRegex.findAll(raw).toList()
        if (!hasTags || matches.isEmpty()) {
            return@withContext KeyboxValidationResult(
                exists = true,
                validXml = false,
                summary = "Invalid or incomplete keybox.xml structure",
            )
        }
        val cf = CertificateFactory.getInstance("X.509")
        val now = Date()
        var validCerts = 0
        var expiredCerts = 0
        var earliest: Date? = null
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (m in matches) {
            runCatching {
                val cert = cf.generateCertificate(
                    ByteArrayInputStream(m.value.trim().toByteArray(Charsets.UTF_8))
                ) as X509Certificate
                validCerts++
                val notAfter = cert.notAfter
                if (notAfter.before(now)) expiredCerts++
                if (earliest == null || notAfter.before(earliest)) earliest = notAfter
            }
        }
        val expiryStr = earliest?.let { fmt.format(it) } ?: "Unknown"
        val summary = when {
            validCerts == 0 -> "XML found, but no readable X.509 certificates"
            expiredCerts > 0 -> "Warning: $expiredCerts of $validCerts cert(s) expired (earliest $expiryStr)"
            else -> "Valid keybox ($validCerts certs, expires $expiryStr)"
        }
        KeyboxValidationResult(
            exists = true,
            validXml = validCerts > 0,
            certCount = validCerts,
            expiredCount = expiredCerts,
            earliestExpiry = expiryStr,
            summary = summary,
        )
    }

    // 8. Kernel Tweaks: ZRAM, Swappiness, I/O Scheduler, TCP Congestion (PART 5)
    suspend fun loadKernelTweaks(): KernelTweaksSnapshot = withContext(Dispatchers.IO) {
        val zramBytes = readOut("cat /sys/block/zram0/disksize 2>/dev/null").trim().toLongOrNull() ?: 0L
        val zramMb = (zramBytes / (1024L * 1024L)).toInt()
        val zramAlgoRaw = readOut("cat /sys/block/zram0/comp_algorithm 2>/dev/null").trim()
        val zramAlgo = Regex("\\[([^\\]]+)\\]").find(zramAlgoRaw)?.groupValues?.getOrNull(1)
            ?: zramAlgoRaw.substringBefore(" ").ifBlank { "unknown" }

        val swappiness = readOut("cat /proc/sys/vm/swappiness 2>/dev/null").trim().toIntOrNull() ?: 60

        val blockDev = readOut(
            "for d in sda mmcblk0 nvme0n1; do [ -f /sys/block/${'$'}d/queue/scheduler ] && echo ${'$'}d && break; done"
        ).trim()
        val ioRaw = if (blockDev.isNotEmpty()) {
            readOut("cat /sys/block/$blockDev/queue/scheduler 2>/dev/null").trim()
        } else ""
        val ioCurrent = Regex("\\[([^\\]]+)\\]").find(ioRaw)?.groupValues?.getOrNull(1) ?: "unknown"
        val ioAvailable = ioRaw.split(Regex("\\s+"))
            .map { it.trim('[', ']') }
            .filter { it.isNotBlank() }

        val tcpCurrent = readOut("cat /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null").trim().ifBlank { "unknown" }
        val tcpAvailable = readOut("cat /proc/sys/net/ipv4/tcp_available_congestion_control 2>/dev/null")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        KernelTweaksSnapshot(
            zramSizeMb = zramMb,
            zramAlgo = zramAlgo,
            swappiness = swappiness,
            ioBlockDev = blockDev,
            ioCurrent = ioCurrent,
            ioAvailable = ioAvailable,
            tcpCurrent = tcpCurrent,
            tcpAvailable = tcpAvailable,
        )
    }

    suspend fun applySwappiness(value: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val safe = value.coerceIn(0, 200)
        val (ok, msg) = runRoot("echo $safe > /proc/sys/vm/swappiness")
        ok to (if (ok) "Swappiness set to $safe" else msg.ifEmpty { "Failed to set swappiness" })
    }

    suspend fun applyIoScheduler(blockDev: String, scheduler: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!blockDev.matches(Regex("^[a-z0-9_]+$")) || !scheduler.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            return@withContext false to "Invalid scheduler parameter"
        }
        val (ok, msg) = runRoot("echo ${scheduler.shellQuote()} > /sys/block/$blockDev/queue/scheduler")
        ok to (if (ok) "I/O scheduler set to $scheduler" else msg.ifEmpty { "Failed to set I/O scheduler" })
    }

    suspend fun applyTcpCongestion(algo: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!algo.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            return@withContext false to "Invalid TCP algorithm"
        }
        val (ok, msg) = runRoot("echo ${algo.shellQuote()} > /proc/sys/net/ipv4/tcp_congestion_control")
        ok to (if (ok) "TCP congestion set to $algo" else msg.ifEmpty { "Failed to set TCP congestion" })
    }

    // B11: One-time cleanup of legacy Toolbox 3-strike bootguard scripts so Settings -> Tools Bootloop Protection does not conflict
    suspend fun migrateLegacyBootloopGuard(context: android.content.Context) = withContext(Dispatchers.IO) {
        val prefs = context.applicationContext.getSharedPreferences("darknight_theme_prefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("bootguard_migrated", false)) return@withContext
        val script = """
            rm -f /data/adb/post-fs-data.d/darknight_bootguard.sh                   /data/adb/service.d/darknight_bootguard_reset.sh                   /data/adb/darknight_boot_fail_count                   /data/adb/darknight_toolbox/boot_fail_count 2>/dev/null || true
        """.trimIndent()
        runRoot(script)
        prefs.edit().putBoolean("bootguard_migrated", true).apply()
    }
}
