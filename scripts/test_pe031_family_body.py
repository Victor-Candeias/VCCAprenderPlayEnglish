"""PE-031: 24 neutral/inclusive family and human body offline vocabulary entries."""
import copy
import unittest
from pathlib import Path
from validate_offline_catalog import validate_catalog, load_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]

class FamilyBodyTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()
        self.words = {w["id"]: w for w in self.data["words"]}

    def test_two_new_categories_and_existing_content(self):
        self.assertEqual((276, 252), validate_catalog(self.data))
        wc, qc = inspect_category_coverage(self.data)
        self.assertEqual((12, 12), (wc["family"], wc["body"]))
        self.assertEqual((12, 12), (qc["family"], qc["body"]))
        self.assertEqual((12, 12), (wc["clothing"], wc["transport"]))

    def test_complete_translations_pronunciation_and_offline_illustrations(self):
        for category in ("family", "body"):
            entries = [w for w in self.data["words"] if w["category"] == category]
            self.assertEqual(12, len(entries))
            self.assertEqual(12, len({w["enGB"].casefold() for w in entries}))
            self.assertEqual(12, len({w["imageRef"] for w in entries}))
            for word in entries:
                self.assertEqual({"min": 3, "max": 8}, word["ageRange"])
                self.assertEqual(word["enGB"], word["audioText"])
                self.assertEqual(f"drawable/pe_{category}_" + word["id"].split(".")[-1], word["imageRef"])
                self.assertTrue((ROOT / "app/src/main/res" / (word["imageRef"] + ".xml")).is_file())
        for key, pair in {
            "family.mother": ("Mãe", "Mother"),
            "family.cousin": ("Primo ou prima", "Cousin"),
            "family.baby": ("Bebé", "Baby"),
            "body.foot": ("Pé", "Foot"),
            "body.finger": ("Dedo da mão", "Finger")
        }.items():
            self.assertEqual(pair, (self.words[key]["ptPT"], self.words[key]["enGB"]))

    def test_question_integrity_and_age_compatibility(self):
        for category in ("family", "body"):
            questions = [q for q in self.data["questions"] if q["id"].startswith("quiz." + category + ".")]
            self.assertEqual(12, len(questions))
            self.assertEqual(12, len({q["correctId"] for q in questions}))
            for q in questions:
                options = q["optionIds"]
                self.assertEqual(3, len(options))
                self.assertEqual(3, len(set(options)))
                self.assertIn(q["correctId"], options)
                self.assertTrue(all(self.words[x]["category"] == category for x in options))
                self.assertEqual(3, len({self.words[x]["enGB"].casefold() for x in options}))
                self.assertIn("«" + self.words[q["correctId"]]["ptPT"] + "»", q["prompt"])

    def test_wrong_or_remote_image_rejected(self):
        for invalid_ref in ("drawable/pe_family_does_not_exist", "https://example.org/face.png"):
            mutated = copy.deepcopy(self.data)
            next(w for w in mutated["words"] if w["id"] == "family.mother")["imageRef"] = invalid_ref
            with self.assertRaises(ValueError):
                validate_catalog(mutated)

    def test_kotlin_categories_and_visuals_connected(self):
        model = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/domain/model/WordEntry.kt").read_text(encoding="utf-8")
        filters = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/components/LearningFilters.kt").read_text(encoding="utf-8")
        quiz = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt").read_text(encoding="utf-8")
        for name in ("FAMILY", "BODY"):
            self.assertIn(name + '("', model)
            self.assertIn("WordCategory." + name + " -> R.string.category_", filters)
            self.assertIn("WordCategory." + name, quiz)
        self.assertIn("quiz-pe031-illustration", quiz)

if __name__ == "__main__":
    unittest.main()
