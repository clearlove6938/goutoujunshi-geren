package com.jev.probe.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatCaptureServiceTest {
    @Test fun accessibilityEventsCannotReplacePendingReview() {
        assertTrue(shouldIgnoreAccessibilityEvents(reviewPending = true))
    }

    @Test fun accessibilityEventsResumeAfterReview() {
        assertFalse(shouldIgnoreAccessibilityEvents(reviewPending = false))
    }

    @Test fun confirmationWaitsForOverlayFocusToReturnToChat() {
        assertEquals(ReviewConfirmationState.WAIT_FOR_CHAT_WINDOW,
            reviewConfirmationState("com.goutoujunshi.chat",
                "com.goutoujunshi.chat", "cn.soulapp.android"))
        assertEquals(ReviewConfirmationState.WAIT_FOR_CHAT_WINDOW,
            reviewConfirmationState(null,
                "com.goutoujunshi.chat", "cn.soulapp.android"))
    }

    @Test fun confirmationAcceptsOnlyTheOriginalChatApp() {
        assertEquals(ReviewConfirmationState.CHAT_CURRENT,
            reviewConfirmationState("cn.soulapp.android",
                "com.goutoujunshi.chat", "cn.soulapp.android"))
        assertEquals(ReviewConfirmationState.CHAT_CHANGED,
            reviewConfirmationState("com.tencent.mobileqq",
                "com.goutoujunshi.chat", "cn.soulapp.android"))
    }

    @Test fun adaptedChatAppsKeepABubbleWhenTheirNodeTreeIsUnreadable() {
        // QQ/X/Feishu are adapted apps. Their extract() can return null after
        // an app update renames the hard-coded view ids. The bubble menu's
        // "截屏识别一次" is the only way in, so it must stay tappable.
        assertTrue(shouldParkIdleBubble("com.tencent.mobileqq",
            "com.jev.probe", "com.tencent.mm", bubbleShowing = false))
        assertTrue(shouldParkIdleBubble("com.ss.android.lark",
            "com.jev.probe", "com.tencent.mm", bubbleShowing = false))
        assertTrue(shouldParkIdleBubble("com.twitter.android",
            "com.jev.probe", "com.tencent.mm", bubbleShowing = false))
    }

    @Test fun noIdleBubbleOnOurOwnScreensWeChatOrLauncher() {
        assertFalse(shouldParkIdleBubble("com.jev.probe",
            "com.jev.probe", "com.tencent.mm", false))
        assertFalse(shouldParkIdleBubble("com.tencent.mm",
            "com.jev.probe", "com.tencent.mm", false))
        assertFalse(shouldParkIdleBubble("com.miui.home",
            "com.jev.probe", "com.tencent.mm", false))
        assertFalse(shouldParkIdleBubble("com.android.systemui",
            "com.jev.probe", "com.tencent.mm", false))
        assertFalse(shouldParkIdleBubble("com.oppo.launcher",
            "com.jev.probe", "com.tencent.mm", false))
        assertFalse(shouldParkIdleBubble(null,
            "com.jev.probe", "com.tencent.mm", false))
    }

    @Test fun aShowingBubbleIsNeverReplacedWithIdle() {
        assertFalse(shouldParkIdleBubble("com.tencent.mobileqq",
            "com.jev.probe", "com.tencent.mm", bubbleShowing = true))
    }
}
