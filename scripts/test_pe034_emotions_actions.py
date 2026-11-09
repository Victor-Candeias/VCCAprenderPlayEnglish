"""PE-034: emotion/action vocabulary, inclusive labels and offline quiz integrity."""
import copy
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage
ROOT = Path(__file__).resolve().parents[1]
class EmotionsActionsTests(unittest.TestCase):
    def setUp(self):
        self.catalog = load_catalog()
        self.words = {w["id"]: w for w in self.catalog["words"]}
    def test_counts(self):
        self.assertEqual((296, 272), validate_catalog(self.catalog))
        wc, qc = inspect_category_coverage(self.catalog)
        self.assertEqual((15, 15), (wc["emotions"], wc["actions"]))
        self.assertEqual((15, 15), (qc["emotions"], qc["actions"]))
    def test_emotions_actions_and_simple_phrases(self):
        for ident, pt, en in (
            ("emotions.happy", "Feliz", "Happy"),
            ("emotions.sad", "Triste", "Sad"),
            ("emotions.angry", "Zangado ou zangada", "Angry"),
            ("emotions.tired", "Cansado ou cansada", "Tired"),
            ("emotions.excited", "Entusiasmado ou entusiasmada", "Excited"),
            ("actions.run", "Correr", "Run"),
            ("actions.jump", "Saltar", "Jump"),
            ("actions.play", "Brincar", "Play"),
            ("emotions.i_am_happy", "Estou feliz", "I am happy"),
            ("actions.i_can_run", "Eu consigo correr", "I can run")
        ):
            self.assertEqual((pt,en), (self.words[ident]["ptPT"], self.words[ident]["enGB"]))
    def test_no_moral_labels_and_age_level(self):
        new = [w for w in self.catalog["words"] if w["category"] in ("emotions","actions")]
        self.assertEqual(30,len(new))
        self.assertEqual(30,len({w["imageRef"] for w in new}))
        for w in new:
            self.assertEqual(w["enGB"],w["audioText"])
            self.assertTrue((ROOT/"app/src/main/res"/(w["imageRef"]+".xml")).is_file())
            if w["id"].split(".")[-1].startswith(("i_am_","i_can_")):
                self.assertEqual(6,w["ageRange"]["min"])
            else:
                self.assertEqual(3,w["ageRange"]["min"])
        for w in new:
            self.assertNotIn("bad", w["enGB"].lower())
            self.assertNotIn("good child",w["enGB"].lower())
    def test_quiz_distinct_and_age_safe(self):
        for q in self.catalog["questions"]:
            if not q["id"].startswith(("quiz.emotions.","quiz.actions.")): continue
            options=q["optionIds"]
            self.assertEqual(3,len(options))
            self.assertEqual(3,len(set(options)))
            self.assertIn(q["correctId"],options)
            self.assertEqual(3,len({self.words[k]["enGB"].casefold() for k in options}))
            word=self.words[q["correctId"]]
            self.assertTrue(all(self.words[k]["category"]==word["category"] for k in options))
            self.assertTrue(all(self.words[k]["ageRange"]["min"]<=word["ageRange"]["min"] for k in options))
            self.assertIn("«"+word["ptPT"]+"»",q["prompt"])
    def test_invalid_references_rejected(self):
        broken=copy.deepcopy(self.catalog)
        next(w for w in broken["words"] if w["id"]=="emotions.happy")["imageRef"]="drawable/pe_missing"
        with self.assertRaises(ValueError): validate_catalog(broken)
        broken=copy.deepcopy(self.catalog)
        next(q for q in broken["questions"] if q["id"]=="quiz.actions.run")["optionIds"][0]="emotions.happy"
        with self.assertRaises(ValueError): inspect_category_coverage(broken)
    def test_kotlin_screen_and_filter_integration(self):
        model=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/domain/model/WordEntry.kt").read_text(encoding="utf-8")
        filt=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/ui/components/LearningFilters.kt").read_text(encoding="utf-8")
        quiz=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt").read_text(encoding="utf-8")
        for c in ("EMOTIONS","ACTIONS"):
            self.assertIn(c+'("',model)
            self.assertIn("WordCategory."+c+" -> R.string.category_",filt)
            self.assertIn("WordCategory."+c,quiz)
        self.assertIn("quiz-pe034-illustration",quiz)
if __name__=="__main__": unittest.main()
