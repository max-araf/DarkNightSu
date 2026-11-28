# GitHub About & v9.3.0 Release Notes (Copy-Paste Guide)

## 1. GitHub Repo "About" Sidebar (click the gear icon next to About)

- **Description:**
  `Open-source kernel root solution for Android (GKI & Non-GKI) with a frosted-glass Material 3 / Miuix Manager, 10 dynamic palettes, compatibility checker & boot extractor.`
- **Website:**
  `https://max-araf.github.io/DarkNightSu/`
- **Topics:**
  `android` `root` `darknightsu` `kernelsu` `gki` `non-gki` `susfs` `kpm` `android-kernel` `jetpack-compose`

---

## 2. GitHub Release Notes for `v9.3.0`

```markdown
## DarkNightSu v9.3.0

### What's New
- **Guided Frosted-Glass Install & Quick Start:** Redesigned Install screen across Material 3 and Miuix with 10 live palette tints, 2-tap Quick Start, collapsible *Other methods*, *Backup and tools*, *Advanced*, and a frosted-glass pinned **Next** bar.
- **Collapsible Compatibility Checker:** Verdict & recommended install method always stay visible, dangerous (`BLOCK`/`RISK`) items preview even when collapsed, and full details toggle smoothly.
- **Boot Image Extractor Improvements:** Wired `RootExec.shellProvider` to `ksud` (`getRootShell()`) for devices without plain `su` (e.g., Motorola), plus 3-way partition size fallback (`blockdev`, `/sys/class/block`, `/proc/partitions`) and clear empty-state diagnostics.
- **Miuix Module Banner Fix:** Module cards with banners now render over a frosted-glass base with a vertical dark scrim so text stays readable on white or transparent PNG banners in both Light and Dark themes.
- **Dynamic MAX Contributor Logo:** About screen now renders a palette-reactive `DarkNightMaxLogo` with crisp white `MAX` lettering.
- **Full Open-Source Kernel Integration:** `kernel/setup.sh` and `kernel/Kbuild` now point directly to `max-araf/DarkNightSu` with `com.darknight.ksu` pre-pinned for GKI & Non-GKI kernel builders.

### Kernel Integration (GKI & Non-GKI)
```bash
curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s v9.3.0
```
```
