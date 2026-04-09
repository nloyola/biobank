# Docker

Follow these instructions to get Biobank running under Docker on a Linux host.

A Docker image is available on Docker Hub [here](https://hub.docker.com/repository/docker/nloyola/biobank/general).

## Setup

1. Clone the repository:

    ```sh
    git clone git@github.com:CBSR-Biobank/biobank.git /opt/biobank/biobank-thick-client
    cd /opt/biobank/biobank-thick-client
    ```

1. Download and unzip the unversioned assets:

    ```sh
    curl https://biobank.cbsr.ualberta.ca/unversioned/biobank_unversioned_v3.10.5.zip -o biobank_unversioned_v3.10.5.zip
    unzip biobank_unversioned_v3.10.5.zip
    ```

    You need `zip` and `unzip` installed.

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
    ```

    Replace `changeme` with your chosen credentials.

1. Generate a self-signed SSL certificate for Nginx. Requires `openssl` and
   `ant` on your PATH.

   Java 7 enforces Subject Alternative Name (SAN) verification and will reject
   certificates that lack a SAN matching the address the thick client connects
   to. The CN alone is not sufficient.

    ```sh
    cd /opt/biobank/biobank-thick-client
    ant nginx-cert-gen
    ```

   The target prompts twice:

   - **CN** — the hostname or IP address users type into the thick client
     (e.g. `192.168.50.3` or `biobank.cbsr.ualberta.ca`). Defaults to the
     machine's local IP address.
   - **subjectAltName** — the SAN entries for the certificate. Defaults to
     `IP:<local-ip>,DNS:localhost`. Add additional entries separated by commas
     if clients connect via different names or addresses
     (e.g. `IP:192.168.50.3,DNS:biobank.example.com`).

   The target writes `docker/nginx-selfsigned.crt` and
   `docker/nginx-selfsigned.key`. Do not commit these files — the key is
   secret and the certificate is specific to this host.

1. Provide the Tomcat and Ant distributions required by the Tomcat Docker image.
   These are not committed to the repository because of their size.

   Download Apache Tomcat 8.5.30 and extract it into `docker/tomcat/`:

    ```sh
    curl -O https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.30/bin/apache-tomcat-8.5.30.tar.gz
    tar -xzf apache-tomcat-8.5.30.tar.gz -C docker/tomcat/
    rm apache-tomcat-8.5.30.tar.gz
    ```

   Extract the Ant distribution (already in the repo) into `docker/tomcat/`:

    ```sh
    tar -xjf docker/apache-ant-1.9.0-bin.tar.bz2 -C docker/tomcat/
    ```

   After this, `docker/tomcat/` should contain `apache-tomcat-8.5.30/` and
   `apache-ant-1.9.0/` alongside the `Dockerfile` and `entrypoint.sh`.

1. Copy a database dump into place:

    ```sh
    cp __path_to_dump__ /opt/biobank/biobank-thick-client/database/db_initial.sql.gz
    ```

## Building the image

Run this when there are Dockerfile or configuration changes:

```sh
cd /opt/biobank/biobank-thick-client
docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache
```

## Running Biobank

Start all containers:

```sh
cd /opt/biobank/biobank-thick-client
docker compose --env-file .env -f docker/compose.yaml --project-directory docker up
```

On first run the database container imports the dump, which may take a few minutes. The Tomcat
container builds and deploys the web application automatically via its entrypoint. Look for these
lines to confirm a successful startup:

```
tomcat-1  | INFO  [DbMigrator] Current schema version: 1.7
tomcat-1  | INFO  [DbMigrator] Schema is up to date. No migration necessary.
```

Once the database has been imported, shut down with `CTRL-c` and restart in detached mode so the
containers survive a reboot:

```sh
docker compose --env-file .env -f docker/compose.yaml --project-directory docker up --detach
```

## Trusting the server certificate on the client machine

The thick client connects over HTTPS. Because the server uses a self-signed
certificate, each client machine must import that certificate into its JRE
truststore.

1. Copy `docker/nginx-selfsigned.crt` from the server to the client machine.

2. Import it into the JRE truststore with `keytool`. Adjust `$JAVA_HOME` to
   your JRE installation path:

    ```sh
    keytool -import -trustcacerts -alias biobank-server \
        -file nginx-selfsigned.crt \
        -keystore $JAVA_HOME/lib/security/cacerts
    ```

    The default truststore password is `changeit`.

3. Confirm with `yes` when prompted to trust the certificate.

After importing, restart the thick client. It should connect without SSL errors.

## Redeploying after a code change

With the containers running, exec into the Tomcat container and run Ant:

```sh
docker compose --env-file .env -f docker/compose.yaml --project-directory docker exec tomcat ant deploy_tomcat
```

## Building the thick client

The thick client is an Eclipse RCP application built using a dedicated Docker image that
provides Java 1.7, Ant, Eclipse 3.7 Indigo Classic (with PDE), and the Eclipse delta pack
(required for cross-platform builds).

### Build the image (once)

Download the Eclipse 3.7.2 Indigo Classic and delta pack from the Eclipse archive and place
them in `docker/build-client/`:

- `eclipse-SDK-3.7.2-linux-gtk-x86_64.tar.gz`
- `eclipse-3.7.2-delta-pack.zip`

Both are available at:
`https://archive.eclipse.org/eclipse/downloads/drops/R-3.7.2-201202080800/`

Then build the image:

```sh
cd /opt/biobank/biobank-thick-client
ln -f docker/apache-ant-1.9.0-bin.tar.bz2 docker/build-client/apache-ant-1.9.0-bin.tar.bz2
docker build -t biobank-build-client docker/build-client/
```

### Build the Windows client

```sh
cd /opt/biobank/biobank-thick-client
docker run --rm -v $(pwd):/opt/biobank biobank-build-client \
    ant product -Dconfigs="win32, win32, x86"
```

The distributable is written to `product/buildDirectory/`.

### Build the Linux client

```sh
cd /opt/biobank/biobank-thick-client
docker run --rm -v $(pwd):/opt/biobank biobank-build-client \
    ant product -Dconfigs="linux, gtk, x86_64"
```

### Build all platforms

Omit `-Dconfigs` to build for all platforms at once:

```sh
docker run --rm -v $(pwd):/opt/biobank biobank-build-client ant product
```

## Publishing the image to Docker Hub

```sh
docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache
docker push nloyola/biobank:0.1
```

Replace `nloyola` with your Docker Hub account name and `0.1` with the new version number.

## Cron job

On **biobank-new.cbsr.ualberta.ca** a cron job dumps the database nightly:

```
05 2 * * * /opt/biobank/biobank_db_backup.sh
```

The script writes a gzipped SQL file dated by creation date to `/data/dbbackups/`. Database
credentials are stored in `/home/biobank/.my.cnf`:

```ini
[mariadb-client]
user=changeme
password=changeme

[mariadb-dump]
user=changeme
password=changeme
```
