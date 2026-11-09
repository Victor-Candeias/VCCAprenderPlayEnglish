"""PE-028: food and drinks vocabulary, offline vectors and quiz checks."""
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]

class FoodVocabularyTests(unittest.TestCase):
    def setUp(self):
        self.catalog = load_catalog()
        self.words = {w["id"]: w for w in self.catalog["words"]}
        self.food = [w for w in self.catalog["words"] if w["category"] == "food"]
        self.questions = [q for q in self.catalog["questions"] if q["id"].startswith("quiz.food.")]

    def test_offline_catalog_and_coverage(self):
        self.assertEqual((246, 222), validate_catalog(self.catalog))
        word_counts, question_counts = inspect_category_coverage(self.catalog)
        self.assertEqual((24, 20), (word_counts["food"], question_counts["food"]))

    def test_all_items_have_local_vectors_and_british_pronunciation(self):
        self.assertEqual(24, len(self.food))
        refs = [w["imageRef"] for w in self.food]
        self.assertEqual(24, len(set(refs)))
        self.assertEqual(24, len({w["enGB"].casefold() for w in self.food}))
        for word in self.food:
            self.assertEqual(word["enGB"], word["audioText"])
            self.assertEqual((3, 8), (word["ageRange"]["min"], word["ageRange"]["max"]))
            path = ROOT / "app/src/main/res" / (word["imageRef"] + ".xml")
            self.assertTrue(path.is_file(), str(path))
        for key, expected in (
            ("pear", ("Pera", "Pear")), ("yoghurt", ("Iogurte", "Yoghurt")),
            ("juice", ("Sumo", "Juice")), ("sandwich", ("Sanduíche", "Sandwich"))
        ):
            word = self.words["food." + key]
            self.assertEqual(expected, (word["ptPT"], word["enGB"]))

    def test_questions_have_three_distinct_options_and_pt_pt_prompt(self):
        self.assertEqual(20, len(self.questions))
        correct = set()
        for question in self.questions:
            choices = question["optionIds"]
            self.assertEqual(3, len(choices))
            self.assertEqual(3, len(set(choices)))
            self.assertIn(question["correctId"], choices)
            self.assertNotIn(question["correctId"], correct)
            correct.add(question["correctId"])
            self.assertTrue(all(self.words[c]["category"] == "food" for c in choices))
            self.assertEqual(3, len({self.words[c]["enGB"].casefold() for c in choices}))
            self.assertIn("«" + self.words[question["correctId"]]["ptPT"] + "»", question["prompt"])
        for key in ("pear", "grapes", "lemon", "tomato", "potato", "fish",
                    "pizza", "pasta", "yoghurt", "juice", "sandwich", "cake"):
            self.assertIn("food." + key, correct)

if __name__ == "__main__":
    unittest.main()
