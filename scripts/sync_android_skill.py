#!/usr/bin/env python3
"""Sync the bundled goutoujunshi Skill into the Android assets folder.

Source: skills/goutoujunshi (unmodified upstream Skill, MIT) plus the desktop
OCR guide from references/practical. Asset filenames are ASCII so aapt and
AssetManager never have to deal with CJK asset paths.

Run from anywhere: python -B scripts/sync_android_skill.py
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "skills" / "goutoujunshi"
OCR_GUIDE = ROOT / "references" / "practical" / "桌面OCR对话接入.md"
TARGET = (ROOT / "integrations" / "jev_android" / "app" / "src" / "main" /
          "assets" / "goutou_skill")

ALIASES = {
    "guide_replies.md": "实战话术编排器：从一句回复到后续分支.md",
    "guide_online.md": "09-在线约会与数字关系.md",
    "guide_invite.md": "主动表达、第一次见面与自然接触.md",
    "guide_conflict.md": "07-沟通冲突与修复.md",
    "guide_withdraw.md": "关系投入失衡：互惠判断、降级投入与退出决策.md",
}

SCENES = {
    "自然接话": ["guide_replies.md", "guide_online.md", "guide_ocr.md"],
    "主动邀约": ["guide_replies.md", "guide_invite.md", "guide_ocr.md"],
    "澄清关系": ["guide_replies.md", "guide_conflict.md", "guide_ocr.md"],
    "修复冲突": ["guide_replies.md", "guide_conflict.md", "guide_ocr.md"],
    "减少投入": ["guide_replies.md", "guide_withdraw.md", "guide_ocr.md"],
    "结束联系": ["guide_replies.md", "guide_withdraw.md", "guide_ocr.md"],
}


def main() -> int:
    if not (SOURCE / "SKILL.md").is_file():
        raise SystemExit("缺少完整 Skill 包：skills/goutoujunshi/SKILL.md")
    if not OCR_GUIDE.is_file():
        raise SystemExit("缺少桌面 OCR 指南：references/practical/桌面OCR对话接入.md")
    if TARGET.exists():
        shutil.rmtree(TARGET)
    TARGET.mkdir(parents=True)

    manifest = []
    shutil.copyfile(SOURCE / "SKILL.md", TARGET / "SKILL.md")
    manifest.append({"asset": "SKILL.md", "source": "SKILL.md"})

    for prefix, folder in (("k", "knowledge"), ("p", "practical")):
        files = sorted((SOURCE / "references" / folder).glob("*.md"))
        for i, path in enumerate(files, 1):
            name = f"{prefix}{i:02d}.md"
            shutil.copyfile(path, TARGET / name)
            manifest.append({"asset": name, "source": f"references/{folder}/{path.name}"})

    by_name = {item["source"].rsplit("/", 1)[-1]: item["asset"] for item in manifest}
    for alias, original in ALIASES.items():
        if original not in by_name:
            raise SystemExit(f"找不到语义化指南：{original}")
        shutil.copyfile(TARGET / by_name[original], TARGET / alias)
        manifest.append({"asset": alias, "source": f"alias:{by_name[original]}"})

    shutil.copyfile(OCR_GUIDE, TARGET / "guide_ocr.md")
    manifest.append({"asset": "guide_ocr.md", "source": "references/practical/桌面OCR对话接入.md"})

    index = {
        "version": 1,
        "source": "skills/goutoujunshi (upstream goutoujunshi, MIT)",
        "budget_chars": 6000,
        "skill": "SKILL.md",
        "scenes": SCENES,
        "files": manifest,
    }
    (TARGET / "index.json").write_text(
        json.dumps(index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"synced {len(manifest) + 1} files -> {TARGET.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
