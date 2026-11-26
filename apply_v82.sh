#!/bin/sh
# Run in the ROOT of the DarkNightSu repo after unzipping this kit over it.
set -e
cd manager/app/src/main/assets/about/contributors
for f in *.jpg; do [ "$f" != "max-araf.jpg" ] && git rm -q --ignore-unmatch "$f" 2>/dev/null || true; done
for f in *.jpg; do [ "$f" != "max-araf.jpg" ] && rm -f "$f"; done
cd - >/dev/null
git add -A site manager apply_v82.sh 2>/dev/null || true
git status --short | head -40
echo "Now: git commit -m 'feat: v8.2 About (author only), Telegram links, send logs, new website' && push"
