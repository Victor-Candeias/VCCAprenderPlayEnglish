"""PE-027: verify cardinal numbers, geometric shapes, ages and distractors."""
import re
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog

ROOT = Path(__file__).resolve().parents[1]
SCREEN = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt"
CARDINALS = ["zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen","twenty"]
TENS = ["thirty","forty","fifty"]
SHAPES = ["shape_circle","shape_square","shape_triangle","shape_rectangle","shape_star","shape_diamond","shape_pentagon","shape_hexagon"]

class NumbersShapesTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()
        self.words = {w["id"]: w for w in self.data["words"]}
        self.questions = [q for q in self.data["questions"] if q["id"].startswith("quiz.numbers.")]

    def test_all_cardinals_decades_and_shapes_have_en_gb_audio(self):
        entries = [w for w in self.data["words"] if w["category"] == "numbers"]
        self.assertEqual(32, len(entries))
        self.assertEqual(28, len(self.questions))
        for id_ in CARDINALS + TENS + SHAPES:
            word = self.words["numbers." + id_]
            self.assertEqual(word["enGB"], word["audioText"])
        self.assertEqual(32, len({w["enGB"].casefold() for w in entries}))

    def test_age_bands(self):
        for id_ in CARDINALS[13:] + TENS + SHAPES[3:]:
            self.assertEqual(6, self.words["numbers." + id_]["ageRange"]["min"])
        for id_ in CARDINALS[:13] + SHAPES[:3]:
            self.assertEqual(3, self.words["numbers." + id_]["ageRange"]["min"])

    def test_three_unambiguous_choices_per_question(self):
        for q in self.questions:
            opts = q["optionIds"]
            self.assertEqual(3, len(opts))
            self.assertEqual(3, len(set(opts)))
            self.assertEqual(1, opts.count(q["correctId"]))
            self.assertTrue(all(self.words[o]["category"] == "numbers" for o in opts))
            self.assertEqual(3, len({self.words[o]["enGB"].casefold() for o in opts}))
            self.assertIn("«" + self.words[q["correctId"]]["ptPT"] + "»", q["prompt"])
            ages = [self.words[o]["ageRange"] for o in opts]
            self.assertLessEqual(max(x["min"] for x in ages), min(x["max"] for x in ages))

    def test_all_numbers_and_shapes_have_real_visual_mapping(self):
        source = SCREEN.read_text(encoding="utf-8")
        self.assertIn('"●".repeat(count)', source)
        self.assertIn('"quiz-number-shape-illustration"', source)
        for id_ in CARDINALS + TENS + SHAPES:
            self.assertRegex(source, r'"' + re.escape(id_) + r'"\\s*->')
        for id_ in SHAPES:
            self.assertRegex(source, r'"' + re.escape(id_) + r'"\\s*->\\s*"[^"]+"')

    def test_no_ordinals_in_prompts(self):
        for q in self.questions:
            self.assertNotIn("ordinal", q["prompt"].casefold())
