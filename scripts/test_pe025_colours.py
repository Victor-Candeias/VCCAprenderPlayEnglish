"""PE-025: validate bilingual colours, age bands and real UI swatches."""
import re
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog

ROOT = Path(__file__).resolve().parents[1]
QUIZ_SCREEN = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt"

class ColoursEditorialTests(unittest.TestCase):
    def setUp(self):
        self.catalog = load_catalog()
        self.words = {w["id"]: w for w in self.catalog["words"]}
        self.colours = [w for w in self.catalog["words"] if w["category"] == "colors"]
        self.questions = [q for q in self.catalog["questions"] if q["id"].startswith("quiz.colors.")]

    def test_fifteen_distinct_translated_colours_with_english_audio(self):
        self.assertGreaterEqual(len(self.colours), 15)
        self.assertEqual(len(self.colours), len({w["enGB"].casefold() for w in self.colours}))
        for word in self.colours:
            self.assertEqual(word["enGB"], word["audioText"])
            self.assertTrue(word["ptPT"].strip())
            self.assertTrue(word["enGB"].strip())

    def test_advanced_shades_are_restricted_to_age_six_to_eight(self):
        for name, pt, en in (
            ("silver", "Prateado", "Silver"),
            ("beige", "Bege", "Beige"),
            ("turquoise", "Turquesa", "Turquoise"),
        ):
            word = self.words["colors." + name]
            self.assertEqual((pt, en), (word["ptPT"], word["enGB"]))
            self.assertEqual({"min": 6, "max": 8}, word["ageRange"])

    def test_all_colours_have_corresponding_swatch_in_real_quiz(self):
        source = QUIZ_SCREEN.read_text(encoding="utf-8")
        for word in self.colours:
            color_id = word["id"].split(".")[-1]
            self.assertRegex(source, r'"' + re.escape(color_id) + r'"\s*->\s*(?:Color\(0xFF[0-9A-Fa-f]{6}\)|Color\.White)')

    def test_every_shade_question_has_three_unambiguous_choices(self):
        for q in self.questions:
            self.assertEqual(3, len(q["optionIds"]))
            self.assertEqual(3, len(set(q["optionIds"])))
            self.assertIn(q["correctId"], q["optionIds"])
            correct = self.words[q["correctId"]]
            self.assertIn("«" + correct["ptPT"] + "»", q["prompt"])
            translated = [self.words[x]["enGB"].casefold() for x in q["optionIds"]]
            self.assertEqual(3, len(set(translated)))

if __name__ == "__main__":
    unittest.main()
