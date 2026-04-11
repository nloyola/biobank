#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

# Ensure Tomcat is present in the build context
if [ ! -d docker/tomcat/apache-tomcat-8.5.30 ]; then
    echo "Downloading Apache Tomcat 8.5.30..."
    curl -fsSL https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.30/bin/apache-tomcat-8.5.30.tar.gz \
        | tar -xz -C docker/tomcat/
fi

# Ensure Ant is present in the build context
if [ ! -d docker/tomcat/apache-ant-1.9.0 ]; then
    echo "Downloading Apache Ant 1.9.0..."
    curl -fsSL https://archive.apache.org/dist/ant/binaries/apache-ant-1.9.0-bin.tar.gz \
        | tar -xz -C docker/tomcat/
fi

docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache

# Generate the SSL certificate if not already present
if [ ! -f docker/nginx-selfsigned.crt ] || [ ! -f docker/nginx-selfsigned.key ]; then
    echo "Generating SSL certificate..."
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker \
        run --no-deps --rm tomcat ant nginx-cert-gen
fi
