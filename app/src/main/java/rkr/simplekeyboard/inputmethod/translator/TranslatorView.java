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
        root.setPadding(16, 16, 16, 16);

        TextView title = new TextView(context);
        title.setText("🌐 Translate");
        title.setTextColor(theme.onSurface);
        title.setTextSize(18);
        root.addView(title);

        mInput = new EditText(context);
        mInput.setHint("Text to translate…");
        mInput.setTextColor(theme.onSurface);
        mInput.setHintTextColor(theme.onSurfaceVariant);
        mInput.setMinLines(3);
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
        insert.setOnClickListener(v -> {
            CharSequence t = mOutput.getText();
            if (t != null && t.length() > 0 && mListener != null) {
                mListener.onInsertTranslation(t.toString());
            }
        });
        root.addView(insert);

        Button close = new Button(context);
        close.setText("✕ Close");
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
