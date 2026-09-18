package rkr.simplekeyboard.inputmethod.translator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Lazy-loaded translation panel.
 * Instantiates the provider only when first used.
 */
public class TranslatorView extends FrameLayout {

    public interface Listener {
        void onInsertTranslation(String text);
        void onClose();
    }

    private Listener mListener;
    private Translator mTranslator;
    private EditText mInput;
    private TextView mOutput;
    private Spinner mFrom, mTo;
    private Button mTranslateBtn;

    public TranslatorView(Context context) {
        super(context);
        init(context);
    }

    public TranslatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        final rkr.simplekeyboard.inputmethod.keyboard.PanelTheme theme =
                rkr.simplekeyboard.inputmethod.keyboard.PanelTheme.resolve(context);
        theme.applyPanelBackground(this);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        final int pad = theme.dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(context);
        title.setText("Translate");
        title.setTextColor(theme.onSurface);
        title.setTextSize(18);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, theme.dp(8));
        root.addView(title);

        mInput = new EditText(context);
        mInput.setHint("Text to translate…");
        mInput.setTextColor(theme.onSurface);
        mInput.setHintTextColor(theme.onSurfaceVariant);
        mInput.setMinLines(3);
        mInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        mInput.setBackground(theme.outlinedRoundRect(theme.surface, 12f));
        mInput.setPadding(theme.dp(12), theme.dp(10), theme.dp(12), theme.dp(10));
        root.addView(mInput);

        LinearLayout langs = new LinearLayout(context);
        langs.setOrientation(LinearLayout.HORIZONTAL);
        mFrom = new Spinner(context);
        mTo = new Spinner(context);
        String[] codes = new LibreTranslateProvider().getSupportedLanguages();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, codes);
        mFrom.setAdapter(adapter);
        mTo.setAdapter(adapter);
        // default en -> es
        mFrom.setSelection(0);
        mTo.setSelection(1);
        langs.addView(mFrom);
        TextView arrow = new TextView(context);
        arrow.setText(" → ");
        arrow.setTextColor(theme.onSurface);
        langs.addView(arrow);
        langs.addView(mTo);
        root.addView(langs);

        mTranslateBtn = new Button(context);
        mTranslateBtn.setText("Translate");
        mTranslateBtn.setAllCaps(false);
        mTranslateBtn.setTextColor(theme.onAccent);
        mTranslateBtn.setBackground(theme.pressable(theme.accent, theme.accentPressed, 14f));
        mTranslateBtn.setOnClickListener(v -> doTranslate());
        root.addView(mTranslateBtn);

        mOutput = new TextView(context);
        mOutput.setTextColor(theme.accent);
        mOutput.setTextSize(16);
        mOutput.setPadding(0, 12, 0, 12);
        mOutput.setMinHeight(80);
        root.addView(mOutput);

        Button insert = new Button(context);
        insert.setText("Insert result");
        insert.setAllCaps(false);
        insert.setTextColor(theme.onSurface);
        insert.setBackground(theme.pressable(theme.surface, theme.surfacePressed, 14f));
        insert.setOnClickListener(v -> {
            CharSequence t = mOutput.getText();
            if (t != null && t.length() > 0 && mListener != null) {
                mListener.onInsertTranslation(t.toString());
            }
        });
        root.addView(insert);

        Button close = new Button(context);
        close.setText("Close");
        close.setAllCaps(false);
        close.setTextColor(theme.onSurfaceVariant);
        close.setBackground(theme.pressable(theme.functionalSurface, theme.surfacePressed, 14f));
        close.setOnClickListener(v -> {
            if (mListener != null) mListener.onClose();
        });
        root.addView(close);

        addView(root);
    }

    public void setListener(Listener l) { mListener = l; }

    private void ensureTranslator() {
        if (mTranslator == null) {
            mTranslator = new LibreTranslateProvider();
        }
    }

    private void doTranslate() {
        String text = mInput.getText() != null ? mInput.getText().toString() : "";
        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Enter text first", Toast.LENGTH_SHORT).show();
            return;
        }
        ensureTranslator();
        mTranslateBtn.setEnabled(false);
        mOutput.setText("Translating…");
        String from = (String) mFrom.getSelectedItem();
        String to = (String) mTo.getSelectedItem();
        mTranslator.translate(text, from, to, new Translator.Callback() {
            @Override
            public void onSuccess(String translated) {
                mOutput.setText(translated);
                mTranslateBtn.setEnabled(true);
            }
            @Override
            public void onError(String message) {
                mOutput.setText("Error: " + message);
                mTranslateBtn.setEnabled(true);
            }
        });
    }
}
