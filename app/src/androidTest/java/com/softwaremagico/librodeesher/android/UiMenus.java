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
        openOverflow();
        selectVisible(label(title));
    }

    /** Opens the overflow menu, selects a submenu and then one of its items. */
    public static void clickNested(int parentTitle, int childTitle) {
        openOverflow();
        selectVisible(label(parentTitle));
        selectVisible(label(childTitle));
    }

    private static String label(int title) {
        return ApplicationProvider.getApplicationContext().getString(title);
    }

    private static void openOverflow() {
        final String overflowDescription = ApplicationProvider.getApplicationContext().getString(
                androidx.appcompat.R.string.abc_action_menu_overflow_description);
        final UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        boolean opened = false;
        final long openDeadline = SystemClock.uptimeMillis() + 10_000;
        do {
            final AccessibilityNodeInfo initial = automation.getRootInActiveWindow();
            if (initial != null && clickOverflow(initial, overflowDescription)) {
                opened = true;
                break;
            }
            SystemClock.sleep(200);
        } while (SystemClock.uptimeMillis() < openDeadline);
        if (!opened) {
            throw new AssertionError("Overflow menu button not visible");
        }
    }

    private static void selectVisible(String text) {
        final UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
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
