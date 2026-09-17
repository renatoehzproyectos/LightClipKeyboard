package rkr.simplekeyboard.inputmethod.diagnostic;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import rkr.simplekeyboard.inputmethod.latin.settings.SettingsActivity;

public final class DiagnosticActivity extends Activity {
    private static final int PAD = 24;

    @Override
    protected void onCreate(final Bundle state) {
        super.onCreate(state);
        setTitle("LightClip Keyboard diagnostics");
        showReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showReport();
    }

    private void showReport() {
        final int padding = Math.round(PAD * getResources().getDisplayMetrics().density);
        final LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(padding, padding, padding, padding);

        final TextView heading = new TextView(this);
        heading.setText("LightClip Keyboard diagnostic build");
        heading.setTextSize(22);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(heading);

        final String report = CrashRecorder.read(this);
        final TextView message = new TextView(this);
        message.setText(report == null
                ? "No startup crash has been recorded yet. Tap Open original settings. "
                        + "If it closes, open LightClip Keyboard again to see the exact error."
                : report);
        message.setTextIsSelectable(true);
        message.setPadding(0, padding, 0, padding);
        content.addView(message);

        if (report != null) {
            addButton(content, "Copy crash report", () -> copyReport(report));
            addButton(content, "Clear report", () -> {
                CrashRecorder.clear(this);
                showReport();
            });
        }

        addButton(content, "Open original settings", () ->
                startActivity(new Intent(this, SettingsActivity.class)));
        addButton(content, "Open Android keyboard settings", () ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));

        final ScrollView scroll = new ScrollView(this);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
    }

    private void addButton(final LinearLayout parent, final String label,
            final Runnable action) {
        final Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(view -> action.run());
        parent.addView(button, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void copyReport(final String report) {
        final ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("LightClip crash report", report));
        Toast.makeText(this, "Crash report copied", Toast.LENGTH_LONG).show();
    }
}