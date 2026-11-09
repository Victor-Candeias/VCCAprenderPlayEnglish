#!/usr/bin/env python3
"""PE-012: validate packaged PT-PT/en-GB vocabulary and quiz references offline."""
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

CATALOG_PATH = (
    Path(__file__).resolve().parents[1]
    / "app/src/main/assets/content/quiz-v1.json"
)
ID_PATTERN = re.compile(r"[a-z0-9]+(?:[._-][a-z0-9]+)*\Z")
CATEGORIES = {"animals", "colors", "numbers", "food", "objects", "phrases", "clothing", "transport", "family", "body", "nature", "weather", "calendar", "time", "emotions", "actions"}


def check_id(value):
    if not isinstance(value, str) or not 1 <= len(value) <= 80:
        raise ValueError("Invalid stable ID length/type")
    if ID_PATTERN.fullmatch(value) is None:
        raise ValueError("Invalid stable ID format")


def text(value):
    if not isinstance(value, str) or not value.strip() or value != value.strip():
        raise ValueError("Required catalog text must be a nonempty string")


def validate_catalog(data):
    if not isinstance(data, dict) or type(data.get("schemaVersion")) is not int or data["schemaVersion"] != 1:
        raise ValueError("Unsupported schema version")
    if data.get("locales") != {"source": "pt-PT", "target": "en-GB"}:
        raise ValueError("Invalid locale pair")

    words = data.get("words")
    questions = data.get("questions")
    if not isinstance(words, list) or not isinstance(questions, list):
        raise ValueError("words and questions must be arrays")
    if len(words) > 10000 or len(questions) > 10000:
        raise ValueError("Too many entries")
    vocabulary = {}
    for entry in words:
        if not isinstance(entry, dict):
            raise ValueError("Invalid word object")
        wid = entry.get("id")
        check_id(wid)
        if wid in vocabulary:
            raise ValueError("Duplicate word ID")
        for field in ("ptPT", "enGB", "audioText"):
            text(entry.get(field))
        if entry.get("category") not in CATEGORIES:
            raise ValueError("Unknown category")
        subcategory = entry.get("subcategory")
        if entry["category"] == "objects":
            if subcategory not in ("home", "school"):
                raise ValueError("Object subcategory must be home or school")
        elif subcategory is not None:
            raise ValueError("Only object entries may have a subcategory")
        ages = entry.get("ageRange")
        if not isinstance(ages, dict):
            raise ValueError("Age range is missing")
        low, high = ages.get("min"), ages.get("max")
        if type(low) is not int or type(high) is not int or not 3 <= low <= high <= 8:
            raise ValueError("Age range must be 3..8")
        image = entry.get("imageRef")
        if image is not None:
            if not isinstance(image, str) or ".." in image:
                raise ValueError("Invalid packaged image path")
            if re.fullmatch(r"drawable/[a-z0-9_]+", image):
                target = CATALOG_PATH.parents[2] / "res" / (image + ".xml")
                if not target.is_file():
                    raise ValueError("Missing bundled vector")
                try:
                    vector = ET.parse(target).getroot()
                except ET.ParseError as exc:
                    raise ValueError("Invalid vector XML") from exc
                namespace = "{http://schemas.android.com/apk/res/android}"
                if vector.tag != "vector" or not vector.findall("path") or not all(
                    path.get(namespace + "pathData") for path in vector.findall("path")
                ):
                    raise ValueError("Invalid bundled vector content")
            elif re.fullmatch(r"images/[a-z0-9_./-]+[.](?:png|webp|jpg)", image):
                if not (CATALOG_PATH.parents[1] / image).is_file():
                    raise ValueError("Missing packaged image")
            else:
                raise ValueError("Invalid packaged image path")
        vocabulary[wid] = entry

    seen = set()
    for question in questions:
        if not isinstance(question, dict):
            raise ValueError("Invalid question object")
        qid = question.get("id")
        check_id(qid)
        if qid in seen:
            raise ValueError("Duplicate question ID")
        seen.add(qid)
        text(question.get("prompt"))
        ids = question.get("optionIds")
        if not isinstance(ids, list) or len(ids) != 3:
            raise ValueError("A question requires exactly three options")
        for wid in ids:
            check_id(wid)
        if len(set(ids)) != 3:
            raise ValueError("Duplicate options")
        if question.get("correctId") not in ids:
            raise ValueError("Correct answer is not an option")
        try:
            referenced = [vocabulary[wid] for wid in ids]
        except KeyError as exc:
            raise ValueError("Unknown word referenced by quiz") from exc
        if max(e["ageRange"]["min"] for e in referenced) > min(
            e["ageRange"]["max"] for e in referenced
        ):
            raise ValueError("Question options must share a supported age")

    return len(vocabulary), len(questions)


def load_catalog():
    with CATALOG_PATH.open("r", encoding="utf-8") as fh:
        return json.load(fh)


if __name__ == "__main__":
    count = validate_catalog(load_catalog())
    print(f"Catalog validado offline: {count[0]} palavras, {count[1]} perguntas.")
