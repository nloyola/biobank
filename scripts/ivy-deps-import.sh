#!/usr/bin/env bash
#
# Unpack Ivy dependencies transferred from another machine.
# Run from the project root.
#
# Usage:
#   scripts/ivy-deps-import.sh [archive-file]
#
# Default archive file: ivy-deps.tar.gz

set -euo pipefail

ARCHIVE="${1:-ivy-deps.tar.gz}"

if [[ ! -f "${ARCHIVE}" ]]; then
    echo "Error: ${ARCHIVE} not found."
    echo "Transfer it from the source machine first."
    exit 1
fi

echo "Unpacking ${ARCHIVE}..."
tar -xzf "${ARCHIVE}"
echo "Done. 'ant resolve' will skip downloading — dependencies are already in place."
