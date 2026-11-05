#!/usr/bin/env python3
"""PE-010: calculate the next tagged APK Release version and Android versionCode.

Git tags represent successfully published releases; build attempts do not
increment versions. The first release is v0.1.0. Later releases increment
the patch number, with bounded carry to minor/major at 999.
No network, secrets or third-party libraries are required.
"""
from __future__ import annotations

import argparse
import os
import re
import subprocess
from typing import Iterable

TAG_PATTERN = re.compile(r"v(0|[1-9][0-9]*)[.](0|[1-9][0-9]*)[.](0|[1-9][0-9]*)\Z")
MAX_VERSION_CODE = 2_100_000_000
MAX_PART = 999


def parse_tag(tag: str) -> tuple[int, int, int] | None:
    match = TAG_PATTERN.fullmatch(tag.strip())
    if match is None:
        return None
    version = tuple(int(part) for part in match.groups())
    if version[1] > MAX_PART or version[2] > MAX_PART:
        raise ValueError("A tag Release excede os limites de versionCode do Android.")
    if version_code(version) > MAX_VERSION_CODE:
        raise ValueError("A tag Release excede o máximo versionCode suportado.")
    return version


def version_code(version: tuple[int, int, int]) -> int:
    major, minor, patch = version
    if min(version) < 0 or minor > MAX_PART or patch > MAX_PART:
        raise ValueError("Versão fora dos limites suportados.")
    code = major * 1_000_000 + minor * 1_000 + patch
    if not 1 <= code <= MAX_VERSION_CODE:
        raise ValueError("versionCode deve estar entre 1 e 2100000000.")
    return code


def next_version(tags: Iterable[str]) -> tuple[int, int, int]:
    versions = [parsed for tag in tags if (parsed := parse_tag(tag)) is not None]
    if not versions:
        return 0, 1, 0
    major, minor, patch = max(versions)
    patch += 1
    if patch > MAX_PART:
        patch = 0
        minor += 1
    if minor > MAX_PART:
        minor = 0
        major += 1
    result = major, minor, patch
    version_code(result)  # Fail before build if versionCode would overflow.
    return result


def list_local_tags() -> list[str]:
    result = subprocess.run(
        ["git", "tag", "--list", "v*"], check=True, capture_output=True,
        text=True, encoding="utf-8"
    )
    return result.stdout.splitlines()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--github-output", metavar="PATH",
        help="Optional GITHUB_OUTPUT destination for GitHub Actions."
    )
    args = parser.parse_args()
    version = next_version(list_local_tags())
    name = ".".join(map(str, version))
    code = version_code(version)
    tag = "v" + name
    pairs = {
        "version_name": name,
        "version_code": str(code),
        "release_tag": tag
    }
    if args.github_output:
        with open(args.github_output, "a", encoding="utf-8") as out:
            for key, value in pairs.items():
                out.write(f"{key}={value}\n")
    print(f"Próxima Release: {tag} (versionCode={code})")


if __name__ == "__main__":
    main()
