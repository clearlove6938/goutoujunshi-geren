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

    @Test fun alwaysOnParksTheBubbleEvenOnNormallyDroppedScreens() {
        for (pkg in listOf("com.jev.probe", "com.tencent.mm", "com.miui.home",
                "com.android.systemui", "com.oppo.launcher")) {
            assertTrue("expected a bubble on " + pkg,
                shouldParkIdleBubble(pkg, "com.jev.probe", "com.tencent.mm",
                    bubbleShowing = false, alwaysOn = true))
        }
    }

    @Test fun alwaysOnStillNeedsAPackageAndNeverReplacesAShowingBubble() {
        assertFalse(shouldParkIdleBubble(null, "com.jev.probe", "com.tencent.mm",
            bubbleShowing = false, alwaysOn = true))
        assertFalse(shouldParkIdleBubble("com.jev.probe", "com.jev.probe", "com.tencent.mm",
            bubbleShowing = true, alwaysOn = true))
    }

    @Test fun defaultModeStillDropsOurOwnScreens() {
        assertFalse(shouldParkIdleBubble("com.jev.probe", "com.jev.probe",
            "com.tencent.mm", bubbleShowing = false))
        assertFalse(shouldParkIdleBubble("com.miui.home", "com.jev.probe",
            "com.tencent.mm", bubbleShowing = false))
    }

    // 1080px screen: left gutter ~140, right gutter ~940, so a short outgoing
    // line hugs the right edge while its left edge is far from the left gutter.
    @Test fun rightHuggingOcrLineIsMine() {
        assertTrue(ocrLineIsMine(700, 1030, 1080))
    }

    @Test fun incomingAndAmbiguousLongBubblesStayOther() {
        assertFalse(ocrLineIsMine(160, 500, 1080))   // ordinary incoming line
        assertFalse(ocrLineIsMine(160, 900, 1080))   // long incoming line must NOT flip to me
        assertFalse(ocrLineIsMine(200, 1030, 1080))  // full-width line is ambiguous
    }

    @Test fun centredSystemTextIsNotMine() {
        assertFalse(ocrLineIsMine(480, 600, 1080))
    }

    @Test fun unknownScreenWidthNeverClaimsMine() {
        assertFalse(ocrLineIsMine(700, 1030, 0))
        assertFalse(ocrLineIsMine(700, 1030, -1))
    }
}
