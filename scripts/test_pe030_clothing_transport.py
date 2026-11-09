"""PE-030: illustrated en-GB clothing and transport with distinct PT-PT questions."""
import copy
import re
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]

class ClothingTransportTests(unittest.TestCase):
    def setUp(self):
        self.catalog = load_catalog()
        self.lookup = {word["id"]: word for word in self.catalog["words"]}

    def test_catalog_counts_and_two_new_categories(self):
        self.assertEqual((187, 163), validate_catalog(self.catalog))
        words, questions = inspect_category_coverage(self.catalog)
        self.assertEqual((12, 12), (words["clothing"], words["transport"]))
        self.assertEqual((12, 12), (questions["clothing"], questions["transport"]))

    def test_en_gb_vocabulary_tts_and_distinct_offline_vectors(self):
        for category in ("clothing", "transport"):
            entries = [w for w in self.catalog["words"] if w["category"] == category]
            self.assertEqual(12, len(entries))
            self.assertEqual(12, len({w["enGB"].casefold() for w in entries}))
            self.assertEqual(12, len({w["imageRef"] for w in entries}))
            for word in entries:
                self.assertEqual(word["enGB"], word["audioText"])
                self.assertEqual({"min": 3, "max": 8}, word["ageRange"])
                self.assertRegex(word["imageRef"], r"^drawable/pe_" + category + r"_[a-z_]+$")
                self.assertTrue((ROOT / "app/src/main/res" / (word["imageRef"] + ".xml")).is_file())
        self.assertEqual(("Calças", "Trousers"), (self.lookup["clothing.trousers"]["ptPT"], self.lookup["clothing.trousers"]["enGB"]))
        self.assertEqual(("Camisola", "Jumper"), (self.lookup["clothing.jumper"]["ptPT"], self.lookup["clothing.jumper"]["enGB"]))
        self.assertEqual(("Camião", "Lorry"), (self.lookup["transport.lorry"]["ptPT"], self.lookup["transport.lorry"]["enGB"]))
        self.assertEqual(("Autocarro", "Bus"), (self.lookup["transport.bus"]["ptPT"], self.lookup["transport.bus"]["enGB"]))

    def test_unique_quiz_options_pt_pt_prompts_and_category_isolation(self):
        for category in ("clothing", "transport"):
            questions = [q for q in self.catalog["questions"] if q["id"].startswith("quiz." + category + ".")]
            self.assertEqual(12, len(questions))
            self.assertEqual(12, len({q["correctId"] for q in questions}))
            for q in questions:
                self.assertEqual(3, len(q["optionIds"]))
                self.assertEqual(3, len(set(q["optionIds"])))
                self.assertIn(q["correctId"], q["optionIds"])
                self.assertTrue(all(self.lookup[id]["category"] == category for id in q["optionIds"]))
                self.assertEqual(3, len({self.lookup[id]["enGB"].casefold() for id in q["optionIds"]}))
                self.assertIn("«" + self.lookup[q["correctId"]]["ptPT"] + "»", q["prompt"])

    def test_missing_image_and_mixed_categories_fail(self):
        invalid = copy.deepcopy(self.catalog)
        next(w for w in invalid["words"] if w["id"] == "clothing.shirt")["imageRef"] = "drawable/pe_clothing_missing"
        with self.assertRaises(ValueError):
            validate_catalog(invalid)
        invalid = copy.deepcopy(self.catalog)
        next(q for q in invalid["questions"] if q["id"] == "quiz.transport.car")["optionIds"][0] = "clothing.shirt"
        with self.assertRaises(ValueError):
            inspect_category_coverage(invalid)

    def test_kotlin_ui_categories_are_wired(self):
        model = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/domain/model/WordEntry.kt").read_text(encoding="utf-8")
        filters = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/components/LearningFilters.kt").read_text(encoding="utf-8")
        quiz = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt").read_text(encoding="utf-8")
        for cat in ("CLOTHING", "TRANSPORT"):
            self.assertIn(cat + '("', model)
            self.assertIn("WordCategory." + cat + " -> R.string.category_", filters)
            self.assertIn("WordCategory." + cat, quiz)
        self.assertIn("quiz-pe030-illustration", quiz)

if __name__ == "__main__":
    unittest.main()
