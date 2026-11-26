<div align="center">

<img src="docs/DarkNightSu-logo.png" width="140px" alt="DarkNightSu logo">

# DarkNightSu

**A kernel-based root manager for Android, dressed in glass.**

[Website](https://max-araf.github.io/DarkNightSu/) ·
[Releases](https://github.com/max-araf/DarkNightSu/releases) ·
[Telegram](https://t.me/YOUR_TELEGRAM) ·
[Issues](https://github.com/max-araf/DarkNightSu/issues)

[![Latest release](https://img.shields.io/github/v/release/max-araf/DarkNightSu?label=Release&logo=github)](https://github.com/max-araf/DarkNightSu/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/max-araf/DarkNightSu/total?label=Downloads&logo=github)](https://github.com/max-araf/DarkNightSu/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/max-araf/DarkNightSu/build-manager.yml?label=Build&logo=github)](https://github.com/max-araf/DarkNightSu/actions/workflows/build-manager.yml)
[![Kernel License: GPL v2](https://img.shields.io/badge/Kernel-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![Manager License: GPL v3](https://img.shields.io/github/license/max-araf/DarkNightSu?label=Manager&logo=gnu)](/LICENSE)

</div>

---

DarkNightSu is a kernel-based root solution built on
[KazeSU](https://github.com/KazeSU/KazeSU) (→ SukiSU Nova → SukiSU-Ultra → KernelSU).
The kernel does the work; the manager does everything around it, now with a
frosted-glass UI, 10 live colour palettes and a kernel toolbox.

> [!IMPORTANT]
> Install only the official manager APK from the
> [Releases](https://github.com/max-araf/DarkNightSu/releases) page — the file whose
> name ends in `-release.apk`. Debug, PR and CI builds use a different certificate
> and the kernel rejects them as unofficial.

> [!WARNING]
> If your phone's kernel does not support kernel root, **do not use this**. The
> manager cannot force root onto an unsupported kernel, and flashing a wrong
> image can brick your device. Always back up your original boot image.

## What is new in DarkNightSu

1. **10 live palettes** — logo, splash, toggles and icons all follow your choice.
2. **Frosted-glass UI** (Material 3 + Miuix), floating pill dock or glass sidebar, proper flat mode.
3. **DarkNight Toolbox** — Performance, Charging, Integrity, Monitor, Rescue.
4. **DarkTune** kernel tuning.
5. **Live side info bar** — CPU speed, battery, temperature.
6. **Aurora startup + first-run onboarding**, coloured by your palette.
7. Non-root users only see Home and Settings; root-only pages are hidden.

## Inherited from KazeSU

Kernel-based `su` with App Profile, KPM, built-in Zygisk, SUSFS management and
hiding, stealth mode, KazeMount, module batch tools with backup/restore, and
bootloop protection.

## Compatibility

| Scenario | Method | Notes |
|---|---|---|
| GKI devices (kernel 5.10+) | LKM mode | Recommended; flash the `.ko` matching your KMI, or patch a boot image |
| Non-GKI / older kernels (4.14+) | Compile the kernel | See [Kernel integration](#kernel-integration) |
| KPM features | `CONFIG_KPM=y` | 64-bit; non-GKI also needs `CONFIG_KALLSYMS=y` and `CONFIG_KALLSYMS_ALL=y` |

Architectures: `arm64-v8a`, `armeabi-v7a`, `x86_64`, `riscv64`.

## Install

1. Flash a compatible kernel, or load the LKM / patch your `boot.img` from the manager.
2. Install the manager APK from [Releases](https://github.com/max-araf/DarkNightSu/releases).
3. Open the manager and follow the first-run setup.

## Kernel integration

Run this in the **root of your kernel source** (see the upstream KazeSU README for the exact script line):

```bash
# see https://github.com/KazeSU/KazeSU#kernel-integration
```

Then enable `CONFIG_KSU`, and `CONFIG_KPM` on 64-bit kernels for KPM.

## Build

```bash
just bm   # cross-build ksud (aarch64) + bundled debug manager APK
just bk   # ksud only
```

`ksud` is built with [`cross`](https://github.com/cross-rs/cross); the manager is a
Gradle/Kotlin project under `manager/`.

## Repository layout

| Path | Contents |
|---|---|
| `kernel/` | Kernel module: hooks, policy, SUSFS, KPM, security |
| `userspace/` | `ksud` (Rust daemon), `ksuinit`, module tooling |
| `manager/` | Android manager app (Compose, Miuix/Material) |
| `uapi/` | Shared kernel/userspace headers |
| `docs/` | Guides and logo assets |
| `site/` | Source of the website (GitHub Pages) |
| `.github/` | CI workflows, release assembly |

## License

- `kernel/` is [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html).
- The anime `ic_launcher*` icon files from KazeSU are covered by `LICENSE_icon_English` / `LICENSE_icon_SC` — **do not reuse them**; DarkNightSu ships its own logo.
- Everything else is [GPL-3.0-or-later](LICENSE).

## Acknowledgements

[KazeSU](https://github.com/KazeSU/KazeSU), [SukiSU Nova](https://github.com/SukiSU-Nova/SukiSU-Nova),
[SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra), [KernelSU](https://github.com/tiann/KernelSU),
[KernelPatch](https://github.com/bmax121/KernelPatch), [susfs](https://gitlab.com/simonpunk/susfs4ksu),
and Kagami (Rouyashiki/YukiSU) for the magic-mount backend.
