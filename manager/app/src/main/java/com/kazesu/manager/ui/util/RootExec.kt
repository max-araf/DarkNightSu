package com.kazesu.manager.ui.util

import com.topjohnwu.superuser.Shell

/**
 * DarkNightSu - private root shell for the checker / extractor.
 *
 * Why: libsu caches ONE global shell (Shell.cmd). If that shell was created while root was not
 * available yet (app start, manager not approved yet) it silently becomes a NON-root "sh" and stays that
 * way, so "id -u" != 0 and partitions are "not found" even though the phone is rooted.
 * Here we build our own "su" shell and only cache it once it is verified root, so a later retry works.
 */
internal object RootExec {
    @Volatile
    private var cached: Shell? = null

    @Synchronized
    fun shell(): Shell? {
        cached?.let { if (it.isAlive && it.isRoot) return it }
        cached = null
        val s = try {
            Shell.Builder.create().build("su")
        } catch (_: Throwable) {
            return null
        }
        return if (s.isRoot) {
            cached = s
            s
        } else {
            try { s.close() } catch (_: Throwable) { }
            null
        }
    }

    fun isRoot(): Boolean = shell() != null

    /** Runs one command as root. null = root not available. */
    fun exec(cmd: String): Shell.Result? = try {
        shell()?.newJob()?.add(cmd)?.exec()
    } catch (_: Throwable) {
        null
    }

    fun lines(cmd: String): List<String> = exec(cmd)?.out.orEmpty()

    fun ok(cmd: String): Boolean = exec(cmd)?.isSuccess == true
}
