package com.kazesu.manager.ui.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kazesu.manager.ui.theme.DarkNightLinks

/** Share helpers used by the compatibility checker and the boot image extractor. */
object DarkNightShare {
    private val telegramPackages = listOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.thunderdog.challegram",
        "org.telegram.plus",
        "nekox.messenger",
    )

    fun copyText(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** Sends a text report to the developer (Telegram first, system chooser as fallback). */
    fun shareTextToDeveloper(context: Context, text: String, chooserTitle: String) {
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${DarkNightLinks.DEVELOPER_HANDLE}\n\n$text")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        for (pkg in telegramPackages) {
            try {
                context.startActivity(Intent(base).setPackage(pkg))
                return
            } catch (_: ActivityNotFoundException) { }
        }
        context.startActivity(Intent.createChooser(base, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Shares a saved image (content:// Uri) with any app. */
    fun shareFile(context: Context, uri: Uri, chooserTitle: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
