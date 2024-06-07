# Docker

Follow these instructions to get Biobank running under Docker. These instructions are meant to be used on a computer running Linux.

A Docker image to run Biobank is available on docker hub [here](https://hub.docker.com/repository/docker/nloyola/biobank/general).

## Setup

1. Clone the project on the virtual machine or computer you wish to run Biobank on.

    ```sh
    cd __root_folder__
    git clone git@github.com:CBSR-Biobank/biobank.git
    cd biobank
    ```

    Replace `__root_folder__` with the name of the folder you wish to have biobank installed at (usually `/opt/biobank/biobank-thick-client`).

1. The instructions given below depend on these files being present in the project folder. Download the *unversioned* ZIP file and unzip it:

    ```sh
    cd /opt/biobank/biobank-thick-client
    curl https://biobank.cbsr.ualberta.ca/unversioned/biobank_unversioned_v3.10.5.zip -o biobank_unversioned_v3.10.5.zip
    unzip biobank_unversioned_v3.10.5.zip
    ```
    You need to have `zip` and `unzip` installed.

1. Create a file named `.env`, at the project's root folder,' with the following content:

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

    Replace `changeme` with the values you want to use.

## Building the image

Use these instructions to build a new image if there are configuration or code changes.

1. Run the containers:

    ```sh
    cd /opt/biobank/biobank-thick-client
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache
    ```

1. Start a bash shell in the JBoss container and rebuild the project:

    ```sh
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker run jboss bash
    ant -Doffline=1 deploy-jboss
    ```
     Exit the container shell by pressing `CTRL-d`.

1. Copy the rebuilt files to the JBoss folder:

    ```sh
    cd /opt/biobank/biobank-thick-client/docker
    ./copy-built-files.sh
    ```

1. Start the docker containers:

    ```sh
    cd /opt/biobank/biobank-thick-client
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker up
    ```

    Look for the following line in the output:

    ```
    b-jboss-1  | 13:48:47,207 INFO  [STDOUT] 13:48:47,207 INFO  [DbMigrator] Current schema version: 1.6
    bb-jboss-1  | 13:48:47,208 INFO  [STDOUT] 13:48:47,208 INFO  [DbMigrator] Schema is up to date. No migration necessary.
    ```

    If they show up, then the web application was built successfully.

1. Create the Docker image:

    ```sh
    cd /opt/biobank/biobank-thick-client/docker
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache
    ```
1. Push the image to Docker Hub:

    ```sh
    docker push nloyola/biobank:0.1
    ```

    Replace `nloyola` with the name of the Docker Hub account you wish to use, and replace `0.1` with the image's new version number.


## Running Biobank on Docker

1. Create a self signed certificate:

    ```sh
    cd /opt/biobank/biobank-thick-client/docker/docker
    ./nginx-selfsigned.sh
    ```

    You may enter blank values for all prompts (just press the `Enter` key) except for the **Common Name**
    one. The common name should be the DNS name users will enter into the thick client to connect to the
    server. For example, for CBSR's biobank server, enter:

    ```
    biobank.cbsr.ualberta.ca
    ```

1. Copy a working copy of the database:

    ```sh
    cd /opt/biobank/biobank-thick-client
    cp __biobank_database__ database/db_initial.sql.gz
    ```
1. Start the containers:

    ```sh
    cd /opt/biobank/biobank-thick-client
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker up
    ```

    The first time the database container runs, it will import the database. This may take a few minutes to finish.

    Once the database has been imported, shut down the containers by pressing `CTRL-c`.

1. Now, restart the containers in detached mode:

    ```sh
    cd /opt/biobank/biobank-thick-client
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker up --detach
    ```

    By running in detached mode, the containers will restart if the VM is rebooted.

You can now test the connection to the new server using the thick client.

## Cron Job

On the **biboank-new.cbsr.ualberta.ca** VM, there is a cron job that dumps the databse to a file every night.
The script is at `/opt/biobank/biobank_db_backup.sh`.

The setting for the cron job is as follows.

 ```
 05 2 * * * /opt/biobank/biobank_db_backup.sh
 ```

The script places a file into the `/data/dbbackups/` folder as a gzipped SQL file with the date the file was created.

For the script to work, the database login credentials are stored in the file `/home/biobank/.my.cnf`.

This file has the following format:

```ini
[mariadb-client]
user=changeme
password=changeme

[mariadb-dump]
user=changeme
password=changeme
```
