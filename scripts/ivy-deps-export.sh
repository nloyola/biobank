#!/usr/bin/env bash
#
# Pack resolved Ivy dependencies into a tarball for transfer to another machine.
# Run from the project root after 'ant resolve' has been run successfully.
#
# Usage:
#   scripts/ivy-deps-export.sh [output-file]
#
# Default output file: ivy-deps.tar.gz

set -euo pipefail

ARCHIVE="${1:-ivy-deps.tar.gz}"

if [[ ! -f lib/.dependencies.resolved ]]; then
    echo "Error: lib/.dependencies.resolved not found."
    echo "Run 'ant resolve' first to download dependencies."
    exit 1
fi

echo "Packing Ivy dependencies into ${ARCHIVE}..."

tar -czf "${ARCHIVE}" \
    lib \
    eclipse_ws/biobank.common/lib \
    eclipse_ws/biobank.gui.common/lib \
    eclipse_ws/biobank.mvp/lib \
    eclipse_ws/biobank2/lib \
    eclipse_ws/biobank2.tools/lib \
    eclipse_ws/biobank2.tests/lib

echo "Done. Copy ${ARCHIVE} to the target machine and run scripts/ivy-deps-import.sh"
