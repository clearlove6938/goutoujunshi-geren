package com.jev.probe.core

import org.junit.Assert.*
import org.junit.Test

class SkillBundleTest {

    @Test fun unknownGoalFallsBackToDailyGuides() {
        val daily = SkillBundle.filesFor("自然接话")
        assertEquals(daily, SkillBundle.filesFor(null))
        assertEquals(daily, SkillBundle.filesFor("没这个目标"))
    }

    @Test fun everySceneHasPhrasingAndOcrGuides() {
        for (scene in listOf("自然接话", "主动邀约", "澄清关系", "修复冲突", "减少投入", "结束联系")) {
            val files = SkillBundle.filesFor(scene)
            assertTrue("$scene 缺少话术编排器", "guide_replies.md" in files)
            assertTrue("$scene 缺少读屏证据规则", "guide_ocr.md" in files)
        }
    }

    @Test fun coreSectionsDropQuestionnaireAndMemory() {
        val skill = "## 核心原则\n甲\n## 首次使用与紧急例外\n先发问卷\n" +
            "## 长期记忆\n记住档案\n## 每次分析\n乙\n## 安全边界\n丙"
        val kept = SkillBundle.coreSections(skill)
        assertTrue("核心原则" in kept)
        assertTrue("每次分析" in kept)
        assertTrue("安全边界" in kept)
        assertFalse("先发问卷" in kept)
        assertFalse("记住档案" in kept)
    }

    @Test fun budgetKeepsWholeLines() {
        val line = "x".repeat(150)
        assertEquals(line, SkillBundle.trim(line + "\n" + "y".repeat(150), 200))
        assertEquals("短文本", SkillBundle.trim("短文本", 100))
    }

    @Test fun scenesRouteToDifferentKnowledge() {
        assertNotEquals(SkillBundle.filesFor("主动邀约"), SkillBundle.filesFor("修复冲突"))
        assertEquals(SkillBundle.filesFor("修复冲突"), SkillBundle.filesFor("澄清关系"))
    }
}
