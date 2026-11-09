"""Regression tests for the PE-037 editorial content lint and reviewer worksheet."""
import copy
import csv
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from lint_editorial_catalog import (
    lint_catalog, export_csv, validate_review_csv, worksheet_rows,
    equivalent, age_band,
)
from validate_offline_catalog import load_catalog


class EditorialLintTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.catalog = load_catalog()

    def test_base_catalog_is_structurally_clean(self):
        self.assertEqual((296, 272), (len(self.catalog["words"]), len(self.catalog["questions"])))
        self.assertEqual([], lint_catalog(self.catalog))

    def test_same_translation_ambiguity_is_rejected(self):
        data = copy.deepcopy(self.catalog)
        q = next(q for q in data["questions"] if q["id"] == "quiz.animals.cat")
        ids = q["optionIds"]
        words = {w["id"]: w for w in data["words"]}
        words[ids[0]]["enGB"] = words[ids[1]]["enGB"]
        words[ids[0]]["audioText"] = words[ids[0]]["enGB"]
        self.assertTrue(any(code in ("AMBIGUOUS_EN", "EN_DUPLICATE")
                            for code, _, _ in lint_catalog(data)))

    def test_wrong_age_distractor_is_rejected(self):
        data = copy.deepcopy(self.catalog)
        q = next(q for q in data["questions"] if q["id"] == "quiz.animals.cat")
        words = {w["id"]: w for w in data["words"]}
        other_id = next(i for i in q["optionIds"] if i != q["correctId"])
        words[other_id]["ageRange"]["min"] = 6
        self.assertTrue(any(code == "AGE_OPTIONS" for code, _, _ in lint_catalog(data)))

    def test_missing_images_are_rejected_by_existing_catalog_validator(self):
        data = copy.deepcopy(self.catalog)
        data["words"][0]["imageRef"] = "drawable/pe037_missing_vector"
        self.assertTrue(any(code == "CATALOG" for code, _, _ in lint_catalog(data)))

    def test_tts_mismatch_is_rejected(self):
        data = copy.deepcopy(self.catalog)
        data["words"][0]["audioText"] = "Not the English term"
        self.assertTrue(any(code == "TTS" for code, _, _ in lint_catalog(data)))

    def test_accent_sensitive_comparison_preserves_grandparents(self):
        self.assertNotEqual(equivalent("Avó"), equivalent("Avô"))
        self.assertEqual("6-8", age_band(6))
        self.assertEqual("3-5", age_band(5))

    def test_csv_has_all_entries_pending_not_self_signed(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "review.csv"
            export_csv(self.catalog, path)
            with path.open(encoding="utf-8-sig", newline="") as f:
                rows = list(csv.DictReader(f))
            self.assertEqual(296, len(rows))
            self.assertEqual({"PENDING"}, {r["reviewStatus"] for r in rows})
            self.assertTrue(all(not r["reviewer"] for r in rows))
            self.assertTrue(all(not r["reviewDate"] for r in rows))
            self.assertEqual([], validate_review_csv(self.catalog, path))
            rows[0]["reviewStatus"] = "APPROVED"
            with path.open("w", encoding="utf-8-sig", newline="") as f:
                w = csv.DictWriter(f, fieldnames=rows[0].keys())
                w.writeheader()
                w.writerows(rows)
            self.assertTrue(any(code == "REVIEW_SIGNOFF"
                                for code, _, _ in validate_review_csv(self.catalog, path)))

    def test_worksheet_has_accurate_question_counts(self):
        rows = {x["id"]: x for x in worksheet_rows(self.catalog)}
        self.assertEqual(1, rows["phrases.hello"]["quizCount"])
        self.assertEqual(0, rows["phrases.good_night"]["quizCount"])


if __name__ == "__main__":
    unittest.main()
