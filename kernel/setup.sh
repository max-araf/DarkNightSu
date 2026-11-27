#!/bin/sh
set -eu

GKI_ROOT=$(pwd)
REPO_URL="https://github.com/max-araf/DarkNightSu.git"

display_usage() {
    echo "DarkNightSu Kernel Integration Script (GKI & Non-GKI)"
    echo "Usage: $0 [--cleanup | --submodule | <commit-or-tag>]"
    echo "  --cleanup:              Cleans up previous modifications made by the script."
    echo "  --submodule:            Adds DarkNightSu as a git submodule inside the kernel tree."
    echo "  <commit-or-tag>:        Sets up or updates DarkNightSu to specified tag, branch, or commit."
    echo "  -h, --help:             Displays this usage information."
    echo "  (no args):              Sets up or updates DarkNightSu to the latest tagged version (or main)."
}

initialize_variables() {
    if test -d "$GKI_ROOT/common/drivers"; then
         DRIVER_DIR="$GKI_ROOT/common/drivers"
    elif test -d "$GKI_ROOT/drivers"; then
         DRIVER_DIR="$GKI_ROOT/drivers"
    else
         echo '[ERROR] "drivers/" directory not found. Run this script from the root of your kernel source.'
         exit 127
    fi

    DRIVER_MAKEFILE=$DRIVER_DIR/Makefile
    DRIVER_KCONFIG=$DRIVER_DIR/Kconfig
}

perform_cleanup() {
    echo "[+] Cleaning up DarkNightSu..."
    [ -L "$DRIVER_DIR/kernelsu" ] && rm "$DRIVER_DIR/kernelsu" && echo "[-] Symlink removed."
    grep -q "kernelsu" "$DRIVER_MAKEFILE" && sed -i '/kernelsu/d' "$DRIVER_MAKEFILE" && echo "[-] Makefile reverted."
    grep -q "drivers/kernelsu/Kconfig" "$DRIVER_KCONFIG" && sed -i '/drivers\/kernelsu\/Kconfig/d' "$DRIVER_KCONFIG" && echo "[-] Kconfig reverted."
    if [ -d "$GKI_ROOT/KernelSU" ]; then
        rm -rf "$GKI_ROOT/KernelSU" && echo "[-] KernelSU directory deleted."
    fi
}

link_kernel_driver() {
    cd "$DRIVER_DIR"
    ln -sf "$(realpath --relative-to="$DRIVER_DIR" "$GKI_ROOT/KernelSU/kernel")" "kernelsu" && echo "[+] Symlink created."
    grep -q "kernelsu" "$DRIVER_MAKEFILE" || printf "\nobj-\$(CONFIG_KSU) += kernelsu/\n" >> "$DRIVER_MAKEFILE" && echo "[+] Modified Makefile."
    grep -q "source \"drivers/kernelsu/Kconfig\"" "$DRIVER_KCONFIG" || sed -i "/endmenu/i\source \"drivers/kernelsu/Kconfig\"" "$DRIVER_KCONFIG" && echo "[+] Modified Kconfig."
    echo "[+] DarkNightSu kernel integration complete! Enable CONFIG_KSU=y (and CONFIG_KPM=y on 64-bit if desired)."
}

setup_kernelsu() {
    echo "[+] Setting up DarkNightSu..."
    test -d "$GKI_ROOT/KernelSU" || git clone "$REPO_URL" KernelSU && echo "[+] Repository cloned."
    cd "$GKI_ROOT/KernelSU"
    git stash 2>/dev/null || true
    git fetch --all --tags
    if [ -z "${1-}" ]; then
        LATEST_TAG=$(git describe --abbrev=0 --tags 2>/dev/null || true)
        if [ -n "$LATEST_TAG" ]; then
            git checkout "$LATEST_TAG" && echo "[-] Checked out latest tag: $LATEST_TAG"
        else
            git checkout main && git pull && echo "[-] Checked out main branch."
        fi
    else
        git checkout "$1" && echo "[-] Checked out $1." || echo "[-] Checkout default branch"
    fi
    link_kernel_driver
}

setup_submodule() {
    echo "[+] Adding DarkNightSu as git submodule..."
    test -d "$GKI_ROOT/KernelSU" || git submodule add "$REPO_URL" KernelSU
    git submodule update --init --recursive KernelSU
    link_kernel_driver
}

if [ "$#" -eq 0 ]; then
    initialize_variables
    setup_kernelsu
elif [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    display_usage
elif [ "$1" = "--cleanup" ]; then
    initialize_variables
    perform_cleanup
elif [ "$1" = "--submodule" ]; then
    initialize_variables
    setup_submodule
else
    initialize_variables
    setup_kernelsu "$@"
fi
