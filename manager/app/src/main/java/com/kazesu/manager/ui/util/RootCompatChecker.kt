package com.kazesu.manager.ui.util

import android.os.Build
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

/** Which install method fits this device: GKI -> patch a file (LKM), non-GKI -> AnyKernel3 kernel zip. */
enum class RecommendedMethod { LKM_PATCH_FILE, ANYKERNEL3 }

/**
 * Values that "I know my device" switches can override. Integrity / spoof modules, custom kernels and
 * spoofed uname can make props lie, so the user must always be able to correct the checker.
 * [gki] = null means auto-detect. Pass the app's existing GKI detection here if it has one.
 */
data class CompatOverrides(
    val bootloaderUnlocked: Boolean = false,
    val vendorProtectionOff: Boolean = false,
    val gki: Boolean? = null,
)

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
    val recommended: RecommendedMethod = RecommendedMethod.ANYKERNEL3,
    /** e.g. "init_boot.img" / "boot.img" for the LKM path, "boot.img" for AnyKernel3. */
    val imageToPatch: String = "boot.img",
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
        appendLine("Recommended: $recommended ($imageToPatch)")
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

    suspend fun run(deep: Boolean, overrides: CompatOverrides = CompatOverrides()): CompatReport = withContext(Dispatchers.IO) {
        val items = ArrayList<CompatItem>()
        fun add(key: String, sev: CompatSeverity, title: String, detail: String) {
            items.add(CompatItem(key, sev, title, detail))
        }

        val rootOk = deep && RootExec.isRoot()
        val manufacturer = (Build.MANUFACTURER ?: "").lowercase()
        val brand = (Build.BRAND ?: "").lowercase()
        val customRom = isCustomRom()
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

        // ---- Android / ABI ----
        // Kernel root needs a 64-bit ARM KERNEL. The app itself may be 32-bit (armv7 build) on a 64-bit kernel
        // (very common on cheap phones), so we look at the kernel, not only at the app ABI.
        val abis = Build.SUPPORTED_ABIS.toList()
        val primary = abis.firstOrNull().orEmpty()
        val osArch = (System.getProperty("os.arch") ?: "").lowercase()
        val unameM = try {
            ProcessBuilder("uname", "-m").redirectErrorStream(true).start().inputStream.bufferedReader().use { it.readText() }.trim().lowercase()
        } catch (_: Throwable) { "" }
        val arm64Kernel = Build.SUPPORTED_64_BIT_ABIS.any { it.startsWith("arm64") } ||
            osArch in setOf("aarch64", "armv8l", "arm64") || unameM in setOf("aarch64", "armv8l", "arm64")
        if (Build.VERSION.SDK_INT < 26) {
            add("sdk", CompatSeverity.BLOCK, "Android too old", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}). Android 8.0+ is required.")
        } else {
            add("sdk", CompatSeverity.OK, "Android ${Build.VERSION.RELEASE}", "SDK ${Build.VERSION.SDK_INT}")
        }
        when {
            primary.startsWith("arm64") ->
                add("abi", CompatSeverity.OK, "arm64 device", "ABI $primary")
            primary.startsWith("armeabi") && arm64Kernel ->
                add("abi", CompatSeverity.INFO, "32-bit app on a 64-bit ARM kernel",
                    "ABI $primary, kernel arch ${osArch.ifEmpty { unameM }}. Kernel root works on 64-bit kernels. Use the armv7 / universal manager build.")
            primary.startsWith("armeabi") ->
                add("abi", CompatSeverity.BLOCK, "32-bit ARM kernel",
                    "ABI $primary, kernel arch ${osArch.ifEmpty { unameM }}. Kernel root needs a 64-bit (arm64) kernel.")
            primary.startsWith("x86_64") || primary.startsWith("x86") ->
                add("abi", CompatSeverity.INFO, "x86 device", "ABI $primary. Only for emulators / x86 devices with a KernelSU-enabled kernel.")
            primary.startsWith("riscv64") ->
                add("abi", CompatSeverity.INFO, "RISC-V device", "ABI $primary. Experimental.")
            else -> add("abi", CompatSeverity.WARN, "Unknown CPU ABI", "ABI '$primary', kernel arch ${osArch.ifEmpty { unameM }}.")
        }

        // ---- Kernel ----
        // NOTE: uname can be spoofed by custom kernels, so nothing here is allowed to BLOCK.
        val release = System.getProperty("os.version").orEmpty()
        val gki = GKI_RE.find(release)
        val kv = KVER_RE.find(release)
        val major = kv?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minor = kv?.groupValues?.get(2)?.toIntOrNull() ?: 0
        // Header version of the real boot partition beats the uname string (deep mode only).
        var bootHdr: Int? = null
        var bootDev: String? = null
        if (rootOk) {
            val parts = BootImageExtractor.listPartitions()
            val bp = parts.firstOrNull { it.name == "boot" || it.name.startsWith("boot_") }
            if (bp != null) {
                bootDev = bp.devicePath
                bootHdr = BootImageInspector.headerVersionViaRoot(bp.devicePath)
            }
        }
        val isGki = overrides.gki ?: (bootHdr?.let { it >= 3 } ?: (gki != null))
        val rootPath: RootPath = if (isGki) RootPath.LKM_GKI else RootPath.KERNEL_BUILTIN
        if (major < 4 || (major == 4 && minor < 4)) {
            add("kernel", CompatSeverity.WARN, "Kernel version looks too old", "Linux $release. Linux 4.4+ is required (this value can be spoofed).")
        } else if (isGki) {
            val label = gki?.let { "android${it.groupValues[4]}-$major.$minor" } ?: "$major.$minor"
            add("kernel", CompatSeverity.OK, "GKI kernel ($label)", "Linux $release. LKM (.ko) mode and AnyKernel3 are both possible.")
        } else {
            val old = if (major == 4 && minor < 14) " Very old kernel: needs manual hooks." else ""
            add("kernel", CompatSeverity.WARN, "Non-GKI kernel ($major.$minor)",
                "Linux $release. LKM mode is not available. Root needs an AnyKernel3 kernel built with KernelSU for your exact device.$old")
        }

        if (bootHdr != null) {
            if (bootHdr >= 3) {
                add("boot_header", CompatSeverity.OK, "Boot image header v$bootHdr (GKI 2.0 format)", "LKM patching works with this boot layout.")
            } else {
                add("boot_header", CompatSeverity.WARN, "Boot image header v$bootHdr (NOT GKI 2.0)",
                    "Kernel ${major}.${minor} does not mean GKI. Header v$bootHdr cannot be LKM-patched (\"bootimage version $bootHdr is not supported\"). Use an AnyKernel3 kernel built with KernelSU.")
            }
        }

        // ---- Bootloader ----
        // Props can be faked by integrity modules (resetprop). /proc/cmdline comes from the real bootloader,
        // so it wins when the two disagree. A locked reading is a RISK, never a BLOCK.
        val cmdline = if (rootOk) RootExec.lines("cat /proc/cmdline").joinToString(" ") else readFile("/proc/cmdline")
        fun cmd(key: String): String = Regex("androidboot\\.$key=(\\S+)").find(cmdline)?.groupValues?.get(1).orEmpty()
        fun propBoot(key: String): String = prop("ro.boot.$key")
        fun lockedFrom(flash: String, vb: String, dev: String): Boolean? = when {
            dev == "unlocked" || vb == "orange" || flash == "0" -> false
            dev == "locked" || vb == "green" || vb == "yellow" || flash == "1" -> true
            else -> null
        }
        val propLocked = lockedFrom(propBoot("flash.locked"), propBoot("verifiedbootstate"), propBoot("vbmeta.device_state"))
        val cmdLocked = lockedFrom(cmd("flash.locked"), cmd("verifiedbootstate"), cmd("vbmeta.device_state"))
        val warranty = propBoot("warranty_bit").ifEmpty { cmd("warranty_bit") }
        when {
            overrides.bootloaderUnlocked ->
                add("bootloader", CompatSeverity.OK, "Bootloader: marked unlocked by you", "You said the bootloader is unlocked (integrity / spoof modules).")
            cmdLocked == false -> {
                add("bootloader", CompatSeverity.OK, "Bootloader is unlocked", "Kernel command line says unlocked.")
                if (propLocked == true) add("spoof", CompatSeverity.INFO, "Lock state is being spoofed", "System props say locked but the real boot state is unlocked (integrity module).")
            }
            warranty == "1" ->
                add("bootloader", CompatSeverity.OK, "Bootloader was unlocked (Knox tripped)", "Samsung warranty bit is 1, so custom binaries have already run on this phone.")
            cmdLocked == true || propLocked == true ->
                add("bootloader", CompatSeverity.RISK, "Bootloader looks LOCKED",
                    "A locked bootloader refuses modified images. If you already unlocked it and use integrity/spoof modules, the value is fake: turn on \"My bootloader is unlocked\" and check again.")
            propLocked == false ->
                add("bootloader", CompatSeverity.OK, "Bootloader is unlocked", "verifiedbootstate=${propBoot("verifiedbootstate")}")
            else -> add("bootloader", CompatSeverity.WARN, "Bootloader state unknown",
                "Could not read lock state. Make sure OEM unlocking is on and the bootloader is unlocked.")
        }

        // ---- Vendor specific risk ----
        val transsion = setOf("infinix", "tecno", "itel", "transsion")
        when {
            manufacturer == "samsung" || brand == "samsung" -> {
                val stockFp = (Build.FINGERPRINT ?: "").lowercase().startsWith("samsung/")
                val samsungMsg = "Samsung protects the kernel at hypervisor level. A modified kernel on stock firmware can bootloop. Flash only a kernel built for your exact model with these protections handled, and keep a stock boot backup."
                if (stockFp && !customRom) {
                    when {
                        overrides.vendorProtectionOff ->
                            add("vendor", CompatSeverity.INFO, "Samsung stock firmware: protections marked off by you", "You said RKP / KDP / DEFEX are already off. Keep a stock boot backup.")
                        warranty == "1" ->
                            add("vendor", CompatSeverity.WARN, "Samsung stock firmware (Knox tripped)", "Custom binaries already ran here. $samsungMsg")
                        else -> add("vendor", CompatSeverity.RISK, "Samsung stock firmware (Knox / RKP / KDP / DEFEX)", samsungMsg)
                    }
                } else {
                    add("vendor", CompatSeverity.INFO, "Samsung device on custom ROM", "RKP/KDP checks are usually not enforced by custom ROM kernels.")
                }
                if (warranty == "1") add("knox", CompatSeverity.INFO, "Knox warranty bit tripped", "Already tripped. Some Samsung features stay disabled.")
            }
            manufacturer in transsion || brand in transsion -> {
                if (!customRom && overrides.vendorProtectionOff) {
                    add("vendor", CompatSeverity.INFO, "Transsion stock firmware: marked OK by you", "Keep a scatter/firmware ready.")
                } else if (!customRom) {
                    add("vendor", CompatSeverity.RISK, "Transsion (Infinix / Tecno / itel) stock firmware",
                        "Many Transsion MediaTek devices ship a modified AVB / preloader chain and reject unknown kernels. Magisk working does NOT mean a kernel flash will work. Keep a scatter/firmware ready.")
                } else {
                    add("vendor", CompatSeverity.INFO, "Transsion device on custom ROM", "Custom ROM kernels are usually fine.")
                }
            }
            manufacturer == "huawei" -> add("vendor", CompatSeverity.RISK, "Huawei", "Huawei does not provide an official bootloader unlock.")
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
        // init_boot depends on the launch Android version (13+), NOT on GKI. GKI only decides LKM vs AnyKernel3.
        val ramdiskImage = if (firstApi >= 33) "init_boot.img" else "boot.img"
        val recommended = if (isGki) RecommendedMethod.LKM_PATCH_FILE else RecommendedMethod.ANYKERNEL3
        val imageToPatch = if (isGki) ramdiskImage else "boot.img"
        val slotInfo = "A/B slot: ${slot.ifEmpty { "none (A-only)" }}. Launched with API ${if (firstApi > 0) firstApi else "unknown"}."
        if (rootPath == RootPath.LKM_GKI) {
            add("partition", CompatSeverity.INFO, "LKM mode: patch $ramdiskImage", "$slotInfo AnyKernel3 kernel zips flash the kernel inside boot.img.")
        } else {
            add("partition", CompatSeverity.INFO, "Kernel root: AnyKernel3 (boot.img)", "$slotInfo The kernel lives in boot.img, so a kernel with KernelSU built in is flashed there.")
        }
        val sel = readFile("/sys/fs/selinux/enforce")
        if (sel.isNotEmpty()) add("selinux", CompatSeverity.INFO, "SELinux", if (sel == "1") "Enforcing" else "Permissive")

        // ---- Deep / root-assisted ----
        var provider: String? = null
        var deepClean = false
        if (deep) {
            if (!rootOk) {
                add("deep", CompatSeverity.WARN, "Root access not granted", "Deep check needs root from your current root manager. Allow it and run again.")
            } else {
                provider = RootExec.lines("su -v").firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                if (provider != null) add("provider", CompatSeverity.INFO, "Current root provider", provider)

                var protFound = false
                val cfg = RootExec.lines("zcat /proc/config.gz 2>/dev/null")
                if (cfg.isEmpty()) {
                    add("config", CompatSeverity.INFO, "Kernel config not exposed", "/proc/config.gz is missing, so config checks were skipped.")
                } else {
                    val set = cfg.map { it.trim() }.filter { it.startsWith("CONFIG_") }.toHashSet()
                    if (rootPath == RootPath.LKM_GKI) {
                        if ("CONFIG_MODULES=y" !in set) add("cfg_modules", CompatSeverity.RISK, "Kernel modules disabled", "CONFIG_MODULES is off, LKM mode cannot load.")
                        if ("CONFIG_MODULE_SIG_FORCE=y" in set) add("cfg_sig", CompatSeverity.RISK, "Module signature enforced", "CONFIG_MODULE_SIG_FORCE=y, unsigned LKM will be rejected.")
                    } else {
                        if ("CONFIG_KPROBES=y" in set) add("cfg_kprobes", CompatSeverity.OK, "KPROBES available", "CONFIG_KPROBES=y")
                        else add("cfg_kprobes", CompatSeverity.INFO, "KPROBES missing", "Kernel source needs manual hooks (no kprobes).")
                    }
                    val prot = set.filter {
                        it.startsWith("CONFIG_RKP") || it.startsWith("CONFIG_UH=") || it.startsWith("CONFIG_KDP") ||
                            it.startsWith("CONFIG_SECURITY_DEFEX=")
                    }.filter { it.endsWith("=y") }
                    if (prot.isNotEmpty()) {
                        protFound = true
                        add("cfg_rkp", if (customRom || overrides.vendorProtectionOff) CompatSeverity.WARN else CompatSeverity.RISK,
                            "Samsung kernel protection in config", prot.take(4).joinToString(", "))
                    }
                    if (prot.isEmpty()) deepClean = true
                }
                val dm = RootExec.lines("dmesg 2>/dev/null | grep -i -m3 -E 'rkp|defex|kdp'")
                if (dm.isNotEmpty() && !protFound) {
                    deepClean = false
                    add("dmesg_rkp", CompatSeverity.WARN, "RKP / DEFEX / KDP seen in kernel log", dm.first().take(120))
                }
                val parts = BootImageExtractorProbe.countTargets()
                if (parts == 0) add("parts", CompatSeverity.WARN, "boot / init_boot partition not found", "Image extraction may not work on this device.")
                else add("parts", CompatSeverity.OK, "Boot partitions found", "$parts extractable partition(s)")
            }
        }
        if (deepClean) {
            // Config + kernel log show no protection markers: soften the vendor RISK (hypervisor level still cannot be read).
            for (i in items.indices) {
                val it0 = items[i]
                if (it0.key == "vendor" && it0.severity == CompatSeverity.RISK) {
                    items[i] = it0.copy(severity = CompatSeverity.WARN,
                        detail = it0.detail + " Deep check found no RKP / KDP / DEFEX markers in kernel config or log.")
                }
            }
        }
        add("spoof_note", CompatSeverity.INFO, "Values can be spoofed",
            "Integrity modules and custom kernels can fake props and uname. If a result is wrong for your phone, use the \"I know my device\" switches and check again.")

        val penalty = items.sumOf { it.severity.penalty }
        val verdict = when {
            items.any { it.severity == CompatSeverity.BLOCK } -> RootVerdict.UNSUPPORTED
            items.any { it.severity == CompatSeverity.RISK } || penalty >= 40 -> RootVerdict.RISKY
            penalty >= 15 -> RootVerdict.LIKELY
            else -> RootVerdict.SUPPORTED
        }
        val report = CompatReport(verdict, items, deviceName, release, rootPath, deep, provider, recommended, imageToPatch)
        _latest.value = report
        report
    }
}

/** Tiny helper so the checker does not depend on the extractor's coroutine API. */
internal object BootImageExtractorProbe {
    fun countTargets(): Int {
        val dirs = listOf("/dev/block/by-name", "/dev/block/bootdevice/by-name")
        val dir = dirs.firstOrNull { RootExec.ok("[ -d '$it' ]") } ?: return 0
        return listOf("boot", "init_boot", "vendor_boot", "recovery").count { n ->
            RootExec.ok("[ -e '$dir/$n' ] || [ -e '$dir/${n}_a' ] || [ -e '$dir/${n}_b' ]")
        }
    }
}
