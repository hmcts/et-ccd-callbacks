#!/bin/bash
# Script to load the AppArmor profile on the host system
# Called automatically by devcontainer initializeCommand

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROFILE_PATH="$SCRIPT_DIR/apparmor-profile"

# Check if AppArmor is available
if ! command -v apparmor_parser &> /dev/null; then
    echo "AppArmor not available, skipping profile load"
    exit 0
fi

if [ ! -f "$PROFILE_PATH" ]; then
    echo "Error: AppArmor profile not found at $PROFILE_PATH"
    exit 1
fi

echo "Loading AppArmor profile..."
# Always reload (-r) to pick up any changes
sudo apparmor_parser -r -W "$PROFILE_PATH"
echo "AppArmor profile 'agent-devcontainer' loaded successfully"
