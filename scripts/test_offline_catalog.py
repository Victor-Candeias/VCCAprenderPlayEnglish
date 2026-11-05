"""PE-012: deterministic tampering tests for offline JSON schema and references."""
import copy
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_offline_catalog import load_catalog, validate_catalog


class OfflineCatalogTests(unittest.TestCase):
    def setUp(self):
        self.sample = load_catalog()

    def test_packaged_catalog_is_valid_and_uses_unicode(self):
        self.assertEqual((18, 6), validate_catalog(self.sample))
        self.assertEqual(
            "Pássaro",
            next(w["ptPT"] for w in self.sample["words"] if w["id"] == "animals.bird")
        )
        self.assertEqual("en-GB", self.sample["locales"]["target"])

    def mutated(self):
        return copy.deepcopy(self.sample)

    def test_rejects_duplicate_word_id(self):
        data = self.mutated()
        data["words"].append(copy.deepcopy(data["words"][0]))
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_empty_translation(self):
        data = self.mutated()
        data["words"][0]["enGB"] = " "
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_unknown_option_reference(self):
        data = self.mutated()
        data["questions"][0]["optionIds"][0] = "animals.dinosaur"
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_repeated_options(self):
        data = self.mutated()
        data["questions"][0]["optionIds"][0] = data["questions"][0]["optionIds"][1]
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_missing_correct_choice(self):
        data = self.mutated()
        data["questions"][0]["correctId"] = "animals.dinosaur"
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_wrong_option_count(self):
        data = self.mutated()
        data["questions"][0]["optionIds"] = ["animals.cat", "animals.dog"]
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_invalid_age_range(self):
        data = self.mutated()
        data["words"][0]["ageRange"] = {"min": 8, "max": 3}
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_bad_schema_and_locales(self):
        data = self.mutated()
        data["schemaVersion"] = 2
        with self.assertRaises(ValueError):
            validate_catalog(data)
        data = self.mutated()
        data["locales"]["target"] = "en-US"
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_unbundled_remote_images(self):
        data = self.mutated()
        data["words"][0]["imageRef"] = "https://example.org/cat.png"
        with self.assertRaises(ValueError):
            validate_catalog(data)

    def test_rejects_invalid_reference_age_overlap(self):
        data = self.mutated()
        data["words"][0]["ageRange"] = {"min": 3, "max": 3}
        data["words"][1]["ageRange"] = {"min": 8, "max": 8}
        with self.assertRaises(ValueError):
            validate_catalog(data)


if __name__ == "__main__":
    unittest.main()
