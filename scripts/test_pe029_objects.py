"""PE-029: home/school objects with offline illustrations and en-GB questions."""
import copy
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]

class ObjectVocabularyTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()
        self.objects = [w for w in self.data["words"] if w["category"] == "objects"]
        self.words = {w["id"]: w for w in self.data["words"]}
        self.questions = [q for q in self.data["questions"] if q["id"].startswith("quiz.objects.")]

    def test_catalog_and_subcategories(self):
        self.assertEqual((163, 139), validate_catalog(self.data))
        wc, qc = inspect_category_coverage(self.data)
        self.assertEqual((24, 20), (wc["objects"], qc["objects"]))
        self.assertEqual({"home", "school"}, {w["subcategory"] for w in self.objects})
        self.assertGreaterEqual(sum(w["subcategory"] == "home" for w in self.objects), 10)
        self.assertGreaterEqual(sum(w["subcategory"] == "school" for w in self.objects), 8)

    def test_offline_icons_translations_and_tts(self):
        self.assertEqual(24, len(self.objects))
        self.assertEqual(24, len({w["imageRef"] for w in self.objects}))
        self.assertEqual(24, len({w["enGB"].casefold() for w in self.objects}))
        for word in self.objects:
            self.assertEqual(word["enGB"], word["audioText"])
            self.assertTrue(word["imageRef"].startswith("drawable/pe_object_"))
            self.assertTrue((ROOT / "app/src/main/res" / (word["imageRef"] + ".xml")).is_file())
        for key, pt, en in (
            ("door", "Porta", "Door"), ("window", "Janela", "Window"),
            ("rubber", "Borracha", "Rubber"), ("school_bag", "Mochila escolar", "School bag"),
            ("fridge", "Frigorífico", "Fridge")
        ):
            w = self.words["objects." + key]
            self.assertEqual((pt, en), (w["ptPT"], w["enGB"]))

    def test_quiz_options_and_subcategory_safety(self):
        self.assertEqual(20, len(self.questions))
        for q in self.questions:
            self.assertEqual(3, len(q["optionIds"]))
            self.assertEqual(3, len(set(q["optionIds"])))
            self.assertIn(q["correctId"], q["optionIds"])
            self.assertTrue(all(self.words[o]["category"] == "objects" for o in q["optionIds"]))
            self.assertEqual(3, len({self.words[o]["enGB"].casefold() for o in q["optionIds"]}))
            self.assertIn("«" + self.words[q["correctId"]]["ptPT"] + "»", q["prompt"])
        invalid = copy.deepcopy(self.data)
        next(w for w in invalid["words"] if w["id"] == "objects.door")["subcategory"] = "transport"
        with self.assertRaises(ValueError):
            validate_catalog(invalid)

if __name__ == "__main__":
    unittest.main()
