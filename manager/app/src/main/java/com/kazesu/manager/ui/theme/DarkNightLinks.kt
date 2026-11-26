package com.kazesu.manager.ui.theme

/**
 * Central place for DarkNightSu user-facing links so upstream personal links
 * (Patreon / OpenCollective / KazeSU Telegram / upstream contributors) never leak into the UI.
 */
object DarkNightLinks {
    const val REPO_URL = "https://github.com/max-araf/DarkNightSu"
    const val KERNEL_INTEGRATION_GUIDE_URL = "https://github.com/max-araf/DarkNightSu/blob/main/DARKNIGHT_SETUP.md"

    // Official community group and developer contact.
    const val TELEGRAM_GROUP_URL = "https://t.me/darknightsu"
    const val DEVELOPER_TELEGRAM_URL = "https://t.me/Max_araf_2010"
    const val DEVELOPER_HANDLE = "@Max_araf_2010"

    const val SHOW_SUPPORT_CARD = false
    const val SUPPORT_URL = ""

    const val SHOW_TELEGRAM_LINK = true
    const val TELEGRAM_URL = TELEGRAM_GROUP_URL

    // About page shows ONLY the DarkNightSu author (assets/about/contributors.json).
    const val SHOW_UPSTREAM_CONTRIBUTORS = true
}
