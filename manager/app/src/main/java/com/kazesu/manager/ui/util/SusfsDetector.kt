package com.kazesu.manager.ui.util

import android.util.Log
import com.topjohnwu.superuser.ShellUtils

/**
 * Unified SuSFS detection used by both Home ("SuSFS Version") and Settings -> Extension features.
 * Rule: if a valid SuSFS version string is found (via ksud or ksu_susfs binary, using either
 * libsu root shell or RootExec), SuSFS is Supported. Only Unsupported when no version is found.
 */
object SusfsDetector {
    private const val TAG = "DarkNightSusfs"

    data class Result(
        val supported: Boolean,
        val version: String,
        val source: String,
    )

    private fun isValidVersion(raw: String): Boolean {
        val v = raw.trim()
        if (v.isEmpty()) return false
        if (v.equals("null", ignoreCase = true)) return false
        if (v.equals("false", ignoreCase = true)) return false
        if (v.contains("Invalid", ignoreCase = true)) return false
        if (v.contains("Error", ignoreCase = true)) return false
        if (v.contains("unsupport", ignoreCase = true)) return false
        if (v.contains("not found", ignoreCase = true)) return false
        if (v.contains("not supported", ignoreCase = true)) return false
        return true
    }

    private fun runCmdWithFallback(cmd: String): String {
        val primary = runCatching {
            val sh = getRootShell()
            if (sh.isRoot) ShellUtils.fastCmd(sh, cmd).trim() else ""
        }.getOrDefault("")
        if (primary.isNotEmpty()) return primary
        return runCatching {
            RootExec.lines(cmd).firstOrNull()?.trim().orEmpty()
        }.getOrDefault("")
    }

    private fun findSusfsBinary(): String? {
        val candidates = listOf(
            "command -v ksu_susfs 2>/dev/null | head -n 1",
            "ls /data/adb/ksu/bin/ksu_susfs /data/adb/modules/*/ksu_susfs /system/bin/ksu_susfs 2>/dev/null | head -n 1",
        )
        for (c in candidates) {
            val found = runCmdWithFallback(c)
            if (found.isNotEmpty() && found.startsWith("/")) return found
        }
        return null
    }

    fun detect(): Result {
        // 1. Try ksud susfs version
        val ksudPath = runCatching { getKsuDaemonPath() }.getOrDefault("")
        if (ksudPath.isNotBlank()) {
            val v1 = runCmdWithFallback("$ksudPath susfs version")
            if (isValidVersion(v1)) {
                return Result(true, v1, "ksud_version").also {
                    Log.d(TAG, "supported=true version='${it.version}' source=${it.source}")
                }
            }
        }

        // 2. Try ksu_susfs binary directly
        val cli = findSusfsBinary()
        if (cli != null) {
            val v2 = runCmdWithFallback("'$cli' show version 2>/dev/null")
            if (isValidVersion(v2)) {
                return Result(true, v2, "ksu_susfs_bin:$cli").also {
                    Log.d(TAG, "supported=true version='${it.version}' source=${it.source}")
                }
            }
        }

        // 3. Fallback to ksud susfs status
        if (ksudPath.isNotBlank()) {
            val status = runCmdWithFallback("$ksudPath susfs status")
            if (status.equals("true", ignoreCase = true)) {
                val ver = runCatching { getSuSFSVersion().trim() }.getOrDefault("")
                val cleanVer = if (isValidVersion(ver)) ver else ""
                return Result(true, cleanVer, "ksud_status").also {
                    Log.d(TAG, "supported=true version='${it.version}' source=${it.source}")
                }
            }
        }

        return Result(false, "", "none").also {
            Log.d(TAG, "supported=false version='' source=none")
        }
    }
}
