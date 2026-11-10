#!/usr/bin/env python3
"""PE-039 offline-only safeguards for quiz assets, Android permissions and TTS."""
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from validate_offline_catalog import validate_catalog, load_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
ASSETS = ROOT / "app/src/main/assets/content"
OUTPUT = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/audio/AndroidEnglishPronunciationOutput.kt"
POLICY = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/audio/EnglishVoicePolicy.kt"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
FORBIDDEN_PERMISSIONS = {
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.RECORD_AUDIO",
}
REMOTE_PATTERN = re.compile(r"\b(?:https?://|wss?://)", re.IGNORECASE)

def audit(manifest_path=MANIFEST, assets_dir=ASSETS, output_path=OUTPUT, policy_path=POLICY):
    problems = []
    tree = ET.parse(manifest_path).getroot()
    permissions = {
        node.attrib.get(ANDROID_NS + "name")
        for node in tree.findall("uses-permission")
    }
    for permission in sorted(FORBIDDEN_PERMISSIONS & permissions):
        problems.append("Disallowed permission: " + permission)
    catalog = load_catalog()
    try:
        validate_catalog(catalog)
        inspect_category_coverage(catalog)
    except (ValueError, TypeError, KeyError) as error:
        problems.append("Offline catalog validation: " + str(error))
    if not list(assets_dir.glob("*.json")):
        problems.append("Packaged offline catalog absent")
    for file in sorted(assets_dir.glob("*.json")):
        try:
            payload = json.loads(file.read_text(encoding="utf-8"))
        except (ValueError, UnicodeError) as error:
            problems.append(f"Invalid bundled asset {file.name}: {error}")
            continue
        if REMOTE_PATTERN.search(json.dumps(payload, ensure_ascii=False)):
            problems.append("Remote content URL in " + file.name)
    output = output_path.read_text(encoding="utf-8")
    policy = policy_path.read_text(encoding="utf-8")
    for name, fragment in {
        "en-GB locale": "Locale.UK",
        "offline voice requirement": "isNetworkConnectionRequired",
        "installed voice requirement": "KEY_FEATURE_NOT_INSTALLED",
        "explicit offline voice selection": "setVoice(chosen)",
        "voice failure fallback": "EnglishTtsStatus.VOICE_MISSING",
        "single-playback safety": "turnGate.tryBegin(id)",
    }.items():
        if fragment not in output:
            problems.append("Missing TTS safeguard: " + name)
    if "usableInstalledVoice" not in policy:
        problems.append("Offline voice policy missing")
    return problems

if __name__ == "__main__":
    errors = audit()
    for error in errors:
        print("PE-039:", error)
    if errors:
        raise SystemExit(1)
    print("PE-039 offline audit: valid catalog, packaged content, no app Internet permission, strict installed en-GB voice.")
