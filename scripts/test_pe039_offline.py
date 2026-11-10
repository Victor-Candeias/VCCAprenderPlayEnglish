"""PE-039 automated regressions for offline content/manifest and TTS safeguards."""
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from validate_pe039_offline import audit, MANIFEST, ASSETS, OUTPUT, POLICY

class OfflineSafeguardsTests(unittest.TestCase):
    def test_offline_package_and_voice_safeguards(self):
        self.assertEqual([], audit())

    def test_internet_permission_is_not_allowed(self):
        with tempfile.TemporaryDirectory() as temp:
            manifest = Path(temp) / "AndroidManifest.xml"
            content = MANIFEST.read_text(encoding="utf-8")
            content = content.replace("</manifest>", '<uses-permission android:name="android.permission.INTERNET"/></manifest>')
            manifest.write_text(content, encoding="utf-8")
            self.assertTrue(any("INTERNET" in item for item in audit(manifest_path=manifest)))

    def test_remote_asset_reference_is_detected(self):
        with tempfile.TemporaryDirectory() as temp:
            folder = Path(temp)
            for original in ASSETS.glob("*.json"):
                (folder / original.name).write_bytes(original.read_bytes())
            (folder / "remote.json").write_text('{"asset": "https://example.com/image.png"}', encoding="utf-8")
            self.assertTrue(any("Remote content URL" in item for item in audit(assets_dir=folder)))

    def test_missing_voice_gate_is_detected(self):
        with tempfile.TemporaryDirectory() as temp:
            output = Path(temp) / "Output.kt"
            output.write_text(OUTPUT.read_text(encoding="utf-8").replace("setVoice(chosen)", "setLanguage(Locale.UK)"), encoding="utf-8")
            self.assertTrue(any("explicit offline voice selection" in item for item in audit(output_path=output)))

    def test_offline_no_voice_does_not_block_textual_quiz(self):
        # Quiz is backed by bundled JSON and never invokes the TTS engine.
        from validate_offline_catalog import load_catalog
        catalog = load_catalog()
        self.assertGreaterEqual(len(catalog["questions"]), 150)
        quiz_screen = (MANIFEST.parent / "java/com/vcc/aprender/playenglish/ui/screens/QuizScreen.kt").read_text(encoding="utf-8")
        self.assertNotIn("AndroidEnglishPronunciationOutput", quiz_screen)
        self.assertNotIn("TextToSpeech(", quiz_screen)

if __name__ == "__main__":
    unittest.main()
