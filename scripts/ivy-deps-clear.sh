#!/usr/bin/env bash
#
# Remove resolved Ivy dependencies so they can be replaced (e.g. via ivy-deps-import.sh).
# Run from the project root.

set -euo pipefail

echo "Clearing Ivy dependencies..."

rm -rf \
    lib/build \
    lib/hibernatetool \
    lib/biobank.webapp \
    lib/flyway \
    lib/.dependencies.resolved \
    eclipse_ws/biobank.common/lib/client \
    eclipse_ws/biobank.common/lib/server \
    eclipse_ws/biobank.gui.common/lib \
    eclipse_ws/biobank.mvp/lib \
    eclipse_ws/biobank2/lib \
    eclipse_ws/biobank2.tools/lib \
    eclipse_ws/biobank2.tests/lib

echo "Done. Run 'ant resolve' or scripts/ivy-deps-import.sh to restore dependencies."
