#!/usr/bin/env python3
"""PE-037: independent PT-PT/en-GB editorial and age-level catalog linter.

Static checks are not a substitute for signed linguistic/pedagogical review.
Run without arguments for CI; --csv exports a human review worksheet.
"""
import argparse
import csv
import json
import re
import sys
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path

from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]
REPORT_COLUMNS = (
    "id", "category", "ptPT", "enGB", "audioText",
    "minAge", "maxAge", "ageBand", "imageRef", "quizCount",
    "reviewStatus", "reviewer", "reviewDate", "reviewNotes",
)
# Intentional equivalents across categories (e.g. greeting vs time of day)
# are reviewed per category, not falsely treated as duplicates.
def equivalent(text):
    return unicodedata.normalize("NFC", text).casefold().strip().rstrip(" .!?")

def age_band(age):
    return "3-5" if age <= 5 else "6-8"

def lint_catalog(catalog):
    """Return deterministic, actionable diagnostics (code, reference, detail)."""
    errors = []
    def error(code, ref, message):
        errors.append((code, ref, message))
    try:
        validate_catalog(catalog)
    except (ValueError, TypeError, KeyError) as exc:
        error("CATALOG", "catalog", str(exc))
        return errors
    try:
        inspect_category_coverage(catalog)
    except (ValueError, TypeError, KeyError) as exc:
        error("COVERAGE", "catalog", str(exc))
    words = catalog["words"]
    if len(words) < 150:
        error("MINIMUM", "catalog", "Require at least 150 unique content entries")
    lookup = {w["id"]: w for w in words}
    per_pt = defaultdict(list)
    per_en = defaultdict(list)
    for w in words:
        ref = w["id"]
        per_pt[(w["category"], equivalent(w["ptPT"]))].append(ref)
        per_en[(w["category"], equivalent(w["enGB"]))].append(ref)
        if w["audioText"] != w["enGB"]:
            error("TTS", ref, "audioText must match enGB for reviewed pronunciation")
        if unicodedata.normalize("NFC", w["ptPT"]) != w["ptPT"]:
            error("UNICODE", ref, "ptPT must be NFC-normalized")
        if unicodedata.normalize("NFC", w["enGB"]) != w["enGB"]:
            error("UNICODE", ref, "enGB must be NFC-normalized")
        if any(ord(ch) < 32 for ch in w["ptPT"] + w["enGB"]):
            error("CONTROL_CHARACTER", ref, "No control characters in vocabulary")
        if w["ageRange"]["min"] <= 5 and w["ageRange"]["max"] < 5:
            error("AGE", ref, "Early-level vocabulary must support the complete 3-5 band")
    for label, mapping in (("PT_DUPLICATE", per_pt), ("EN_DUPLICATE", per_en)):
        for (_, _), ids in sorted(mapping.items()):
            if len(ids) > 1:
                error(label, ",".join(ids), "Equivalent translations within category")
    for q in catalog["questions"]:
        ref = q["id"]
        options = [lookup[i] for i in q["optionIds"]]
        correct = lookup[q["correctId"]]
        if any(x["category"] != correct["category"] for x in options):
            error("CATEGORY_OPTIONS", ref, "All answers must belong to the correct category")
        if len({equivalent(x["enGB"]) for x in options}) != 3:
            error("AMBIGUOUS_EN", ref, "Two choices have equivalent English labels")
        if len({equivalent(x["ptPT"]) for x in options}) != 3:
            error("AMBIGUOUS_PT", ref, "Two choices have equivalent Portuguese labels")
        if "«" + correct["ptPT"] + "»" not in q["prompt"]:
            error("PROMPT", ref, "PT-PT prompt must show the exact correct label")
        if max(x["ageRange"]["min"] for x in options) > correct["ageRange"]["min"]:
            error("AGE_OPTIONS", ref, "Distractor is not available at correct answer's age")
    return sorted(errors)

def worksheet_rows(catalog):
    question_counts = Counter(q["correctId"] for q in catalog["questions"])
    for word in sorted(catalog["words"], key=lambda w: (w["category"], w["id"])):
        age = word["ageRange"]
        yield {
            "id": word["id"], "category": word["category"],
            "ptPT": word["ptPT"], "enGB": word["enGB"],
            "audioText": word["audioText"], "minAge": age["min"],
            "maxAge": age["max"], "ageBand": age_band(age["min"]),
            "imageRef": word["imageRef"] or "",
            "quizCount": question_counts[word["id"]],
            "reviewStatus": "PENDING", "reviewer": "",
            "reviewDate": "", "reviewNotes": "",
        }

def export_csv(catalog, path):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as out:
        writer = csv.DictWriter(out, fieldnames=REPORT_COLUMNS)
        writer.writeheader()
        writer.writerows(worksheet_rows(catalog))

def validate_review_csv(catalog, path):
    """Check reviewer-provided CSV without ever self-approving its contents."""
    errors = []
    expected = {w["id"] for w in catalog["words"]}
    with Path(path).open("r", encoding="utf-8-sig", newline="") as source:
        reader = csv.DictReader(source)
        if not set(REPORT_COLUMNS).issubset(reader.fieldnames or ()):
            return [("REVIEW_SCHEMA", str(path), "Missing editorial review columns")]
        rows = list(reader)
    seen = set()
    for row in rows:
        ref = row["id"]
        if ref in seen:
            errors.append(("REVIEW_DUPLICATE", ref, "Repeated worksheet ID"))
        seen.add(ref)
        status = row["reviewStatus"].strip().upper()
        if status not in ("PENDING", "APPROVED", "REJECTED", "NEEDS_CHANGES"):
            errors.append(("REVIEW_STATUS", ref, "Invalid reviewStatus"))
        if status == "APPROVED" and not (
            row["reviewer"].strip() and re.fullmatch(r"\d{4}-\d{2}-\d{2}", row["reviewDate"].strip())
        ):
            errors.append(("REVIEW_SIGNOFF", ref, "Approved row requires reviewer and YYYY-MM-DD date"))
    for missing in sorted(expected - seen):
        errors.append(("REVIEW_MISSING", missing, "Word omitted from worksheet"))
    for extra in sorted(seen - expected):
        errors.append(("REVIEW_UNKNOWN", extra, "ID absent from current catalog"))
    return sorted(errors)

def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv", help="Export all words to UTF-8 CSV for editorial review")
    parser.add_argument("--review", help="Check an editorial worksheet supplied by a reviewer")
    args = parser.parse_args(argv)
    catalog = load_catalog()
    errors = lint_catalog(catalog)
    if args.csv:
        export_csv(catalog, args.csv)
        print("Review worksheet exported:", args.csv)
    if args.review:
        errors.extend(validate_review_csv(catalog, args.review))
    for code, ref, detail in sorted(errors):
        print(f"{code}: {ref}: {detail}", file=sys.stderr)
    print(f"PE-037: {len(catalog['words'])} entries; {len(catalog['questions'])} questions; {len(errors)} diagnostics")
    return 1 if errors else 0

if __name__ == "__main__":
    sys.exit(main())
