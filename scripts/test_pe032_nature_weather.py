"""PE-032: nature, weather and the four seasons in offline en-GB."""
import copy
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]

class NatureWeatherTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()
        self.words = {w["id"]: w for w in self.data["words"]}

    def test_new_categories_and_previous_categories_are_preserved(self):
        self.assertEqual((296, 272), validate_catalog(self.data))
        wc, qc = inspect_category_coverage(self.data)
        self.assertEqual((12, 12), (wc["nature"], wc["weather"]))
        self.assertEqual((12, 12), (qc["nature"], qc["weather"]))
        self.assertEqual((12, 12), (wc["family"], wc["body"]))

    def test_seasons_british_spelling_and_weather_is_not_clock_time(self):
        for id, pt, en in [
            ("nature.spring", "Primavera", "Spring"),
            ("nature.summer", "Verão", "Summer"),
            ("nature.autumn", "Outono", "Autumn"),
            ("nature.winter", "Inverno", "Winter"),
            ("weather.rain", "Chuva", "Rain"),
            ("weather.wind", "Vento", "Wind")
        ]:
            word = self.words[id]
            self.assertEqual((pt, en), (word["ptPT"], word["enGB"]))
        new_words = [w for w in self.data["words"] if w["category"] in ("nature", "weather")]
        self.assertNotIn("time", {w["enGB"].casefold() for w in new_words})
        self.assertEqual(24, len(new_words))
        for category in ("nature", "weather"):
            entries = [w for w in new_words if w["category"] == category]
            self.assertEqual(12, len({w["enGB"].casefold() for w in entries}))
            self.assertEqual(12, len({w["imageRef"] for w in entries}))
            for w in entries:
                self.assertEqual(w["enGB"], w["audioText"])
                self.assertEqual({"min": 3, "max": 8}, w["ageRange"])
                self.assertEqual(f"drawable/pe_{category}_" + w["id"].split(".")[-1], w["imageRef"])
                self.assertTrue((ROOT / "app/src/main/res" / (w["imageRef"] + ".xml")).is_file())

    def test_questions_have_three_distinct_nonoverlapping_options(self):
        for category in ("nature", "weather"):
            qs = [q for q in self.data["questions"] if q["id"].startswith("quiz." + category + ".")]
            self.assertEqual(12, len(qs))
            self.assertEqual(12, len({q["correctId"] for q in qs}))
            for q in qs:
                self.assertEqual(3, len(q["optionIds"]))
                self.assertEqual(3, len(set(q["optionIds"])))
                self.assertIn(q["correctId"], q["optionIds"])
                self.assertTrue(all(self.words[id]["category"] == category for id in q["optionIds"]))
                self.assertEqual(3, len({self.words[id]["enGB"].casefold() for id in q["optionIds"]}))
                self.assertIn("«" + self.words[q["correctId"]]["ptPT"] + "»", q["prompt"])

    def test_broken_image_and_cross_category_distractor_are_rejected(self):
        data = copy.deepcopy(self.data)
        next(w for w in data["words"] if w["id"] == "nature.spring")["imageRef"] = "drawable/missing_pe032"
        with self.assertRaises(ValueError):
            validate_catalog(data)
        data = copy.deepcopy(self.data)
        q = next(q for q in data["questions"] if q["id"] == "quiz.weather.rain")
        q["optionIds"][0] = "nature.tree"
        with self.assertRaises(ValueError):
            inspect_category_coverage(data)

    def test_kotlin_category_integration(self):
        model = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/domain/model/WordEntry.kt").read_text(encoding="utf-8")
        filters = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/components/LearningFilters.kt").read_text(encoding="utf-8")
        quiz = (ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt").read_text(encoding="utf-8")
        for category in ("NATURE", "WEATHER"):
            self.assertIn(category + '("', model)
            self.assertIn("WordCategory." + category + " -> R.string.category_", filters)
            self.assertIn("WordCategory." + category, quiz)
        self.assertIn("quiz-pe032-illustration", quiz)

if __name__ == "__main__":
    unittest.main()
