package com.kazesu.manager.ui.util

import android.os.Build
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * DarkNightSu - Kernel Root Compatibility Checker.
 *
 * Quick mode (no root): reads only getprop / uname / a few world-readable files.
 * Deep mode (root-assisted): root granted by ANY manager (Magisk etc.) lets us also read
 * /proc/config.gz, dmesg, partition table and the existing root provider.
 *
 * This is a HEURISTIC. It cannot guarantee a kernel will boot. UI must say so.
 */
enum class RootVerdict { SUPPORTED, LIKELY, RISKY, UNSUPPORTED }

enum class CompatSeverity(val penalty: Int) {
    OK(0), INFO(0), WARN(15), RISK(40), BLOCK(100)
}

enum class RootPath { LKM_GKI, KERNEL_BUILTIN }

data class CompatItem(
    val key: String,
    val severity: CompatSeverity,
    val title: String,
    val detail: String,
)

data class CompatReport(
    val verdict: RootVerdict,
    val items: List<CompatItem>,
    val deviceName: String,
    val kernelRelease: String,
    val rootPath: RootPath,
    val deep: Boolean,
    val rootProvider: String?,
) {
    /** True when the UI should force an explicit "I understand the brick risk" confirmation. */
    val needsBrickWarning: Boolean
        get() = verdict == RootVerdict.RISKY || verdict == RootVerdict.UNSUPPORTED

    /** Plain text for "Copy / Send report to developer". */
    fun toPlainText(): String = buildString {
        appendLine("DarkNightSu compatibility report")
        appendLine("Device: $deviceName")
        appendLine("Kernel: $kernelRelease")
        appendLine("Verdict: $verdict   Mode: ${if (deep) "deep(root)" else "quick"}")
        rootProvider?.let { appendLine("Root provider: $it") }
        appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})  ABI: ${Build.SUPPORTED_ABIS.firstOrNull()}")
        appendLine("Fingerprint: ${Build.FINGERPRINT}")
        appendLine()
        items.forEach { appendLine("[${it.severity}] ${it.title} - ${it.detail}") }
    }
}

object RootCompatChecker {

    /** Latest report so Home / Install can show the same label without re-running. */
    private val _latest = MutableStateFlow<CompatReport?>(null)
    val latest: StateFlow<CompatReport?> = _latest

    private val GKI_RE = Regex("""^(\d+)\.(\d+)\.(\d+)-android(\d+)-(\d+)""")
    private val KVER_RE = Regex("""^(\d+)\.(\d+)""")

    private fun prop(key: String): String = try {
        val p = ProcessBuilder("getprop", key).redirectErrorStream(true).start()
        p.inputStream.bufferedReader().use { it.readText() }.trim()
    } catch (_: Throwable) {
        ""
    }

    private fun readFile(path: String): String = try {
        File(path).readText().trim()
    } catch (_: Throwable) {
        ""
    }

    private fun isCustomRom(): Boolean {
        val customProps = listOf(
            "ro.lineage.version", "ro.modversion", "ro.crdroid.version",
            "ro.pixelexperience.version", "ro.evolution.version", "ro.aospa.version",
            "ro.havoc.version", "ro.derp.version",
        )
        if (customProps.any { prop(it).isNotEmpty() }) return true
        if (Build.TAGS?.contains("test-keys") == true) return true
        return Regex("^(lineage|aosp|crdroid|evolution|pa|havoc|derp)_").containsMatchIn(Build.PRODUCT ?: "")
    }

    suspend fun run(deep: Boolean): CompatReport = withContext(Dispatchers.IO) {
        val items = ArrayList<CompatItem>()
        fun add(key: String, sev: CompatSeverity, title: String, detail: String) {
            items.add(CompatItem(key, sev, title, detail))
        }

        val manufacturer = (Build.MANUFACTURER ?: "").lowercase()
        val brand = (Build.BRAND ?: "").lowercase()
        val customRom = isCustomRom()
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        // ---- Android / ABI ----
        val abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
        if (Build.VERSION.SDK_INT < 26) {
            add("sdk", CompatSeverity.BLOCK, "Android too old", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}). Android 8.0+ is required.")
        } else {
            add("sdk", CompatSeverity.OK, "Android ${Build.VERSION.RELEASE}", "SDK ${Build.VERSION.SDK_INT}")
        }
        if (abi != "arm64-v8a") {
            add("abi", CompatSeverity.BLOCK, "Not an arm64 device", "Primary ABI is '$abi'. Kernel root supports arm64 devices only.")
        } else {
            add("abi", CompatSeverity.OK, "arm64 device", abi)
        }

        // ---- Kernel ----
        val release = System.getProperty("os.version").orEmpty()
        val gki = GKI_RE.find(release)
        val kv = KVER_RE.find(release)
        val major = kv?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minor = kv?.groupValues?.get(2)?.toIntOrNull() ?: 0
        val rootPath: RootPath
        if (major < 4 || (major == 4 && minor < 4)) {
            rootPath = RootPath.KERNEL_BUILTIN
            add("kernel", CompatSeverity.BLOCK, "Kernel too old", "Linux $release. Linux 4.4+ is required.")
        } else if (gki != null) {
            rootPath = RootPath.LKM_GKI
            add("kernel", CompatSeverity.OK, "GKI kernel (android${gki.groupValues[4]}-$major.$minor)",
                "Linux $release. LKM (.ko) mode and AnyKernel3 are both possible.")
        } else {
            rootPath = RootPath.KERNEL_BUILTIN
            val old = if (major == 4 && minor < 14) " Very old kernel: needs manual hooks." else ""
            add("kernel", CompatSeverity.WARN, "Non-GKI kernel ($major.$minor)",
                "Linux $release. LKM mode is not available. Root needs an AnyKernel3 kernel built with KernelSU for your exact device.$old")
        }

        // ---- Bootloader ----
        val cmdline = readFile("/proc/cmdline")
        fun boot(key: String): String {
            val p = prop("ro.boot.$key")
            if (p.isNotEmpty()) return p
            return Regex("androidboot\\.$key=(\\S+)").find(cmdline)?.groupValues?.get(1).orEmpty()
        }
        val flashLocked = boot("flash.locked")
        val vbState = boot("verifiedbootstate")
        val devState = boot("vbmeta.device_state")
        val locked: Boolean? = when {
            devState == "unlocked" || vbState == "orange" || flashLocked == "0" -> false
            devState == "locked" || vbState == "green" || vbState == "yellow" || flashLocked == "1" -> true
            else -> null
        }
        when (locked) {
            true -> add("bootloader", CompatSeverity.BLOCK, "Bootloader is LOCKED",
                "A locked bootloader refuses modified boot images. Unlock it first (this wipes data) and try again.")
            false -> add("bootloader", CompatSeverity.OK, "Bootloader is unlocked", "verifiedbootstate=$vbState")
            null -> add("bootloader", CompatSeverity.WARN, "Bootloader state unknown",
                "Could not read lock state. Make sure OEM unlocking is enabled and the bootloader is unlocked.")
        }

        // ---- Vendor specific risk ----
        val transsion = setOf("infinix", "tecno", "itel", "transsion")
        when {
            manufacturer == "samsung" || brand == "samsung" -> {
                val stockFp = (Build.FINGERPRINT ?: "").lowercase().startsWith("samsung/")
                if (stockFp && !customRom) {
                    add("vendor", CompatSeverity.RISK, "Samsung stock firmware (Knox / RKP / KDP / DEFEX)",
                        "Samsung protects the kernel at hypervisor level. A modified kernel on stock firmware can bootloop. Flash only a kernel built for your exact model with these protections handled, and keep a stock boot backup.")
                } else {
                    add("vendor", CompatSeverity.INFO, "Samsung device on custom ROM", "RKP/KDP checks are usually not enforced by custom ROM kernels.")
                }
                val warranty = boot("warranty_bit")
                if (warranty == "1") add("knox", CompatSeverity.INFO, "Knox warranty bit tripped", "Already tripped. Some Samsung features stay disabled.")
            }
            manufacturer in transsion || brand in transsion -> {
                if (!customRom) {
                    add("vendor", CompatSeverity.RISK, "Transsion (Infinix / Tecno / itel) stock firmware",
                        "Many Transsion MediaTek devices ship a modified AVB / preloader chain and reject unknown kernels. Magisk working does NOT mean a kernel flash will work. Keep a scatter/firmware ready.")
                } else {
                    add("vendor", CompatSeverity.INFO, "Transsion device on custom ROM", "Custom ROM kernels are usually fine.")
                }
            }
            manufacturer == "huawei" -> add("vendor", CompatSeverity.BLOCK, "Huawei", "Huawei does not provide an official bootloader unlock.")
            manufacturer == "honor" || brand == "honor" -> add("vendor", CompatSeverity.WARN, "Honor", "Bootloader unlock is restricted on most Honor devices.")
            manufacturer == "vivo" || brand == "iqoo" -> add("vendor", CompatSeverity.WARN, "vivo / iQOO", "Bootloader unlock is limited and region dependent.")
            manufacturer == "xiaomi" || brand == "redmi" || brand == "poco" ->
                add("vendor", CompatSeverity.INFO, "Xiaomi / Redmi / POCO", "Needs an approved Mi Unlock. HyperOS may restrict unlock per account.")
        }

        // ---- Chipset / partitions (info) ----
        val platform = prop("ro.board.platform").ifEmpty { prop("ro.hardware") }
        if (platform.isNotEmpty()) add("chip", CompatSeverity.INFO, "Chipset platform", platform)
        val slot = prop("ro.boot.slot_suffix")
        val firstApi = prop("ro.product.first_api_level").toIntOrNull() ?: 0
        val target = if (firstApi >= 33 && rootPath == RootPath.LKM_GKI) "init_boot.img" else "boot.img"
        add("partition", CompatSeverity.INFO, "Image to patch: $target",
            "A/B slot: ${slot.ifEmpty { "none (A-only)" }}. Launched with API ${if (firstApi > 0) firstApi else "unknown"}.")
        val sel = readFile("/sys/fs/selinux/enforce")
        if (sel.isNotEmpty()) add("selinux", CompatSeverity.INFO, "SELinux", if (sel == "1") "Enforcing" else "Permissive")

        // ---- Deep / root-assisted ----
        var provider: String? = null
        if (deep) {
            val idRes = Shell.cmd("id -u").exec()
            if (!idRes.isSuccess || idRes.out.firstOrNull()?.trim() != "0") {
                add("deep", CompatSeverity.WARN, "Root access not granted", "Deep check needs root from your current root manager. Allow it and run again.")
            } else {
                provider = Shell.cmd("su -v").exec().out.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                if (provider != null) add("provider", CompatSeverity.INFO, "Current root provider", provider)

                val cfg = Shell.cmd("zcat /proc/config.gz 2>/dev/null").exec().out
                if (cfg.isEmpty()) {
                    add("config", CompatSeverity.INFO, "Kernel config not exposed", "/proc/config.gz is missing, so config checks were skipped.")
                } else {
                    val set = cfg.map { it.trim() }.filter { it.startsWith("CONFIG_") }.toHashSet()
                    if (rootPath == RootPath.LKM_GKI) {
                        if ("CONFIG_MODULES=y" !in set) add("cfg_modules", CompatSeverity.RISK, "Kernel modules disabled", "CONFIG_MODULES is off, LKM mode cannot load.")
                        if ("CONFIG_MODULE_SIG_FORCE=y" in set) add("cfg_sig", CompatSeverity.RISK, "Module signature enforced", "CONFIG_MODULE_SIG_FORCE=y, unsigned LKM will be rejected.")
                    } else {
                        if ("CONFIG_KPROBES=y" in set) add("cfg_kprobes", CompatSeverity.OK, "KPROBES available", "CONFIG_KPROBES=y")
                        else add("cfg_kprobes", CompatSeverity.WARN, "KPROBES missing", "Kernel source needs manual hooks (no kprobes).")
                    }
                    val prot = set.filter {
                        it.startsWith("CONFIG_RKP") || it.startsWith("CONFIG_UH=") || it.startsWith("CONFIG_KDP") ||
                            it.startsWith("CONFIG_SECURITY_DEFEX=")
                    }.filter { it.endsWith("=y") }
                    if (prot.isNotEmpty()) {
                        add("cfg_rkp", if (customRom) CompatSeverity.WARN else CompatSeverity.RISK,
                            "Samsung kernel protection in config", prot.take(4).joinToString(", "))
                    }
                }
                val dm = Shell.cmd("dmesg 2>/dev/null | grep -i -m3 -E 'rkp|defex|kdp'").exec().out
                if (dm.isNotEmpty() && items.none { it.key == "cfg_rkp" }) {
                    add("dmesg_rkp", CompatSeverity.WARN, "RKP / DEFEX / KDP seen in kernel log", dm.first().take(120))
                }
                val parts = BootImageExtractorProbe.countTargets()
                if (parts == 0) add("parts", CompatSeverity.WARN, "boot / init_boot partition not found", "Image extraction may not work on this device.")
                else add("parts", CompatSeverity.OK, "Boot partitions found", "$parts extractable partition(s)")
            }
        }

        val penalty = items.sumOf { it.severity.penalty }
        val verdict = when {
            items.any { it.severity == CompatSeverity.BLOCK } -> RootVerdict.UNSUPPORTED
            items.any { it.severity == CompatSeverity.RISK } || penalty >= 40 -> RootVerdict.RISKY
            penalty >= 15 -> RootVerdict.LIKELY
            else -> RootVerdict.SUPPORTED
        }
        val report = CompatReport(verdict, items, deviceName, release, rootPath, deep, provider)
        _latest.value = report
        report
    }
}

/** Tiny helper so the checker does not depend on the extractor's coroutine API. */
internal object BootImageExtractorProbe {
    fun countTargets(): Int {
        val dirs = listOf("/dev/block/by-name", "/dev/block/bootdevice/by-name")
        val dir = dirs.firstOrNull { Shell.cmd("[ -d '$it' ]").exec().isSuccess } ?: return 0
        return listOf("boot", "init_boot", "vendor_boot", "recovery").count { n ->
            Shell.cmd("[ -e '$dir/$n' ] || [ -e '$dir/${n}_a' ] || [ -e '$dir/${n}_b' ]").exec().isSuccess
        }
    }
}
