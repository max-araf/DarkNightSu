package com.kazesu.manager.ui.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kazesu.manager.ui.theme.DarkNightLinks

/** Telegram helpers. Bug reports go to the group's Issues topic; the personal chat is only for About. */
object DarkNightContact {
    private val telegramPackages = listOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.thunderdog.challegram",
        "org.telegram.plus",
        "nekox.messenger",
    )

    private fun open(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(intent) } catch (_: ActivityNotFoundException) { }
    }

    /** Opens the "Issues" topic (t.me/darknightsu/8). Use this for Send logs / bug reports. */
    fun openIssuesTopic(context: Context) = open(context, DarkNightIssues.TOPIC_URL)

    /** Opens the developer's personal Telegram chat (About screen only). */
    fun openDeveloperChat(context: Context) = open(context, DarkNightLinks.DEVELOPER_TELEGRAM_URL)

    /**
     * Shares the bug report archive into Telegram. Android cannot post into a forum topic by itself, so the
     * message text tells the user where it belongs (Issues topic). In Telegram's picker choose the
     * DarkNightSu group, then the Issues topic. No Telegram: normal Android chooser.
     */
    fun shareLogsToDeveloper(context: Context, uri: Uri, chooserTitle: String) {
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "application/gzip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, DarkNightIssues.SHARE_NOTE)
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
