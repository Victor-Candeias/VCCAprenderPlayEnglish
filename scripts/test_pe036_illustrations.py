import copy
import sys
import unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_offline_catalog import validate_catalog, load_catalog

class IllustrationsTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()

    def test_all_food_items_have_distinct_valid_vectors(self):
        food = [w for w in self.data["words"] if w["category"] == "food"]
        self.assertEqual(24, len(food))
        self.assertEqual(24, len(set(w["imageRef"] for w in food)))
        self.assertEqual((139, 115), validate_catalog(self.data))

    def test_invalid_or_remote_paths_are_rejected(self):
        for path in ("drawable/absent", "drawable/../secret", "https://example.com/img.png"):
            clone = copy.deepcopy(self.data)
            clone["words"][0]["imageRef"] = path
            with self.assertRaises(ValueError):
                validate_catalog(clone)

    def test_phrases_fallback_to_text(self):
        self.assertTrue(all(w["imageRef"] is None for w in self.data["words"] if w["category"] == "phrases"))

if __name__ == "__main__":
    unittest.main()
