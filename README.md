<div align="center">

<img src="docs/DarkNightSu-logo.png" width="128" height="128" alt="DarkNightSu Logo" />

# DarkNightSu

**A modern kernel-based root solution for Android — built for GKI & Non-GKI, dressed in frosted glass.**

[Website](https://max-araf.github.io/DarkNightSu/) · [Releases](https://github.com/max-araf/DarkNightSu/releases) · [Telegram Group](https://t.me/darknightsu) · [Report Issues](https://t.me/darknightsu/8) · [Developer](https://t.me/Max_araf_2010)

[![Release](https://img.shields.io/github/v/release/max-araf/DarkNightSu?style=for-the-badge&color=2563FF)](https://github.com/max-araf/DarkNightSu/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/max-araf/DarkNightSu/total?style=for-the-badge&color=00BBFF)](https://github.com/max-araf/DarkNightSu/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/max-araf/DarkNightSu/build-manager.yml?style=for-the-badge&label=Build)](https://github.com/max-araf/DarkNightSu/actions)
[![License](https://img.shields.io/badge/License-GPLv3-blue?style=for-the-badge)](LICENSE)

</div>

---

**DarkNightSu** is a standalone, open-source kernel-based root manager and kernel integration suite maintained by **[MAX ARAF](https://github.com/max-araf)**. It combines kernel-level root access (`LKM` for GKI 2.0 + built-in kernel integration for Non-GKI) with a redesigned frosted-glass Manager supporting both **Material 3** and **Miuix** skins, 10 dynamic colour palettes, a guided Install flow, a built-in compatibility checker, and a raw boot image extractor.

> [!IMPORTANT]
> Install only the official signed APK from the [Releases](https://github.com/max-araf/DarkNightSu/releases) page (or the official [Telegram Manager topic](https://t.me/darknightsu/2)). Debug and PR builds use a different signing key and will not be recognized as the official manager by the kernel.

> [!WARNING]
> Flashing the wrong `init_boot.img` or `boot.img` can cause a bootloop. Always back up your stock boot image first using **Backup and tools → Extract boot image** (or from your device's stock firmware).

---

## Highlights (v9.3.0)

| Feature | What it does |
| --- | --- |
| **Frosted-Glass UI & 10 Live Palettes** | Full glass styling across **Material 3** and **Miuix** (Light + Dark + Flat mode), floating Pill Dock or Glass Sidebar, Aurora Orbit splash, and a dynamic palette-reactive **MAX** contributor badge. |
| **Guided 2-Tap Install & Quick Start** | Clean step-by-step Install flow: *1. Check your phone → Quick Start → 2. Install* with Recommended badge, collapsible *Other methods*, *Backup and tools*, *Advanced*, and a frosted-glass pinned **Next** bar. |
| **Root Compatibility Checker** | Quick (non-root) and Deep (root-assisted) checks across 32-bit, 64-bit, and universal devices with vendor checks (Samsung, Motorola, Xiaomi/Redmi/POCO, Infinix/Tecno/itel, OnePlus, RedMagic/Nubia, Pixel/Nothing), `"I know my device"` spoof overrides, collapsible details, and **Brick Guard** pre-flash confirmation. |
| **Raw Boot Image Extractor** | Extracts `init_boot`, `boot`, `vendor_boot`, and `recovery` straight to `Download/DarkNightSu/` with 3-way partition size detection, header magic check, SHA-256 hash, and one-tap share. |
| **Non-GKI & GKI Ready** | Full GKI 2.0 LKM boot patching + Non-GKI built-in kernel integration, AnyKernel3 flashing, KPM patching, and SuSFS detection (including Non-GKI/BRENE SuSFS v2.3.0). |
| **DarkNight Toolbox & DarkTune** | Performance, Charging, Integrity, Monitor, Rescue tools, and live Side Info Bar (CPU, battery, temperature) — automatically unlocked when running a DarkNightSu kernel. |

---

## Compatibility

| Scenario | Method | Notes |
| --- | --- | --- |
| **GKI 2.0 devices (`5.10+` / `6.x`)** | LKM Mode | Recommended: patch `init_boot.img` (Android 13+) or `boot.img` via **Quick Start** in DarkNightSu Manager. |
| **Non-GKI / older kernels (`4.14+` / `4.19+`)** | Kernel Integration | Integrate `DarkNightSu` directly into your kernel source tree (see below) and flash via AnyKernel3. |
| **KPM features** | `CONFIG_KPM=y` | 64-bit (`arm64-v8a`); Non-GKI also requires `CONFIG_KALLSYMS=y` and `CONFIG_KALLSYMS_ALL=y`. |

**Supported Manager ABIs:** `arm64-v8a` · `armeabi-v7a` · `x86_64` · `riscv64` · `universal`

---

## Kernel Integration (GKI & Non-GKI)

Run this in the **root of your Linux kernel source tree** (where `drivers/` is located):

```bash
curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s main
```

Or pin a specific release tag (e.g. `v9.3.0`):

```bash
curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s v9.3.0
```

**Other setup options:**
- **Add as Git submodule:**
  ```bash
  curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s -- --submodule
  ```
- **Remove / clean up:**
  ```bash
  curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s -- --cleanup
  ```

**GitHub Actions workflow step (for custom kernel CI builders):**

```yaml
- name: Integrate DarkNightSu
  run: curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s main
```

**Required `defconfig` flags:**
```makefile
CONFIG_KSU=y
CONFIG_KPROBES=y
CONFIG_HAVE_KPROBES=y
CONFIG_KPROBE_EVENTS=y
# Optional (64-bit KPM support):
# CONFIG_KPM=y
```

> `kernel/Kbuild` already pins `com.darknight.ksu` and the official DarkNightSu Manager signing certificate, so kernels built with this source automatically work with DarkNightSu Manager out of the box.

---

## Community & Support

Join the official **DARKNIGHT** Telegram Forum ([t.me/darknightsu](https://t.me/darknightsu)):

| Topic | Link | Purpose |
| --- | --- | --- |
| **Manager** | [t.me/darknightsu/2](https://t.me/darknightsu/2) | Official signed DarkNightSu Manager APKs |
| **Manager - Spoofed** | [t.me/darknightsu/3](https://t.me/darknightsu/3) | Spoofed package builds |
| **Developing Manager** | [t.me/darknightsu/4](https://t.me/darknightsu/4) | CI / test builds |
| **Changelog** | [t.me/darknightsu/5](https://t.me/darknightsu/5) | Release notes and updates |
| **Issues** | [t.me/darknightsu/8](https://t.me/darknightsu/8) | Bug reports, compatibility reports, and logs |

---

## Repository Layout

| Path | Contents |
| --- | --- |
| `kernel/` | Kernel driver: hooks, policy, SuSFS, KPM, security, `setup.sh` |
| `userspace/` | `ksud` (Rust daemon), `ksuinit`, module tooling |
| `manager/` | DarkNightSu Manager Android app (Jetpack Compose, Material 3 & Miuix) |
| `uapi/` | Shared kernel/userspace headers |
| `site/` | Official website source ([max-araf.github.io/DarkNightSu](https://max-araf.github.io/DarkNightSu/)) |
| `.github/` | GitHub Actions CI, multi-ABI APK builds, Telegram auto-uploader |

---

## License & Credits

- `kernel/` is licensed under **GPL-2.0-only**.
- Everything else in this repository is licensed under **GPL-3.0-or-later** (see [LICENSE](LICENSE)).
- Built with appreciation for the open-source Android kernel root ecosystem: [KernelSU](https://github.com/tiann/KernelSU), [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra), [KernelPatch](https://github.com/bmax121/KernelPatch), and [SuSFS](https://gitlab.com/simonpunk/susfs4ksu).
