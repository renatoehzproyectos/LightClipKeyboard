package rkr.simplekeyboard.inputmethod.diagnostic;

import android.app.Application;

public final class DiagnosticApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashRecorder.install(this);
    }
}