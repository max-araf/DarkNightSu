## DarkNightSu v9.3.0

Kernel root manager for Android, dressed in glass. This release is about **knowing your phone before you flash** and a much easier Install flow.

### ✨ New
- **Root compatibility checker** (Install → *Check my device*): reads Android/SDK, ABI, kernel version & arch, boot-image header, bootloader state, partitions and vendor protection, then recommends one method. Optional **Deep check** with root and an **I know my device** override. Results can be collapsed; dangerous items always stay visible.
- **Quick start** on Install: one tap selects the recommended method (GKI → patch `init_boot.img` / `boot.img`, non-GKI → AnyKernel3 kernel zip). Other methods, *Backup and tools* and *Advanced Options* are tucked into collapsible groups.
- **Boot image extractor**: back up `boot` / `init_boot` / `recovery` to *Download/DarkNightSu*, now with partition sizes.
- **Boot-image pre-check**: warns before patching when the image is non-GKI (header v0–v2) instead of failing with a vague error.
- **DARKNIGHT wordmark** and a theme-coloured **MAX** logo in About.
- **Send logs** now points you to the Issues topic in the Telegram group.

### 🎨 UI
- Install screen and the pinned *Next* bar are frosted glass and follow your palette.
- Miuix: module cards with a banner image no longer turn white.
- Bottom navigation no longer blocks the Install button on 3-button navigation.

### 🔧 Fixes
- Root detection for Extract / Deep check on phones where plain `su` is not available (e.g. some Motorola phones) now goes through the manager's own root shell.
- Checker no longer shows a false "Not supported" on phones with spoofed props / `uname`; uncertain items are a **RISK**, not a block.
- Better ABI handling for armv7, arm64 and universal builds.

### 📦 Downloads
Pick the APK that matches your phone: `arm64-v8a`, `armeabi-v7a` or `universal` (the file ending in `-release.apk`).

### ⚠️ Before you flash
The checker is a heuristic, not a guarantee. If your phone's kernel doesn't support kernel root, don't flash. **Back up your original boot image first.**

💬 Telegram group: https://t.me/darknightsu · 🐞 Bugs: https://t.me/darknightsu/8 · 🌐 https://max-araf.github.io/DarkNightSu/
