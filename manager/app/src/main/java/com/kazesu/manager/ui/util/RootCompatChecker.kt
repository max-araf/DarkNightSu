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

    private fun rootCmd(cmd: String): Shell.Result =
        getRootShell().newJob().add(cmd).to(ArrayList(), ArrayList()).exec()

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

        val rootOk = deep && rootCmd("id -u").let { it.isSuccess && it.out.firstOrNull()?.trim() == "0" }
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
        val supportedAbis = Build.SUPPORTED_ABIS.joinToString(", ")
        when {
            Build.SUPPORTED_ABIS.contains("arm64-v8a") ->
                add("abi", CompatSeverity.OK, "64-bit ARM (arm64-v8a)", "Full support: GKI LKM, KPM & AnyKernel3 ($supportedAbis)")
            Build.SUPPORTED_ABIS.contains("armeabi-v7a") ->
                add("abi", CompatSeverity.OK, "32-bit ARM (armeabi-v7a)", "Supported via AnyKernel3 / built-in kernel ($supportedAbis). LKM & KPM patching require arm64-v8a.")
            abi.isNotEmpty() ->
                add("abi", CompatSeverity.INFO, "Architecture: $abi", "ABIs: $supportedAbis. Verify a matching kernel build for this ABI.")
            else ->
                add("abi", CompatSeverity.WARN, "Unknown ABI", "Could not detect device CPU architecture.")
        }

        // ---- Kernel ----
        // NOTE: uname can be spoofed by custom kernels, so nothing here is allowed to BLOCK.
        val release = System.getProperty("os.version").orEmpty()
        val gki = GKI_RE.find(release)
        val kv = KVER_RE.find(release)
        val major = kv?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minor = kv?.groupValues?.get(2)?.toIntOrNull() ?: 0
        val isGki = overrides.gki ?: (gki != null)
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

        // ---- Bootloader ----
        // Props can be faked by integrity modules (resetprop). /proc/cmdline comes from the real bootloader,
        // so it wins when the two disagree. A locked reading is a RISK, never a BLOCK.
        val cmdline = if (rootOk) rootCmd("cat /proc/cmdline").out.joinToString(" ") else readFile("/proc/cmdline")
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
            manufacturer == "huawei" ->
                add("vendor", CompatSeverity.RISK, "Huawei", "Huawei does not provide an official bootloader unlock.")
            manufacturer == "honor" || brand == "honor" ->
                add("vendor", CompatSeverity.WARN, "Honor", "Bootloader unlock is restricted on most Honor devices.")
            manufacturer == "vivo" || brand == "iqoo" ->
                add("vendor", CompatSeverity.WARN, "vivo / iQOO", "Bootloader unlock is limited and region dependent.")
            manufacturer == "xiaomi" || brand == "redmi" || brand == "poco" ->
                add("vendor", CompatSeverity.INFO, "Xiaomi / Redmi / POCO", "Unlock via Mi Unlock tool. HyperOS may enforce account wait/quota; check Anti-Rollback (ARB) before flashing.")
            manufacturer == "oneplus" || brand == "oneplus" ->
                add("vendor", CompatSeverity.OK, "OnePlus", "Friendly fastboot OEM unlock (except some carrier-locked models). Keep stock boot/init_boot backup.")
            manufacturer == "realme" || brand == "realme" || manufacturer == "oppo" || brand == "oppo" ->
                add("vendor", CompatSeverity.WARN, "Realme / OPPO", "Requires official Deep Testing app or region-specific unlock support. Check AVB/vbmeta before flashing.")
            manufacturer == "nubia" || brand == "nubia" || manufacturer == "zte" || brand == "redmagic" || manufacturer == "redmagic" ->
                add("vendor", CompatSeverity.WARN, "RedMagic / Nubia / ZTE", "Bootloader unlock varies by model/region (some require EDL or unlock package). Always back up stock boot/init_boot.")
            manufacturer == "google" || brand == "google" || manufacturer == "nothing" || brand == "nothing" ->
                add("vendor", CompatSeverity.OK, "${Build.MANUFACTURER} (official unlock)", "Standard fastboot flashing unlock supported. Very friendly for GKI LKM & AnyKernel3.")
            manufacturer == "motorola" || brand == "motorola" || manufacturer == "sony" || brand == "sony" ->
                add("vendor", CompatSeverity.INFO, Build.MANUFACTURER, "Official bootloader unlock code available on vendor portal. Keep a stock boot/init_boot backup.")
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
                provider = rootCmd("su -v").out.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                    ?: if (DarkNightAccess.toolsUnlocked()) "DarkNightSu (KernelSU)" else null
                if (provider != null) add("provider", CompatSeverity.INFO, "Current root provider", provider)

                var protFound = false
                val cfg = rootCmd("zcat /proc/config.gz 2>/dev/null").out
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
                val dm = rootCmd("dmesg 2>/dev/null | grep -i -m3 -E 'rkp|defex|kdp'").out
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
    private fun sh(cmd: String): Shell.Result =
        getRootShell().newJob().add(cmd).to(ArrayList(), ArrayList()).exec()

    fun countTargets(): Int {
        val dirs = listOf("/dev/block/by-name", "/dev/block/bootdevice/by-name")
        val dir = dirs.firstOrNull { sh("[ -d '$it' ]").isSuccess }
            ?: sh("ls -d /dev/block/platform/*/by-name /dev/block/platform/*/*/by-name 2>/dev/null | head -n 1")
                .out.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
            ?: return 0
        return listOf("boot", "init_boot", "vendor_boot", "recovery").count { n ->
            sh("[ -e '$dir/$n' ] || [ -e '$dir/${n}_a' ] || [ -e '$dir/${n}_b' ]").isSuccess
        }
    }
}
