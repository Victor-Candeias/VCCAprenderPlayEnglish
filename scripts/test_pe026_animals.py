"""PE-026: bilingual animal content, age bands, questions and differentiated illustrations."""
import re
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog

ROOT = Path(__file__).resolve().parents[1]
ILLUSTRATIONS = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/components/AnimalIllustrations.kt"
QUIZ = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt"
PRONUNCIATION = ROOT / "app/src/main/java/com/vcc/aprender/playenglish/ui/screens/PronunciationScreen.kt"

class AnimalsTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()
        self.lookup = {w["id"]: w for w in self.data["words"]}
        self.animals = [w for w in self.data["words"] if w["category"] == "animals"]
        self.questions = [q for q in self.data["questions"] if q["id"].startswith("quiz.animals.")]

    def test_thirty_animals_and_pronounceable_british_english(self):
        validate_catalog(self.data)
        self.assertGreaterEqual(len(self.animals), 30)
        self.assertEqual(32, len(self.animals))
        self.assertEqual(28, len(self.questions))
        self.assertEqual(32, len({w["enGB"].casefold() for w in self.animals}))
        for w in self.animals:
            self.assertEqual(w["enGB"], w["audioText"])
            self.assertEqual("animals", w["id"].split(".")[0])

    def test_domestic_wild_marine_and_unambiguous_examples(self):
        for key, pt, en in (
            ("cat", "Gato", "Cat"), ("dog", "Cão", "Dog"),
            ("lion", "Leão", "Lion"), ("elephant", "Elefante", "Elephant"),
            ("dolphin", "Golfinho", "Dolphin"), ("shark", "Tubarão", "Shark"),
            ("seahorse", "Cavalo-marinho", "Seahorse"),
            ("panda", "Panda-gigante", "Giant panda"),
        ):
            w = self.lookup["animals." + key]
            self.assertEqual((pt, en), (w["ptPT"], w["enGB"]))
        self.assertNotIn("animals.turtle", self.lookup)

    def test_all_question_distractors_are_distinct_and_age_compatible(self):
        for q in self.questions:
            opts = q["optionIds"]
            self.assertEqual(3, len(opts))
            self.assertEqual(3, len(set(opts)))
            self.assertEqual(1, opts.count(q["correctId"]))
            selected = [self.lookup[o] for o in opts]
            self.assertEqual(3, len({w["enGB"].casefold() for w in selected}))
            self.assertTrue(all(w["category"] == "animals" for w in selected))
            self.assertLessEqual(max(w["ageRange"]["min"] for w in selected),
                                 min(w["ageRange"]["max"] for w in selected))
            self.assertIn("«"+self.lookup[q["correctId"]]["ptPT"]+"»",q["prompt"])

    def test_interim_animal_glyph_mapping_and_ui_usage(self):
        source = ILLUSTRATIONS.read_text(encoding="utf-8")
        for w in self.animals:
            key = w["id"].split(".")[-1]
            self.assertRegex(source, r'"'+re.escape(key)+r'"\s*->\s*"[^"]+"')
        self.assertIn("AnimalIllustrations.symbol(questionId)", QUIZ.read_text(encoding="utf-8"))
        self.assertIn("AnimalIllustrations.symbol(word.id)", PRONUNCIATION.read_text(encoding="utf-8"))
        self.assertIn('"quiz-animal-illustration"', QUIZ.read_text(encoding="utf-8"))

if __name__ == "__main__":
    unittest.main()
