#!/usr/bin/env bash
# Ansible vault password client: prints the vault password to stdout.
# The password itself lives in `pass` (the standard Unix password store),
# not in this file, so this script is safe to commit.
#
# Setup for a new operator:
#   pass insert ansible/biobank-vault   # store your vault password
set -euo pipefail
exec pass show ansible/biobank-vault
