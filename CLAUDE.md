# Biobank

Biobank is a Java client-server application for the Canadian BioSample Repository (CBSR). It manages biospecimens and patients across studies and clinics. The thick client is an Eclipse RCP application; the server runs on JBoss 4.0.5.GA backed by MariaDB, with Nginx as the reverse proxy. Dependencies are managed with Apache Ivy. The build system is Apache Ant.

## Critical Constraints

- **Java 1.6 required.** JBoss 4.0.5.GA is incompatible with Java 7 and above. Do not upgrade the Java version.
- **Legacy TLS required.** The thick client communicates with JBoss using TLS 1.0 and old cipher suites (e.g., `AES128-SHA`). Do not modernize the SSL/TLS configuration.

## Project Structure

```
eclipse_ws/          Eclipse workspace — all Java source lives here
  biobank2/          Main Eclipse RCP thick client application
  biobank.common/    Shared domain model and server-side code
  biobank.gui.common/ Shared GUI components
  biobank.mvp/       MVP (Model-View-Presenter) UI layer
  biobank2.tools/    CLI tools (e.g., patient import)
  biobank2.tests/    Test suite
docker/              Docker configuration (Dockerfile, compose.yaml, Nginx, etc.)
docs/                Project documentation
scripts/             Utility scripts
build.xml            Main Ant build file (run `ant -projecthelp` for targets)
ivy.xml              Ivy dependency declarations
build.properties     Build configuration (version numbers, paths)
db.properties        Database connection settings
```

The `.project` files in `eclipse_ws/` are committed and authoritative — they define the Eclipse project type and build configuration for each sub-project.

## Building

Dependencies are fetched with Ivy. Common Ant targets:

```sh
ant resolve              # Fetch Ivy dependencies
ant deploy               # Build and deploy to JBoss + Eclipse projects (default)
ant deploy-jboss         # Deploy the server webapp to JBoss only
ant deploy-eclipse       # Retrieve dependencies for Eclipse projects only
ant build-eclipse-projects # Build the Eclipse RCP client
ant product              # Build the distributable RCP product
ant biobank-cli-dist     # Build the Biobank CLI JAR
ant test                 # Run the test suite
ant clean                # Clean all build output
```

## Running with Docker

The server stack (JBoss + MariaDB + Nginx) runs under Docker Compose.

1. Copy `env.template` to `.env` and fill in credentials.

2. Build the image:

    ```sh
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker build --no-cache
    ```

3. Start the stack:

    ```sh
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker up
    ```

4. To rebuild the server webapp inside the running container:

    ```sh
    docker compose --env-file .env -f docker/compose.yaml --project-directory docker run jboss bash
    ant -Doffline=1 deploy-jboss
    ```

See `docs/docker.md` for the full setup guide, including SSL certificate generation and database import.

## Eclipse IDE Setup

The workspace is in `eclipse_ws/`. To open it in Eclipse:

1. Open Eclipse and select `eclipse_ws/` as the workspace directory.
2. Import the existing projects: **File > Import > Existing Projects into Workspace**, point at `eclipse_ws/`.
3. All `.project` files are committed — Eclipse will recognize each sub-project as a Java project automatically.

The build target is Java 1.6 (`ant.build.javac.target=1.6` in `build.properties`). Ensure your Eclipse JDK is set to JDK 6.
