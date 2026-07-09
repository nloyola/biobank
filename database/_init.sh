#!/bin/bash
set -x
set -e
set -o pipefail

# The dump is bind-mounted at /db_initial.sql.gz. If the host source did not
# exist when the container was created, Docker auto-creates an empty directory
# there; the gzip below then fails, but without pipefail that failure is masked
# by mariadb succeeding on empty input, silently seeding an EMPTY database.
# Fail loudly on a missing/non-regular dump instead.
if [ ! -f /db_initial.sql.gz ]; then
    echo "FATAL: /db_initial.sql.gz is missing or not a regular file; refusing to seed an empty database." >&2
    exit 1
fi

gzip -dc /db_initial.sql.gz | mariadb -uroot -p${MARIADB_ROOT_PASSWORD} ${MARIADB_DATABASE}
