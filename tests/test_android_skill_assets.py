"""Offline checks for the Android Skill assets (no Android SDK required)."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "skills" / "goutoujunshi"
ASSETS = (ROOT / "integrations" / "jev_android" / "app" / "src" / "main" /
          "assets" / "goutou_skill")


def index():
    return json.loads((ASSETS / "index.json").read_text(encoding="utf-8"))


class AndroidSkillAssetsTests(unittest.TestCase):
    def test_index_lists_every_contact_goal(self):
        data = index()
        self.assertEqual(data["version"], 1)
        for scene in ("自然接话", "主动邀约", "澄清关系", "修复冲突", "减少投入", "结束联系"):
            self.assertIn(scene, data["scenes"])
            self.assertIn("guide_replies.md", data["scenes"][scene])
            self.assertIn("guide_ocr.md", data["scenes"][scene])

    def test_all_referenced_assets_exist(self):
        data = index()
        names = [data["skill"]] + [item["asset"] for item in data["files"]]
        names += [name for files in data["scenes"].values() for name in files]
        for name in names:
            self.assertTrue((ASSETS / name).is_file(), name)

    def test_full_knowledge_library_is_bundled(self):
        self.assertEqual(len(list(ASSETS.glob("k??.md"))), 20)
        self.assertEqual(len(list(ASSETS.glob("p??.md"))), 23)

    def test_skill_is_the_unmodified_upstream_version(self):
        text = (ASSETS / "SKILL.md").read_text(encoding="utf-8")
        self.assertEqual(text, (SOURCE / "SKILL.md").read_text(encoding="utf-8"))
        self.assertIn("与其他 skill 的边界", text)
        self.assertIn("多对象硬隔离", text)

    def test_every_asset_matches_its_source(self):
        for item in index()["files"]:
            src = item["source"]
            if src.startswith("alias:"):
                expected = ASSETS / src[len("alias:"):]
            elif src == "SKILL.md":
                expected = SOURCE / "SKILL.md"
            elif (SOURCE / src).is_file():
                expected = SOURCE / src
            else:
                expected = ROOT / src
            self.assertEqual((ASSETS / item["asset"]).read_bytes(),
                             expected.read_bytes(), item["asset"])


if __name__ == "__main__":
    unittest.main()
