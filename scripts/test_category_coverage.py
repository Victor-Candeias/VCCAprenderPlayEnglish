"""PE-013: enforce coverage, PT-PT prompt quality, and distinct vocabulary."""
import copy
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_category_coverage import inspect_category_coverage
from validate_offline_catalog import load_catalog


class CategoryCoverageTests(unittest.TestCase):
    def setUp(self):
        self.catalog = load_catalog()

    def test_all_eight_categories_have_words_and_questions(self):
        words, questions = inspect_category_coverage(self.catalog)
        self.assertEqual(163, sum(words.values()))
        self.assertEqual(139, sum(questions.values()))
        self.assertEqual(15, words['colors'])
        self.assertEqual(11, questions['colors'])
        self.assertEqual(32, words['numbers'])
        self.assertEqual(28, questions['numbers'])
        self.assertEqual(32, words['animals'])
        self.assertEqual(28, questions['animals'])
        self.assertEqual(24, words['food'])
        self.assertEqual(24, words['objects'])
        self.assertEqual(12, words['clothing'])
        self.assertEqual(12, words['transport'])
        self.assertTrue(all(count == 12 for category, count in words.items() if category not in ('colors', 'numbers', 'animals', 'food', 'objects', 'clothing', 'transport')))
        self.assertEqual(20, questions['food'])
        self.assertEqual(20, questions['objects'])
        self.assertEqual(12, questions['clothing'])
        self.assertEqual(12, questions['transport'])
        self.assertTrue(all(count == 8 for category, count in questions.items() if category not in ('colors', 'numbers', 'animals', 'food', 'objects')))

    def test_catches_wrong_pt_pt_prompt(self):
        data = copy.deepcopy(self.catalog)
        data["questions"][0]["prompt"] = "Como se diz «Leão» em inglês?"
        with self.assertRaises(ValueError):
            inspect_category_coverage(data)

    def test_catches_mixed_category_options(self):
        data = copy.deepcopy(self.catalog)
        data["questions"][0]["optionIds"][0] = "food.apple"
        with self.assertRaises(ValueError):
            inspect_category_coverage(data)

    def test_catches_missing_category_questions(self):
        data = copy.deepcopy(self.catalog)
        data["questions"] = [
            q for q in data["questions"] if not q["id"].startswith("quiz.food.")
        ]
        with self.assertRaises(ValueError):
            inspect_category_coverage(data)

    def test_catches_duplicate_english_word_in_one_category(self):
        data = copy.deepcopy(self.catalog)
        animal_words = [x for x in data["words"] if x["category"] == "animals"]
        animal_words[3]["enGB"] = animal_words[0]["enGB"]
        with self.assertRaises(ValueError):
            inspect_category_coverage(data)


if __name__ == "__main__":
    unittest.main()
