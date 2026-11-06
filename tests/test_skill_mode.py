"""Offline checks for the optional upstream Skill bundle switch."""
import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'integrations' / 'jev_mac'))

import core


class SkillModeTests(unittest.TestCase):
    def setUp(self):
        env = patch.dict(os.environ, {}, clear=False)
        env.start()
        self.addCleanup(env.stop)
        os.environ.pop('GOUTOU_SKILL_MODE', None)

    def test_default_keeps_desktop_builtin(self):
        self.assertEqual(core.skill_mode(), 'builtin')
        self.assertEqual(core.skill_root(), core.ROOT)
        custom = core.ROOT / 'custom-root'
        self.assertEqual(core.skill_root(custom), custom)

    def test_full_uses_bundled_upstream_package(self):
        os.environ['GOUTOU_SKILL_MODE'] = 'full'
        self.assertEqual(core.skill_root(), core.FULL_SKILL_DIR)
        self.assertTrue((core.FULL_SKILL_DIR / 'SKILL.md').is_file())
        self.assertTrue((core.FULL_SKILL_DIR / 'LICENSE').is_file())
        for path in core.reference_paths('日常回复'):
            self.assertTrue((core.FULL_SKILL_DIR / path).is_file(), path)

    def test_unknown_mode_is_rejected(self):
        os.environ['GOUTOU_SKILL_MODE'] = 'other'
        with self.assertRaises(ValueError):
            core.skill_root()

    def test_full_bundle_keeps_upstream_rules_and_adds_desktop_rules(self):
        os.environ['GOUTOU_SKILL_MODE'] = 'full'
        paths = core.reference_paths('日常回复')
        skill, references = core.load_bundle(core.ROOT, paths)
        self.assertIn('与其他 skill 的边界', skill)
        self.assertIn('桌面运行时适配', skill)
        plain = '\n\n'.join((core.FULL_SKILL_DIR / p).read_text(encoding='utf-8') for p in paths)
        self.assertGreater(len(references), len(plain))

    def test_builtin_bundle_is_unchanged(self):
        paths = core.reference_paths('冲突修复')
        skill, references = core.load_bundle(core.ROOT, paths)
        self.assertNotIn('与其他 skill 的边界', skill)
        self.assertNotIn('桌面运行时适配', skill)
        expected = '\n\n'.join((core.ROOT / p).read_text(encoding='utf-8') for p in paths)
        self.assertEqual(references, expected)

    def test_both_modes_build_messages(self):
        snapshot = core.Snapshot('测试会话', '我：周末一起去看展吗？\n对方：这周有点忙，下周再看看吧。')
        for mode in ('builtin', 'full'):
            with self.subTest(mode=mode):
                os.environ['GOUTOU_SKILL_MODE'] = mode
                messages, paths = core.build_messages(snapshot, '邀约推进', '认识两周，正在了解中')
                self.assertEqual(len(messages), 2)
                self.assertEqual(paths, core.reference_paths('邀约推进'))
                self.assertIn('只输出一个 JSON 对象', messages[0]['content'])

    def test_jev_strategy_layer_uses_the_same_bundle(self):
        from jev import JevConfig, build_request
        snapshot = core.Snapshot('测试会话', '我：周末一起去看展吗？\n对方：这周有点忙，下周再看看吧。')
        config = JevConfig('dummy-key')
        for mode in ('builtin', 'full'):
            with self.subTest(mode=mode):
                os.environ['GOUTOU_SKILL_MODE'] = mode
                payload = build_request(config, snapshot, '邀约推进', '')
                guide = payload['state']['strategy_guide']
                expected = (core.skill_root() / core.reference_paths('邀约推进')[0]).read_text(
                    encoding='utf-8').split('## 常用话术库', 1)[0]
                self.assertEqual(guide, expected)


if __name__ == '__main__':
    unittest.main()
