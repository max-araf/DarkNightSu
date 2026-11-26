# DarkNightSu Setup & Build Guide

## 1. Apply This Overlay to Your Forked Repo (`max-araf/DarkNightSu`)
In Termux (or PC):
```bash
git clone https://github.com/max-araf/DarkNightSu.git
cd DarkNightSu
unzip -o /path/to/DarkNightSu-overlay-v1.0.0.zip
git add .
git commit -m "feat: rebrand to DarkNightSu & add DarkNight Toolbox"
git push origin main
```

## 2. Configure GitHub Secrets (for Signed APKs & Matching GKI LKMs)
In GitHub -> `max-araf/DarkNightSu` -> **Settings -> Secrets and variables -> Actions**, add:
- `KEYSTORE` : output of `base64 -w 0 release.jks`
- `KEYSTORE_PASSWORD` : your keystore password
- `KEY_ALIAS` : `mykey`
- `KEY_PASSWORD` : your key password

Note: `build-manager.yml` automatically extracts your exact `KSU_EXPECTED_SIZE` and `KSU_EXPECTED_HASH` from `secrets.KEYSTORE` when building all GKI LKM `.ko` files!

## 3. For Local Non-GKI Kernel Builds
Get your exact certificate DER hex size in Termux:
```bash
keytool -exportcert -alias mykey -keystore release.jks -file cert.der
printf '0x%04x\n' $(stat -c%s cert.der)
```
Then update `KSU_EXPECTED_SIZE` in `kernel/Kbuild` (your `KSU_EXPECTED_HASH` is already set to `b28a09ad9d6399dbd69640b05abd973865340d20bc1c462729f1ad63f8a9fd75`).

## 4. Kernel Developer Integration One-Liner (GKI & Non-GKI)
```bash
curl -LSs "https://raw.githubusercontent.com/max-araf/DarkNightSu/main/kernel/setup.sh" | bash -s main
```
Enable `CONFIG_KSU=y` (and `CONFIG_KPM=y` on 64-bit kernels).

## 5. Trigger a Release Build
Push a tag to automatically build `armeabi-v7a`, `arm64-v8a`, `x86_64`, `riscv64`, and `universal` signed APKs + all GKI LKM `.ko` files:
```bash
git tag -a v1.0.0 -m "DarkNightSu v1.0.0"
git push origin v1.0.0
```
