"""PE-033 calendar/time age gates, UK English and offline content tests."""
import copy
import unittest
from pathlib import Path
from validate_offline_catalog import load_catalog, validate_catalog
from validate_category_coverage import inspect_category_coverage
ROOT = Path(__file__).resolve().parents[1]
DAYS = ("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
MONTHS = ("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december")
CLOCKS = ("one_oclock", "two_oclock", "three_oclock", "four_oclock", "five_oclock", "six_oclock", "seven_oclock", "eight_oclock")
class CalendarTimeTests(unittest.TestCase):
    def setUp(self):
        self.cat = load_catalog()
        self.words = {w["id"]: w for w in self.cat["words"]}
    def test_counts_and_calendar(self):
        self.assertEqual((246,222),validate_catalog(self.cat))
        wc,qc = inspect_category_coverage(self.cat)
        self.assertEqual((19,16),(wc["calendar"],wc["time"]))
        self.assertEqual((19,16),(qc["calendar"],qc["time"]))
        self.assertTrue(all("calendar."+d in self.words for d in DAYS))
        self.assertTrue(all("calendar."+m in self.words for m in MONTHS))
    def test_age_gate(self):
        for s in MONTHS: self.assertEqual({"min":6,"max":8},self.words["calendar."+s]["ageRange"])
        for s in CLOCKS: self.assertEqual({"min":6,"max":8},self.words["time."+s]["ageRange"])
        for s in DAYS: self.assertEqual({"min":3,"max":8},self.words["calendar."+s]["ageRange"])
        for s in ("morning","afternoon","evening","night"): self.assertEqual({"min":3,"max":8},self.words["time."+s]["ageRange"])
    def test_greetings_and_full_hours(self):
        self.assertEqual("It's three o'clock",self.words["time.three_oclock"]["enGB"])
        self.assertEqual("Good evening",self.words["time.good_evening"]["enGB"])
        self.assertEqual("Good night",self.words["time.good_night"]["enGB"])
        self.assertNotEqual(self.words["time.good_evening"]["ptPT"],self.words["time.good_night"]["ptPT"])
    def test_images_options_and_suitable_distractors(self):
        ws=[w for w in self.cat["words"] if w["category"] in ("calendar","time")]
        self.assertEqual(35,len(ws))
        self.assertEqual(35,len({w["imageRef"] for w in ws}))
        for w in ws:
            self.assertEqual(w["enGB"],w["audioText"])
            self.assertTrue((ROOT/"app/src/main/res"/(w["imageRef"]+".xml")).is_file())
        for q in self.cat["questions"]:
            if not q["id"].startswith(("quiz.calendar.","quiz.time.")): continue
            opts=q["optionIds"]
            self.assertEqual(3,len(opts))
            self.assertEqual(3,len(set(opts)))
            self.assertIn(q["correctId"],opts)
            self.assertEqual(3,len({self.words[k]["enGB"].casefold() for k in opts}))
            self.assertTrue(all(self.words[k]["category"]==self.words[q["correctId"]]["category"] for k in opts))
            self.assertTrue(all(self.words[k]["ageRange"]["min"]<=self.words[q["correctId"]]["ageRange"]["min"] for k in opts))
            self.assertIn("«"+self.words[q["correctId"]]["ptPT"]+"»",q["prompt"])
    def test_corruption_is_rejected(self):
        mutated=copy.deepcopy(self.cat)
        next(w for w in mutated["words"] if w["id"]=="calendar.monday")["imageRef"]="https://example.org/calendar.svg"
        with self.assertRaises(ValueError): validate_catalog(mutated)
        mutated=copy.deepcopy(self.cat)
        next(q for q in mutated["questions"] if q["id"]=="quiz.time.morning")["optionIds"][0]="calendar.monday"
        with self.assertRaises(ValueError): inspect_category_coverage(mutated)
    def test_kotlin_ui_integration(self):
        model=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/domain/model/WordEntry.kt").read_text()
        filters=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/ui/components/LearningFilters.kt").read_text()
        quiz=(ROOT/"app/src/main/java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt").read_text()
        for key in ("CALENDAR","TIME"):
            self.assertIn(key+'("',model)
            self.assertIn("WordCategory."+key+" -> R.string.category_",filters)
            self.assertIn("WordCategory."+key,quiz)
        self.assertIn("quiz-pe033-illustration",quiz)
if __name__=="__main__": unittest.main()
