package com.softwaremagico.librodeesher.android;

import android.app.UiAutomation;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

/** Opens and selects real overflow-menu items without waiting for Espresso's popup idle state. */
public final class UiMenus {
    private UiMenus() {
    }

    public static void click(int title) {
        final String text = ApplicationProvider.getApplicationContext().getString(title);
        final String overflowDescription = ApplicationProvider.getApplicationContext().getString(
                androidx.appcompat.R.string.abc_action_menu_overflow_description);
        final UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        final AccessibilityNodeInfo initial = automation.getRootInActiveWindow();
        if (initial == null || !clickOverflow(initial, overflowDescription)) {
            throw new AssertionError("Overflow menu button not visible");
        }
        final long deadline = SystemClock.uptimeMillis() + 10_000;
        do {
            final AccessibilityNodeInfo root = automation.getRootInActiveWindow();
            if (root != null) {
                for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(text)) {
                    if (text.contentEquals(node.getText()) && node.isVisibleToUser()) {
                        for (AccessibilityNodeInfo target = node; target != null; target = target.getParent()) {
                            if (target.isClickable() && target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                return;
                            }
                        }
                    }
                }
            }
            SystemClock.sleep(200);
        } while (SystemClock.uptimeMillis() < deadline);
        throw new AssertionError("Overflow menu item not visible: " + text);
    }

    private static boolean clickOverflow(AccessibilityNodeInfo node, String description) {
        final CharSequence className = node.getClassName();
        final CharSequence contentDescription = node.getContentDescription();
        if ((description.contentEquals(contentDescription == null ? "" : contentDescription)
                || (className != null && className.toString().endsWith("OverflowMenuButton")))
                && node.isVisibleToUser() && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            final AccessibilityNodeInfo child = node.getChild(i);
            if (child != null && clickOverflow(child, description)) {
                return true;
            }
        }
        return false;
    }
}
