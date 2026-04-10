#!/usr/bin/env python3
"""
biobankZipAddJre.py - Adds a JRE folder to Biobank installation ZIP files.

Scans the current directory for Biobank ZIP files and creates a copy of each
with the appropriate JRE embedded under BioBank/jre/ (or biobank-cli/jre/).

JRE ZIPs are downloaded on demand from the Azul Zulu CDN (Java 7 builds) unless
they are already present in the JRE directory.

If a server SSL certificate is provided, it is imported into the bundled JRE's
truststore so the thick client can connect to a server using a self-signed cert.

Usage:
    biobankZipAddJre.py [OPTIONS]

Options:
    -p DIR, --path DIR      Directory for JRE ZIP files (default: current directory).
    --download              Download missing JRE ZIPs from Azul before processing.
    --cert FILE             Server SSL certificate (PEM or DER) to trust in the bundled JRE.
"""

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.request
import zipfile
from pathlib import Path

WIN32_JRE_ZIP = "jre.win32.x86.zip"
LINUX64_JRE_ZIP = "jre.linux.x86_64.zip"

AZUL_API = "https://api.azul.com/metadata/v1/zulu/packages/"
AZUL_JAVA_VERSION = 7

# Maps (os, arch) query params to local filename
JRE_DOWNLOADS = {
    WIN32_JRE_ZIP:   {"os": "windows", "arch": "x86"},
    LINUX64_JRE_ZIP: {"os": "linux",   "arch": "x64"},
}


def azul_download_url(os_name: str, arch: str) -> str:
    """Query the Azul metadata API and return the download URL for the latest Java 7 JRE ZIP."""
    params = (
        f"java_version={AZUL_JAVA_VERSION}"
        f"&os={os_name}"
        f"&arch={arch}"
        f"&package_type=jre"
        f"&archive_type=zip"
        f"&latest=true"
        f"&page_size=1"
    )
    url = f"{AZUL_API}?{params}"
    try:
        with urllib.request.urlopen(url) as response:
            packages = json.loads(response.read())
    except Exception as e:
        sys.exit(f"ERROR: Failed to query Azul API ({url}): {e}")

    if not packages:
        sys.exit(f"ERROR: No Azul Java {AZUL_JAVA_VERSION} JRE ZIP found for os={os_name} arch={arch}")

    return packages[0]["download_url"]


def download_jre(dest: Path, os_name: str, arch: str) -> None:
    """Download the latest Azul Java 7 JRE ZIP to dest."""
    url = azul_download_url(os_name, arch)
    print(f"downloading {url}")
    try:
        with urllib.request.urlopen(url) as response, open(dest, "wb") as out:
            shutil.copyfileobj(response, out)
    except Exception as e:
        sys.exit(f"ERROR: Download failed: {e}")
    print(f"saved {dest}")


def ensure_jre_zips(jre_path: Path) -> None:
    """Download any missing JRE ZIPs into jre_path."""
    jre_path.mkdir(parents=True, exist_ok=True)
    for filename, params in JRE_DOWNLOADS.items():
        dest = jre_path / filename
        if dest.exists():
            print(f"already have {dest}, skipping download")
        else:
            download_jre(dest, params["os"], params["arch"])


def _is_symlink(infodict: dict, path: str) -> bool:
    """Return True if the ZIP entry at path is a symbolic link."""
    info = infodict.get(path)
    if info is None:
        return False
    unix_mode = (info.external_attr >> 16) & 0xFFFF
    return (unix_mode & 0xF000) == 0xA000


def find_jre_prefix(zf: zipfile.ZipFile) -> str:
    """
    Return the ZIP path prefix for the JRE root (with trailing slash, or "" for bare root).

    Handles three layouts:
    - Bare root: bin/java at the top level (unusual, prefix = "")
    - JRE zip:   single top-level dir with bin/java inside
    - JDK zip:   single top-level dir with jre/bin/java inside
                 (older Azul Linux builds ship a JDK even when JRE is requested)

    In some old Azul JDK ZIPs, jre/bin and jre/lib are symlinks pointing back to
    ../bin and ../lib. In that case jre/bin/java exists only as a symlink descendant;
    the real executables are under the top-level bin/. We detect this by checking
    whether the jre/bin entry itself is a symlink and fall back to the JDK root.
    """
    names = set(zf.namelist())
    infodict = {info.filename: info for info in zf.infolist()}

    if ("bin/java" in names or "bin/java.exe" in names) and not _is_symlink(infodict, "bin"):
        return ""

    top_dirs = {n.split("/")[0] + "/" for n in names if "/" in n}
    if len(top_dirs) != 1:
        sys.exit(
            f"ERROR: Cannot determine JRE root. Top-level entries: {sorted(top_dirs)}"
        )
    top = top_dirs.pop()

    # JDK layout: check for embedded JRE under top/jre/, but only if jre/bin is not a symlink.
    # Some old Azul JDK ZIPs have jre/bin -> ../bin (symlink); in that case the real
    # executables are directly under top/bin/ and we must use the JDK root as the prefix.
    jre_sub = top + "jre/"
    if (jre_sub + "bin/java") in names or (jre_sub + "bin/java.exe") in names:
        if not _is_symlink(infodict, top + "jre/bin"):
            return jre_sub
        # jre/bin is a symlink — real content is under the JDK root, fall through

    # JRE layout (or JDK with symlinked jre/): bin/java is directly under the top-level dir
    if (top + "bin/java") in names or (top + "bin/java.exe") in names:
        return top

    sys.exit(f"ERROR: Cannot find bin/java in ZIP — is this a JRE or JDK archive?")


def add_jre(biobank_zip: Path, output_zip: Path, jre_zip: Path, jre_prefix: str) -> None:
    """
    Copy biobank_zip to output_zip and append jre/ contents from jre_zip.

    Entries are copied directly between ZIPs (no extraction to disk) so that
    Unix file permissions (ZipInfo.external_attr) are preserved. This is
    required for bin/java and other JRE executables to remain executable after
    the output ZIP is unpacked.
    """
    if not jre_zip.exists():
        sys.exit(f"ERROR: Could not find JRE zip file: {jre_zip}")

    shutil.copy2(biobank_zip, output_zip)

    print(f"appending JRE from {jre_zip} into {output_zip}")
    with zipfile.ZipFile(jre_zip) as src_zf:
        prefix = find_jre_prefix(src_zf)
        # First pass: collect the relative paths of symlink entries.
        # Azul JDK ZIPs contain symlinks (e.g. jre -> ., sample -> ..., man -> ...)
        # plus actual file entries under those same paths. Skipping only the symlink
        # entry itself still leaves the descendant entries, which cause unzip to fail
        # with "exists but is not directory". We must skip both.
        symlink_prefixes: set[str] = set()
        for info in src_zf.infolist():
            unix_mode = (info.external_attr >> 16) & 0xFFFF
            if (unix_mode & 0xF000) == 0xA000:  # S_IFLNK
                rel = info.filename
                if prefix and rel.startswith(prefix):
                    rel = rel[len(prefix):]
                rel = rel.rstrip("/")
                if rel:
                    symlink_prefixes.add(rel + "/")

        with zipfile.ZipFile(output_zip, "a", compression=zipfile.ZIP_DEFLATED) as out_zf:
            for info in src_zf.infolist():
                unix_mode = (info.external_attr >> 16) & 0xFFFF
                if (unix_mode & 0xF000) == 0xA000:  # S_IFLNK — skip symlinks
                    continue

                rel = info.filename
                if prefix:
                    if not rel.startswith(prefix):
                        continue
                    rel = rel[len(prefix):]
                if not rel:
                    continue

                # Skip entries that live under a symlinked directory
                if any(rel.startswith(sp) for sp in symlink_prefixes):
                    continue

                # Path() strips trailing slashes, which turns directory entries into
                # file entries. Preserve the trailing slash so unzip treats them as dirs.
                arc_name = str(Path(jre_prefix) / "jre" / rel.rstrip("/"))
                if rel.endswith("/"):
                    arc_name += "/"
                new_info = zipfile.ZipInfo(arc_name)
                new_info.compress_type = info.compress_type
                new_info.external_attr = info.external_attr  # preserves Unix permissions
                new_info.date_time = info.date_time

                out_zf.writestr(new_info, src_zf.read(info.filename))


CACERTS_SUFFIX = "lib/security/cacerts"
KEYTOOL_STOREPASS = "changeit"


def import_cert_into_zip(output_zip: Path, jre_prefix: str, cert_file: Path) -> None:
    """
    Import a server SSL certificate into the bundled JRE's truststore inside output_zip.

    Extracts cacerts from the ZIP, runs keytool to import the certificate, then
    rewrites the ZIP with the updated cacerts (replacing the original entry).
    Requires keytool on PATH.
    """
    if not shutil.which("keytool"):
        print("WARNING: keytool not found on PATH — skipping certificate import")
        return

    cacerts_entry = str(Path(jre_prefix) / "jre" / CACERTS_SUFFIX).replace("\\", "/")

    with zipfile.ZipFile(output_zip) as zf:
        if cacerts_entry not in zf.namelist():
            print(f"WARNING: {cacerts_entry} not found in ZIP — skipping certificate import")
            return
        cacerts_info = zf.getinfo(cacerts_entry)
        cacerts_data = zf.read(cacerts_entry)

    with tempfile.TemporaryDirectory() as tmpdir:
        cacerts_path = Path(tmpdir) / "cacerts"
        cacerts_path.write_bytes(cacerts_data)

        result = subprocess.run(
            [
                "keytool", "-import",
                "-trustcacerts",
                "-alias", "biobank-server",
                "-keystore", str(cacerts_path),
                "-file", str(cert_file),
                "-storepass", KEYTOOL_STOREPASS,
                "-noprompt",
            ],
            capture_output=True,
            text=True,
        )
        if result.returncode != 0:
            if "already exists" in result.stdout or "already exists" in result.stderr:
                print("certificate already in truststore, skipping")
                return
            sys.exit(
                f"ERROR: keytool import failed:\n{result.stdout}\n{result.stderr}"
            )

        updated_cacerts = cacerts_path.read_bytes()

    # Rewrite the ZIP replacing the old cacerts entry — appending creates duplicates.
    tmp_zip = output_zip.with_suffix(".tmp.zip")
    try:
        with zipfile.ZipFile(output_zip) as src_zf, \
             zipfile.ZipFile(tmp_zip, "w") as dst_zf:
            for info in src_zf.infolist():
                if info.filename == cacerts_entry:
                    new_info = zipfile.ZipInfo(cacerts_entry)
                    new_info.compress_type = cacerts_info.compress_type
                    new_info.external_attr = cacerts_info.external_attr
                    new_info.date_time = cacerts_info.date_time
                    dst_zf.writestr(new_info, updated_cacerts)
                else:
                    dst_zf.writestr(info, src_zf.read(info.filename))
        tmp_zip.replace(output_zip)
    except Exception:
        tmp_zip.unlink(missing_ok=True)
        raise

    print(f"imported {cert_file} into bundled JRE truststore")


def add_win32_jre(zip_file: Path, jre_path: Path, cert_file: Path | None) -> None:
    output = zip_file.with_name(zip_file.stem + "_with_jre.zip")
    add_jre(zip_file, output, jre_path / WIN32_JRE_ZIP, "BioBank")
    if cert_file:
        import_cert_into_zip(output, "BioBank", cert_file)


def add_linux64_jre(zip_file: Path, jre_path: Path, cert_file: Path | None) -> None:
    output = zip_file.with_name(zip_file.stem + "_with_jre.zip")
    add_jre(zip_file, output, jre_path / LINUX64_JRE_ZIP, "BioBank")
    if cert_file:
        import_cert_into_zip(output, "BioBank", cert_file)


def add_jre_to_linux_cli(zip_file: Path, jre_path: Path, cert_file: Path | None) -> None:
    output = zip_file.with_name(zip_file.stem + "_linux_with_jre.zip")
    add_jre(zip_file, output, jre_path / LINUX64_JRE_ZIP, "biobank-cli")
    if cert_file:
        import_cert_into_zip(output, "biobank-cli", cert_file)


def find_biobank_files(directory: Path) -> dict:
    files = {}
    for f in directory.iterdir():
        if not f.is_file():
            continue
        if re.search(r"^BioBank.*win32\.x86\.zip$", f.name):
            files["win32"] = f
        elif re.search(r"^BioBank.*linux\.gtk\.x86_64\.zip$", f.name):
            files["linux64"] = f
        elif re.search(r"^BiobankCli.*\.zip$", f.name) and "with_jre" not in f.name:
            files["cli"] = f
    return files


def main():
    parser = argparse.ArgumentParser(
        description="Add a JRE to Biobank installation ZIP files."
    )
    parser.add_argument(
        "-p", "--path",
        default=".",
        metavar="DIR",
        help="Directory for JRE ZIP files (default: current directory)",
    )
    parser.add_argument(
        "--download",
        action="store_true",
        help="Download missing JRE ZIPs from Azul before processing",
    )
    parser.add_argument(
        "--cert",
        metavar="FILE",
        help="Server SSL certificate (PEM or DER) to import into the bundled JRE truststore",
    )
    args = parser.parse_args()

    jre_path = Path(args.path)
    cert_file = Path(args.cert) if args.cert else None

    if cert_file and not cert_file.exists():
        sys.exit(f"ERROR: Certificate file not found: {cert_file}")

    if args.download:
        ensure_jre_zips(jre_path)

    files = find_biobank_files(Path("."))

    if not files:
        sys.exit("ERROR: No Biobank ZIP files found in the current directory.")

    if "win32" in files:
        add_win32_jre(files["win32"], jre_path, cert_file)

    if "linux64" in files:
        add_linux64_jre(files["linux64"], jre_path, cert_file)

    if "cli" in files:
        add_jre_to_linux_cli(files["cli"], jre_path, cert_file)


if __name__ == "__main__":
    main()
