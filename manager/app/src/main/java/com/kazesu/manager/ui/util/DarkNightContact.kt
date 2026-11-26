package com.kazesu.manager.ui.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kazesu.manager.ui.theme.DarkNightLinks

/** Developer contact helpers (Telegram). Works with every Telegram client; falls back to the system chooser. */
object DarkNightContact {
    private val telegramPackages = listOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.thunderdog.challegram",
        "org.telegram.plus",
        "nekox.messenger",
    )

    /** Opens the developer's Telegram chat. */
    fun openDeveloperChat(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DarkNightLinks.DEVELOPER_TELEGRAM_URL))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(intent) } catch (_: ActivityNotFoundException) { }
    }

    /**
     * Shares the bug report archive straight into Telegram (contact picker opens, the message already says who to send it to).
     * If no Telegram client is installed the normal Android chooser is shown.
     */
    fun shareLogsToDeveloper(context: Context, uri: Uri, chooserTitle: String) {
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "application/gzip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "DarkNightSu bug report -> ${DarkNightLinks.DEVELOPER_HANDLE}")
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        for (pkg in telegramPackages) {
            try {
                context.startActivity(Intent(base).setPackage(pkg))
                return
            } catch (_: ActivityNotFoundException) { }
        }
        context.startActivity(Intent.createChooser(base, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
