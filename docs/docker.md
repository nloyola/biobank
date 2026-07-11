# Docker

Follow these instructions to get Biobank running under Docker on a Linux host.

A Docker image is available on Docker Hub [here](https://hub.docker.com/repository/docker/nloyola/biobank/general).

> **Automated provisioning:** to set up a new host end-to-end (install Docker, clone the
> repo, render `.env`, generate the TLS certificate, build the images, and start the
> stack), use the Ansible playbook - see [ansible/README.md](../ansible/README.md). The
> steps below are the manual equivalent.

## Setup

1. Clone the repository:

    ```sh
    git clone https://github.com/nloyola/biobank.git /opt/biobank/biobank-server
    cd /opt/biobank/biobank-server
    ```

1. Create `.env` in the project root with the following content:

    ```ini
    MODE=DEVELOPMENT
    COMPOSE_PROJECT_NAME=bb
    uid=1000
    gid=1000

    LOG_LEVEL=DEBUG

    DB_HOST=localhost
    DB_PORT=3306
    DB_ROOT_USER=root
    DB_ROOT_PASSWORD=changeme
    DB_NAME=biobank
    DB_USER=biobank
    DB_PASSWORD=changeme

    # Folder (relative to docker/) whose ZIPs the port-80 download page serves.
    DOWNLOADS_DIR=../product/buildDirectory/I.BioBank
    ```

    Replace `changeme` with your chosen credentials.

1. Copy a database dump into place:

    ```sh
    cp __path_to_dump__ /opt/biobank/biobank-server/database/db_initial.sql.gz
    ```

## Building the image

Run this when there are Dockerfile or configuration changes. The script
automatically downloads Apache Tomcat 8.5.30 and Apache Ant 1.9.0 into
`docker/tomcat/` if they are not already present, and generates the SSL
certificate if `docker/nginx-selfsigned.crt` does not exist.

```sh
cd /opt/biobank/biobank-server
./bb-build.sh
```

The SSL certificate generation prompts twice:

- **CN** — the hostname or IP address users type into the thick client
  (e.g. `192.168.50.3` or `biobank.cbsr.ualberta.ca`). Defaults to the
  machine's local IP address.
- **subjectAltName** — the SAN entries for the certificate. Defaults to
  `IP:<local-ip>,DNS:localhost`. Add additional entries separated by commas
  if clients connect via different names or addresses
  (e.g. `IP:192.168.50.3,DNS:biobank.example.com`).

Java 7 enforces Subject Alternative Name (SAN) verification and will reject
certificates that lack a SAN matching the address the thick client connects
to. The CN alone is not sufficient.

The cert and key are written to `docker/` via the volume mount. Do not commit
these files — the key is secret and the certificate is specific to this host.

## Running Biobank

Start all containers:

```sh
cd /opt/biobank/biobank-server
./bb-start.sh
```

On first run the database container imports the dump, which may take a few minutes. The Tomcat
container builds and deploys the web application automatically via its entrypoint. Look for these
lines to confirm a successful startup:

```
tomcat-1  | INFO  [DbMigrator] Current schema version: 1.7
tomcat-1  | INFO  [DbMigrator] Schema is up to date. No migration necessary.
```

On first run, follow the logs to confirm startup before using the application:

```sh
docker compose --env-file .env -f docker/compose.yaml --project-directory docker logs -f
```

To check container status:

```sh
./bb-status.sh
```

To stop and remove the containers:

```sh
./bb-stop.sh
```

## Troubleshooting

### nginx fails to load the SSL certificate

If the containers are started before `bb-build.sh` has generated the certificate,
Docker creates `docker/nginx-selfsigned.crt` and `docker/nginx-selfsigned.key` as
directories rather than files. nginx then fails with:

```
nginx: [emerg] cannot load certificate "/etc/nginx/ssl/nginx-selfsigned.crt": PEM_read_bio_X509_AUX() failed
```

Fix: stop the containers, remove the directories, and rebuild:

```sh
./bb-stop.sh
rm -rf docker/nginx-selfsigned.crt docker/nginx-selfsigned.key
./bb-build.sh
```

## Redeploying after a code change

With the containers running:

```sh
./bb-deploy.sh
```

## Publishing the image to Docker Hub

```sh
docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache
docker push nloyola/biobank:0.1
```

Replace `nloyola` with your Docker Hub account name and `0.1` with the new version number.
