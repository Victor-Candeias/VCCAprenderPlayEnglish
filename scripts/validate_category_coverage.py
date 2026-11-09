#!/usr/bin/env python3
"""PE-013: editorial coverage checks for all bundled vocabulary categories."""
from collections import Counter
from validate_offline_catalog import load_catalog, validate_catalog

CATEGORY_IDS = ("animals", "colors", "numbers", "phrases", "food", "objects", "clothing", "transport", "family", "body", "nature", "weather", "calendar", "time")


def inspect_category_coverage(catalog):
    validate_catalog(catalog)
    counts = Counter(word["category"] for word in catalog["words"])
    lookup = {word["id"]: word for word in catalog["words"]}
    correct_per_category = {category: set() for category in CATEGORY_IDS}
    questions_by_category = Counter()
    for question in catalog["questions"]:
        correct = lookup[question["correctId"]]
        category = correct["category"]
        if category not in correct_per_category:
            raise ValueError("Unexpected question category")
        if not all(lookup[option]["category"] == category for option in question["optionIds"]):
            raise ValueError("Question mixes unrelated vocabulary categories")
        if f"«{correct['ptPT']}»" not in question["prompt"]:
            raise ValueError("Prompt does not match the correct PT-PT word")
        if question["correctId"] in correct_per_category[category]:
            raise ValueError("Repeated correct word within one category")
        correct_per_category[category].add(question["correctId"])
        questions_by_category[category] += 1
    for category in CATEGORY_IDS:
        if counts[category] < 12:
            raise ValueError(f"Insufficient words in category: {category}")
        if questions_by_category[category] < 8:
            raise ValueError(f"Insufficient questions in category: {category}")
        translations = [
            word["enGB"].casefold() for word in catalog["words"]
            if word["category"] == category
        ]
        if len(translations) != len(set(translations)):
            raise ValueError(f"Duplicated English vocabulary in category: {category}")
    return counts, questions_by_category


if __name__ == "__main__":
    wc, qc = inspect_category_coverage(load_catalog())
    for category in CATEGORY_IDS:
        print(f"{category}: {wc[category]} palavras; {qc[category]} perguntas")
