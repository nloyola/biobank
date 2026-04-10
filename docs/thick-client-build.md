# Thick Client Build — Docker Container

## Overview

The `biobank-build-client` Docker container builds the Biobank Eclipse RCP thick client for all platforms (Windows, Mac, Linux) using the Eclipse PDE headless build system.

## Building the image (once)

Download the Eclipse 3.7.2 Indigo Classic SDK and delta pack from the Eclipse archive and place
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

## Running the build

### Build all platforms

```sh
docker run --rm -v $(pwd):/opt/biobank biobank-build-client ant product
```

### Build a specific platform

Pass `-Dconfigs` to limit the build:

```sh
# Linux x86_64
docker run --rm -v $(pwd):/opt/biobank biobank-build-client \
    ant product -Dconfigs="linux, gtk, x86_64"

# Windows x86
docker run --rm -v $(pwd):/opt/biobank biobank-build-client \
    ant product -Dconfigs="win32, win32, x86"
```

Output ZIPs land in `product/buildDirectory/I.BioBank/`.

## Bundling a JRE into the client ZIPs

After building, use `scripts/biobankZipAddJre.py` to embed a Java 7 JRE into
each platform ZIP. This produces self-contained archives that run on machines
without Java installed.

The script requires Azul Zulu Java 7 JRE ZIPs for each target platform. Keep
them in a persistent cache directory (e.g. `/opt/biobank/jre-cache`). Use
`--download` to fetch them automatically the first time:

```sh
cd product/buildDirectory/I.BioBank
python3 /opt/biobank/biobank-thick-client/scripts/biobankZipAddJre.py \
    -p /opt/biobank/jre-cache \
    --download \
    --cert /opt/biobank/biobank-thick-client/docker/nginx-selfsigned.crt
```

For example, from the project root:

```sh
cd product/buildDirectory/I.BioBank
sudo python3 ../../../scripts/biobankZipAddJre.py \
    -p ~/src/cbsr/biobank-sw/biobank-jre-cache \
    --cert ../../../docker/nginx-selfsigned.crt
```

The `--cert` flag imports the server's self-signed certificate into the bundled
JRE's truststore at build time. Clients using these ZIPs do not need to import
the certificate manually.

Output ZIPs land alongside the originals with a `_with_jre` suffix:

```
BioBank-3.12.1-linux.gtk.x86_64_with_jre.zip
BioBank-3.12.1-win32.win32.x86_with_jre.zip
```

Distribute these ZIPs to end users.

## Trusting the server certificate on the client machine

This step is only needed for clients using a ZIP that does **not** have a JRE
bundled (i.e. built without the `--cert` flag above), or for clients who
installed the thick client outside of the bundled ZIPs.

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

## Container contents

- Base image: `azul/zulu-openjdk:7` (Java 1.7 required)
- Apache Ant 1.9.0
- Eclipse 3.7.2 Indigo SDK + delta pack (installed at `/opt/eclipse`)
- Project mounted at `/opt/biobank`

Output ZIPs land in `product/buildDirectory/I.BioBank/`:

```
BioBank-3.12.1-linux.gtk.x86_64.zip
BioBank-3.12.1-linux.gtk.x86.zip
BioBank-3.12.1-macosx.cocoa.x86_64.zip
BioBank-3.12.1-macosx.cocoa.x86.zip
BioBank-3.12.1-win32.win32.x86.zip
```

## Key Build Properties

Set in `build.xml` `product` target:

| Property | Value |
|---|---|
| `buildDirectory` | `${product.dir}/buildDirectory` |
| `buildTempFolder` | `${product.dir}/buildDirectory` |
| `baseLocation` | `${env.ECLIPSE_HOME}` (i.e. `/opt/eclipse`) |

**`buildTempFolder` is critical.** Without it, the generated plugin `build.xml` files set `build.result.folder` to a path inside the plugin's source directory, causing sibling-plugin classpath entries (`${build.result.folder}/../biobank.gui.common_1.0.0/@dot`) to resolve incorrectly.

With `buildTempFolder` set, compiled plugin classes land at:
```
${buildDirectory}/plugins/<pluginId>_<version>/@dot
```

Set in `product/build.properties`:

| Property | Value | Reason |
|---|---|---|
| `p2.gathering` | `false` | Disables p2 gather/assemble path; standard assembler produces platform ZIPs directly |
| `skipDirector` | `true` | Skips `runDirector` p2 install step, which fails due to certificate rejection in the container JRE truststore |

**Why `skipDirector`:** After compilation, the package phase calls `runDirector` to install the product from the local p2 build repo into a fresh Eclipse base. The Zulu JDK 7 container truststore does not trust the Eclipse Foundation code-signing certificate on the gathered JARs. Setting `skipDirector=true` bypasses the `runDirector` target (guarded by `unless="skipDirector"` in the generated package scripts). The assembled product tree from `defaultAssemble` is sufficient for producing distributable ZIPs.

**Why `p2.gathering=false`:** With `p2.gathering=true`, the assembler routes content through the p2 stack and relies on the director to produce the final platform archives — so skipping the director leaves the `I.BioBank/` directory empty. With `p2.gathering=false`, the standard assembler produces the platform ZIPs directly without a director step.

**Update site:** `p2.gathering=false` means no `buildRepo/` is created, so the `updateSiteMetadata` target (which builds an Eclipse update site from `buildRepo/`) is automatically skipped. This is intentional — the update site is a secondary artifact and is not needed for distributable client builds.

## PDE Build Hook — `product/customTargets.xml`

PDE build calls hook targets at defined points in the build lifecycle. We use `postGenerate`, which runs after PDE generates all plugin `build.xml` scripts but before compilation begins.

### scannerConfig Compilation Fix

**Problem:** The generated `scannerConfig/build.xml` uses the JDT compiler adapter (`org.eclipse.jdt.core.JDTCompilerAdapter` / ECJ). ECJ fails to resolve the SWT/JFace type hierarchy needed by `DecodeImageDialog` and related classes, even when:

- SWT JARs are on the classpath
- The `javaCompiler...args` access rules file is empty
- `org.eclipse.swt` is declared in `Require-Bundle`

ECJ reports "The hierarchy of the type X is inconsistent" and "refers to the missing type Shell" for `org.eclipse.swt.widgets.Shell` — despite the JAR being present. This appears to be a quirk of how ECJ resolves type hierarchies across multiple SWT fragment JARs combined with a restricted bootclasspath (`${JavaSE-1.7}` = `rt.jar` only).

Standard `javac` does not have this problem with the same classpath.

**Fix:** In `postGenerate`, patch the generated `scannerConfig/build.xml` with two `<replace>` operations:

1. Change `build.compiler` from `org.eclipse.jdt.core.JDTCompilerAdapter` to `modern` (standard javac)
2. Disable the JDT-specific `<compilerarg>` guards so the args file and ECJ log args are not passed to standard javac

This is done entirely at the build script level — no source changes needed.

### customTargets.xml postGenerate sequence

```
postGenerate
 ├─ empty javaCompiler...args  (defensive; also cleared by patching the adapter)
 ├─ patch scannerConfig/build.xml  (JDTCompilerAdapter → modern)
 └─ clean  (removes stale compiled artifacts before compilation begins)
```

## Launcher Icons

The `biobank2.product` file specifies per-platform launcher icons. The Linux and macOS icons live in `biobank.gui.common/icons/`, not `biobank2/icons/`:

| Platform | File |
|---|---|
| Linux | `eclipse_ws/biobank.gui.common/icons/biobank_launcher.xpm` |
| macOS | `eclipse_ws/biobank.gui.common/icons/biobank_launcher.icns` |
| Windows | `eclipse_ws/biobank.gui.common/icons/biobank_launcher_16.bmp`, `biobank_launcher_32.bmp` |

## Target Platforms

Defined in `product/build.properties` (`configs` property):

- `linux, gtk, x86_64`
- `win32, win32, x86`
- `linux, gtk, x86`
- `macosx, cocoa, x86`
- `macosx, cocoa, x86_64`

## Troubleshooting

### scannerConfig compilation errors

Check `product/buildDirectory/plugins/scannerConfig_0.9.8/@dot.log` for ECJ output.

If you see "hierarchy inconsistent" or "missing type Shell/Widget/Composite":
- Verify `product/customTargets.xml` `postGenerate` target is patching the build.xml
- Confirm `javaCompiler...args` in `product/buildDirectory/plugins/scannerConfig/` is 0 bytes after `postGenerate` runs
- Confirm the generated `build.xml` has `value="modern"` instead of `value="org.eclipse.jdt.core.JDTCompilerAdapter"` after `postGenerate` runs

### build.result.folder resolves into plugin source dir

Symptom: Log shows `build.result.folder` set to the plugin's own directory, and sibling plugin `@dot` paths don't exist.

Cause: `buildTempFolder` not passed to Eclipse PDE launch.

Fix: Ensure `build.xml` passes `-DbuildTempFolder=${product.dir}/buildDirectory` in the `product` target's Eclipse launch args.

### p2 director certificate rejection

Symptom: `[p2.director] One or more certificates rejected. Cannot proceed with installation.`

This occurs if `skipDirector=true` is removed from `product/build.properties`. The Zulu JDK 7 container truststore does not trust the Eclipse Foundation code-signing certificate on the gathered Eclipse platform JARs.

Fix: Ensure `skipDirector=true` is set in `product/build.properties`.

### I.BioBank/ output directory is empty after build

Symptom: Build succeeds but `product/buildDirectory/I.BioBank/` contains only `compilelogs/`.

Cause: `p2.gathering=true` — in this mode the assembler routes through p2 and needs the director to produce platform ZIPs. If the director is skipped (`skipDirector=true`), no ZIPs are written.

Fix: Ensure `p2.gathering=false` in `product/build.properties`.
