#!/bin/sh
# Run in the ROOT of your DarkNightSu repo (after unzipping this kit over it).
# Safe cleanup only: removes upstream KazeSU docs/images. Does NOT touch code, package names, kernel, hash, workflows.
set -e
git rm -rq --ignore-unmatch docs/zh docs/KazeSU.svg docs/KazeSU-mini.png
echo "[ok] removed old KazeSU doc images"
git add README.md docs site .github/workflows/pages.yml manager
git status --short | head -30
echo "Now: git commit -m 'docs: DarkNightSu README, website, updater points to DarkNightSu releases' && git push"
