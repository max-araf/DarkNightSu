package com.kazesu.manager.ui.util

import com.topjohnwu.superuser.Shell

/**
 * DarkNightSu - root shell for the checker / extractor.
 *
 * Order of attempts (first verified-root shell wins):
 *  1. [shellProvider]  - the app's OWN root shell (KsuCli getRootShell()). Set once in Application.onCreate:
 *       RootExec.shellProvider = { getRootShell() }
 *     This is REQUIRED on some phones (e.g. Motorola) where plain "su" is not available to apps and root is only
 *     reachable through the manager's ksud daemon.
 *  2. libsu's global shell, if it is already root.
 *  3. our own "su" shells (su, /system/bin/su, /system/xbin/su), cached only when verified root.
 * Shells that we did not create ourselves are never closed.
 */
internal object RootExec {
    @Volatile
    var shellProvider: (() -> Shell)? = null

    @Volatile
    private var own: Shell? = null

    private fun good(s: Shell?): Boolean = s != null && s.isAlive && s.isRoot

    @Synchronized
    fun shell(): Shell? {
        try {
            shellProvider?.invoke()?.let { if (good(it)) return it }
        } catch (_: Throwable) { }
        try {
            val g = Shell.getShell()
            if (good(g)) return g
        } catch (_: Throwable) { }
        if (good(own)) return own
        own = null
        for (cmd in listOf("su", "/system/bin/su", "/system/xbin/su")) {
            val s = try {
                Shell.Builder.create().build(cmd)
            } catch (_: Throwable) {
                null
            }
            if (s != null) {
                if (s.isRoot) {
                    own = s
                    return s
                }
                try { s.close() } catch (_: Throwable) { }
            }
        }
        return null
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
