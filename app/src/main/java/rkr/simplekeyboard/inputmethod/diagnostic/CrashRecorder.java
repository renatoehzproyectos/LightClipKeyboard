package rkr.simplekeyboard.inputmethod.diagnostic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

public final class CrashRecorder {
    private static final String PREFS = "lightclip_diagnostics";
    private static final String LAST_CRASH = "last_crash";

    private CrashRecorder() {}

    private static SharedPreferences preferences(final Context context) {
        final Context appContext = context.getApplicationContext();
        final Context deviceContext = appContext.createDeviceProtectedStorageContext();
        return deviceContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void install(final Context context) {
        final Thread.UncaughtExceptionHandler previous =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            save(context, thread, throwable);
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            }
        });
    }

    private static void save(final Context context, final Thread thread,
            final Throwable throwable) {
        try {
            final StringWriter trace = new StringWriter();
            throwable.printStackTrace(new PrintWriter(trace));
            final String report =
                    "LightClip Keyboard diagnostic report\n"
                    + "Time: " + DateFormat.getDateTimeInstance().format(new Date()) + "\n"
                    + "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                    + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                    + "Thread: " + thread.getName() + "\n\n"
                    + trace;
            preferences(context).edit().putString(LAST_CRASH, report).commit();
        } catch (Throwable ignored) {
            // The recorder must never replace the original crash.
        }
    }

    public static String read(final Context context) {
        return preferences(context).getString(LAST_CRASH, null);
    }

    public static void clear(final Context context) {
        preferences(context).edit().remove(LAST_CRASH).apply();
    }
}