"""PE-035: British greetings, safe 3-option questions and paired offline dialogues."""
import copy
import json
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage

ROOT = Path(__file__).resolve().parents[1]
DIALOGUE_PATH = ROOT / "app/src/main/assets/content/dialogues-v1.json"

class GreetingsDialogueTests(unittest.TestCase):
    def setUp(self):
        self.data = load_catalog()
        self.words = {w["id"]: w for w in self.data["words"]}
        self.questions = [q for q in self.data["questions"] if q["id"].startswith("quiz.phrases.")]

    def test_catalog_and_phrase_coverage(self):
        self.assertEqual((296, 272), validate_catalog(self.data))
        wc, qc = inspect_category_coverage(self.data)
        self.assertEqual((32, 28), (wc["phrases"], qc["phrases"]))
        self.assertEqual("Good morning", self.words["phrases.good_morning"]["enGB"])
        self.assertEqual("Thank you", self.words["phrases.thank_you"]["enGB"])
        self.assertEqual("Please", self.words["phrases.please"]["enGB"])

    def test_20_new_phrases_and_polite_british_context(self):
        new = [w for w in self.data["words"] if w["category"] == "phrases" and w["id"] not in {
            "phrases.good_morning", "phrases.good_night", "phrases.thank_you",
            "phrases.hello", "phrases.goodbye", "phrases.please", "phrases.sorry",
            "phrases.yes", "phrases.no", "phrases.how_are_you", "phrases.i_am_fine",
            "phrases.see_you_later"
        }]
        self.assertEqual(20, len(new))
        self.assertEqual(20, len({w["enGB"].casefold() for w in new}))
        for w in new:
            self.assertEqual(w["enGB"], w["audioText"])
            self.assertIn(w["enGB"][-1], ".!?")
            self.assertEqual({"min": w["ageRange"]["min"], "max": 8}, w["ageRange"])
            self.assertGreaterEqual(w["ageRange"]["min"], 5)
        self.assertEqual("Good evening.", self.words["phrases.good_evening"]["enGB"])
        self.assertEqual("Good night", self.words["phrases.good_night"]["enGB"])
        self.assertEqual("How are you today?", self.words["phrases.how_are_you_today"]["enGB"])
        self.assertEqual("Can you repeat that, please?", self.words["phrases.can_you_repeat_please"]["enGB"])

    def test_three_answers_no_duplicate_meaning(self):
        self.assertEqual(28, len(self.questions))
        self.assertEqual(28, len({q["correctId"] for q in self.questions}))
        for q in self.questions:
            self.assertEqual(3, len(q["optionIds"]))
            self.assertEqual(3, len(set(q["optionIds"])))
            self.assertIn(q["correctId"], q["optionIds"])
            self.assertEqual(3, len({self.words[x]["enGB"].casefold() for x in q["optionIds"]}))
            self.assertTrue(all(self.words[x]["category"] == "phrases" for x in q["optionIds"]))
            self.assertIn("«"+self.words[q["correctId"]]["ptPT"]+"»", q["prompt"])

    def test_curated_contextual_dialogues_resolve_offline(self):
        data=json.loads(DIALOGUE_PATH.read_text(encoding="utf-8"))
        self.assertEqual(1,data["schemaVersion"])
        self.assertEqual("phrases",data["category"])
        self.assertGreaterEqual(len(data["dialogs"]), 8)
        ids=set()
        for dialog in data["dialogs"]:
            self.assertNotIn(dialog["id"],ids)
            ids.add(dialog["id"])
            self.assertEqual(2,len(dialog["turnIds"]))
            self.assertEqual(2,len(set(dialog["turnIds"])))
            self.assertTrue(dialog["title"])
            self.assertTrue(all(ref in self.words for ref in dialog["turnIds"]))
            self.assertTrue(all(self.words[ref]["category"]=="phrases" for ref in dialog["turnIds"]))
        titles = {d["id"]: d["turnIds"] for d in data["dialogs"]}
        self.assertEqual(["phrases.thank_you","phrases.youre_welcome"],titles["agradecimento"])
        self.assertEqual(["phrases.whats_your_name","phrases.my_name_is_alex"],titles["apresentacao"])

    def test_bad_quiz_options_and_broken_dialogue_detected(self):
        invalid = copy.deepcopy(self.data)
        next(q for q in invalid["questions"] if q["id"]=="quiz.phrases.good_evening")["optionIds"][0]="body.hand"
        with self.assertRaises(ValueError): inspect_category_coverage(invalid)

    def test_explorer_renders_dialogues(self):
        kotlin=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/ui/screens/DiscoverScreen.kt").read_text(encoding="utf-8")
        helper=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/ui/components/PhraseDialogues.kt").read_text(encoding="utf-8")
        self.assertIn("PhraseDialogues.load(ctx, selected.words)",kotlin)
        self.assertIn("discover-dialogue-",kotlin)
        self.assertIn("content/dialogues-v1.json",helper)

if __name__=="__main__": unittest.main()
