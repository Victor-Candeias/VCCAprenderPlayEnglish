"""Signed-release workflow regression checks; no secrets or Android SDK needed."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
RELEASE = ROOT / ".github/workflows/android-release.yml"
CI = ROOT / ".github/workflows/android-ci.yml"
APP_NAME = "VCC-Brincar-ao-Ingles"


class SignedReleaseWorkflowTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.release = RELEASE.read_text(encoding="utf-8")
        cls.debug_ci = CI.read_text(encoding="utf-8")

    def test_runs_on_main_push_and_remains_manual(self):
        self.assertIn(
            "on:\n  push:\n    branches:\n      - main\n  workflow_dispatch:",
            self.release,
        )
        self.assertIn("if: github.ref == 'refs/heads/main'", self.release)
        self.assertIn("contents: write", self.release)
        self.assertIn("cancel-in-progress: false", self.release)

    def test_same_certificate_required_before_upload(self):
        release = self.release
        required = [
            "Require Release Secrets",
            "Restore private Release keystore outside workspace",
            "Test and build signed Release",
            "Verify signature, host certificate and APK version",
            "Upload signed Release APK",
            "Tag version only after successful APK upload",
        ]
        positions = [release.index(s) for s in required]
        self.assertEqual(positions, sorted(positions))
        self.assertIn("--expected-sha256", release)
        self.assertIn("apksigner", release)
        self.assertIn("if: always()", release)

    def test_app_name_is_used_for_apk_and_artifact(self):
        release = self.release
        self.assertIn(
            f'apk_name="{APP_NAME}-v${{RELEASE_VERSION}}-release.apk"', release
        )
        self.assertIn(
            f"name: {APP_NAME}-v${{{{ steps.version.outputs.version_name }}}}-release",
            release,
        )
        self.assertIn("path: dist/${{ steps.verify.outputs.apk_name }}", release)
        self.assertIn('cmp -s "$apk" "dist/$apk_name"', release)

    def test_release_build_checks_content_and_debug_ci_needs_no_secrets(self):
        self.assertIn("validate_category_coverage.py", self.release)
        self.assertIn(":app:assembleRelease", self.release)
        self.assertIn("VCC_RELEASE_KEYSTORE_BASE64", self.release)
        self.assertNotIn("VCC_RELEASE_KEYSTORE_BASE64", self.debug_ci)


if __name__ == "__main__":
    unittest.main()
