package com.jev.probe.core

import android.content.Context

/**
 * Optional full upstream goutoujunshi Skill, bundled into the APK as assets.
 *
 * Off by default: enabling it adds several thousand characters to every
 * drafting and detail-analysis call. Only the generative layers read it.
 * JevQuestions stays untouched on purpose - its English question wording was
 * calibrated and must not be diluted with a long Chinese rule block.
 *
 * The whole 20 + 23 document library ships in the APK, but one round loads
 * only the Skill's core sections plus 2-3 scene guides, under [BUDGET_CHARS].
 */
object SkillBundle {

    const val BUDGET_CHARS = 6000
    private const val DIR = "goutou_skill"

    private val CORE_SECTIONS = listOf("核心原则", "输入证据边界", "每次分析", "安全边界")

    private val MOBILE_NOTE = """
        ## 移动端运行时适配（优先于上方技能原文）
        上方规则来自本地 Codex Skill 原版，其中「不写用户工作区 / 数据不外发 / 不发起网络请求」描述的是 Skill 自身，不是本 App；截图识别、密钥读取、模型调用由 App 完成，你只负责分析与生成文本。
        本轮输出必须遵守下方的 JSON 输出契约，它覆盖技能原文的排版与首轮问卷流程：不建立档案、不发问卷，缺失背景保持未知。
        截图识别的文字保留错字、截断与说话人不确定性；左右布局不等于身份确认，以用户核对后的原文为准。
    """.trimIndent()

    /**
     * Scene comes from the contact goal in the knowledge base. Unknown or blank
     * goals fall back to everyday replies. Every scene includes the shared
     * phrasing guide and the OCR evidence rules.
     */
    fun filesFor(scene: String?): List<String> = when (scene) {
        "主动邀约" -> listOf("guide_replies.md", "guide_invite.md", "guide_ocr.md")
        "澄清关系", "修复冲突" -> listOf("guide_replies.md", "guide_conflict.md", "guide_ocr.md")
        "减少投入", "结束联系" -> listOf("guide_replies.md", "guide_withdraw.md", "guide_ocr.md")
        else -> listOf("guide_replies.md", "guide_online.md", "guide_ocr.md")
    }

    /** Keep whole top-level sections; drop the questionnaire and memory chapters. */
    fun coreSections(skill: String, wanted: List<String> = CORE_SECTIONS): String {
        val out = StringBuilder()
        var keep = false
        for (line in skill.lines()) {
            if (line.startsWith("## ")) keep = wanted.any { line.contains(it) }
            if (keep) out.append(line).append('\n')
        }
        return out.toString().trim()
    }

    /** Never cut a document mid-line when the character budget is reached. */
    fun trim(text: String, limit: Int = BUDGET_CHARS): String {
        if (text.length <= limit) return text
        val cut = text.substring(0, limit)
        val lastBreak = cut.lastIndexOf('\n')
        return if (lastBreak > limit / 2) cut.substring(0, lastBreak) else cut
    }

    private fun asset(context: Context, name: String): String = try {
        context.assets.open("$DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (e: Exception) {
        ""
    }

    /** Empty when assets are missing, so callers silently keep the compact prompt. */
    fun load(context: Context, scene: String?): String {
        val skill = asset(context, "SKILL.md")
        if (skill.isBlank()) return ""
        val parts = ArrayList<String>()
        parts.add(MOBILE_NOTE)
        val core = coreSections(skill)
        if (core.isNotBlank()) parts.add(core)
        filesFor(scene).forEach { name ->
            val text = asset(context, name)
            if (text.isNotBlank()) parts.add(text)
        }
        return trim(parts.joinToString("\n\n"))
    }
}
