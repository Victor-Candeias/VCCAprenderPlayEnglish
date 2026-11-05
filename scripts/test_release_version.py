"""PE-010: deterministic offline checks for Release version/tag rules."""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from release_version import next_version, parse_tag, version_code


class ReleaseVersionTests(unittest.TestCase):
    def test_first_release_starts_at_0_1_0(self):
        self.assertEqual((0, 1, 0), next_version([]))
        self.assertEqual(1000, version_code((0, 1, 0)))

    def test_patch_increments_after_latest_semver_not_latest_string(self):
        self.assertEqual(
            (0, 1, 11),
            next_version(["v0.1.9", "v0.1.10", "v0.1.2"])
        )
        self.assertEqual(
            (1, 2, 4),
            next_version(["v0.9.9", "v1.2.3", "v1.2.1"])
        )

    def test_bad_tags_do_not_become_release_versions(self):
        self.assertIsNone(parse_tag("v1.2"))
        self.assertIsNone(parse_tag("v1.2.3-rc1"))
        self.assertIsNone(parse_tag("v01.2.3"))
        self.assertIsNone(parse_tag("release-0.1.3"))
        self.assertEqual((0, 1, 0), next_version(["test-foo", "v1.2.3-rc1"]))

    def test_rollover_remains_monotonic(self):
        new = next_version(["v0.1.999"])
        self.assertEqual((0, 2, 0), new)
        self.assertGreater(version_code(new), version_code((0, 1, 999)))
        major = next_version(["v1.999.999"])
        self.assertEqual((2, 0, 0), major)
        self.assertGreater(version_code(major), version_code((1, 999, 999)))

    def test_bounds_prevent_invalid_android_version_codes(self):
        with self.assertRaises(ValueError):
            version_code((0, 0, 0))
        with self.assertRaises(ValueError):
            version_code((2101, 0, 0))
        with self.assertRaises(ValueError):
            next_version(["v2100.0.0"])
        with self.assertRaises(ValueError):
            next_version(["v0.1.1000"])


if __name__ == "__main__":
    unittest.main()
