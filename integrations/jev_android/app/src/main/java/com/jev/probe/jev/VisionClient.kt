package com.jev.probe.jev

import android.graphics.Bitmap
import android.util.Base64
import com.jev.probe.core.Prefs
import java.io.ByteArrayOutputStream
import org.json.JSONArray
import org.json.JSONObject

/**
 * The vision route: an OpenAI-compatible `/chat/completions` endpoint that
 * accepts `image_url` content parts. The screenshot pipeline uses it when cloud OCR is selected.
 *
 * Reads visionBaseUrl / visionKey / visionModel from [Prefs]. The base URL does
 * not inherit from the reply route; the user chooses the host that receives
 * the screenshot. Keys are reused only for the same API origin.
 *
 * Wire format notes that cost real debugging time:
 * - JPEG, not PNG: a screenshot as PNG base64 is several times larger.
 * - `Base64.NO_WRAP`: Android's default inserts newlines, which corrupts the
 *   data URL.
 * - The image part goes BEFORE the text part — DashScope's compatible-mode
 *   rejects the other order.
 */
class VisionClient(private val prefs: Prefs) {

    /**
     * Send a screenshot and get the transcribed dialog back as plain text.
     * The caller parses this into bubbles and asks the user to review them.
     *
     * @param imageBase64Jpeg base64 of a JPEG, without the `data:` prefix.
     */
    fun extractDialog(imageBase64Jpeg: String): String = ask(
        imageBase64Jpeg,
        "你是聊天截图转写助手。把图中聊天气泡按从上到下的顺序转写成文本，" +
            "每行一条，格式严格为 `我：正文` 或 `对方：正文`，不要输出任何解释。\n" +
            "说话人身份只按气泡位置判断，不要按文字内容猜测：" +
            "靠屏幕右侧、头像在右侧的气泡是「我」；靠左侧、头像在左侧的气泡是「对方」。" +
            "群聊里除「我」之外的所有人一律记为「对方」。\n" +
            "忽略时间戳、系统提示、昵称、输入框、已读回执和表情说明，只转写气泡正文。" +
            "如果某条实在分不清左右，写「对方：」。"
    )

    /** Generic single-question call against the image (used by the settings test). */
    fun ask(imageBase64Jpeg: String, prompt: String): String {
        val url = prefs.visionEndpoint()
        val image = JSONObject().put("type", "image_url")
            .put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$imageBase64Jpeg"))
        val text = JSONObject().put("type", "text").put("text", prompt)
        // DashScope requires image first; DeepSeek's documented format puts text first.
        val content = if (prefs.visionBaseUrl.trim().trimEnd('/') == Prefs.DEEPSEEK_BASE)
            JSONArray().put(text).put(image) else JSONArray().put(image).put(text)
        val messages = JSONArray().put(
            JSONObject().put("role", "user").put("content", content))
        val body = JSONObject()
            .put("model", prefs.visionModel)
            .put("messages", messages)
            .put("temperature", 0.0)
        val resp = HttpJson.post(url, prefs.effectiveVisionKey(), body, Route.VISION, HttpJson.headersFor(url))
        return resp.optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content") ?: ""
    }

    companion object {
        /** Bitmap -> JPEG base64 in the exact form [ask] expects. */
        fun encodeJpeg(bitmap: Bitmap, quality: Int = 80): String {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        }

        /** DeepSeek Flash accepts image_url; custom hosts are checked by the request itself. */
        fun supportsVision(baseUrl: String): Boolean = baseUrl.trim().isNotEmpty()
    }
}
