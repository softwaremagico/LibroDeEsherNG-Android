package com.softwaremagico.librodeesher.android;

import android.util.Log;

/**
 * Thin logging facade over {@link android.util.Log}.
 *
 * <p>The rules engine logs through SLF4J (no binding is shipped, so those calls become no-ops),
 * while the Android side logs through this class so every message ends up under a single tag.</p>
 */
public final class AppLog {

    private static final String TAG = "LibroDeEsher";

    private AppLog() {
        // Utility class.
    }

    public static void debug(String message) {
        Log.d(TAG, message);
    }

    public static void info(String message) {
        Log.i(TAG, message);
    }

    public static void warn(String message) {
        Log.w(TAG, message);
    }

    public static void error(String message) {
        Log.e(TAG, message);
    }

    public static void error(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }
}
