# DarkNightSu Release + Website Guide (Banglish, step by step)

## A) Website live korar upay (GitHub Pages) — 5 minute
1. `site/` folder ta repo-r root e rakho (ei zip e ache), ar `.github/workflows/pages.yml` o rakho.
2. `site/index.html` er upor dike `LINKS` block e `telegram` ar `drive` link bosao.
3. Commit + push: `git add site .github/workflows/pages.yml README.md && git commit -m "docs: website + README" && git push`
4. GitHub e repo -> **Settings -> Pages -> Build and deployment -> Source = "GitHub Actions"**.
5. **Actions** tab e "Deploy website" green hole site live: `https://max-araf.github.io/DarkNightSu/`
6. Link ta repo er **About** (daan pashe gear icon) -> Website e boshao, ar description + topics (root, kernelsu, android, magisk-alternative) dao.

Site customise: `:root` er CSS variable (colour), `PALS` (palette list), shob ek file e. Alada build lagbe na.

## B) Repo sajano
- Root `README.md` replace koro (ei zip er ta). `[Telegram]` link bodlao. Kernel integration section e KazeSU er original script line ta boshao.
- `DARKNIGHT_SETUP.md` ta `docs/` e soriye dao (root clean thakbe).
- `docs/` e screenshot rakho (`docs/screenshots/home.png`...) ar README te `<img>` diye dekhao. Site e o screenshot section add kora jay.
- **License rule (zaroori):** KazeSU GPL-3.0 — tai DarkNightSu o GPL-3.0 thakte hobe, `LICENSE` file thak, KazeSU/SukiSU er credit thak (README te ache). Anime `ic_launcher` icon file gulo (LICENSE_icon_*) **reuse korba na**, tomar nijer logo ache.
- Source open thakle "Open source" claim thik; keystore/secret kokhono commit korba na.

## C) Release (v1.0.0 / v8.1.0) — GitHub Releases
1. Tag: `git tag v1.0.0 && git push origin v1.0.0` (ba existing `release.yml` workflow chalao).
2. Releases -> **Draft a new release** -> tag select -> title "DarkNightSu v1.0.0".
3. Assets: signed `DarkNightSu_..._release.apk` + `.ko` LKM file gulo.
4. Release notes template:

```
## DarkNightSu vX.Y.Z
### New
- ...
### Fixed
- ...
### Install
1. Kernel ba LKM ready koro  2. APK install  3. Open -> setup
> Warning: kernel root support na thakle use korba na, brick hote pare.
Support/bug: Telegram <link>   Mirror: <drive link>
```
5. **Latest release** tick dao -> Publish.

## D) Telegram post
Ager pathano Bangla post ta use koro, shesh e site link `https://max-araf.github.io/DarkNightSu/` jog koro.
