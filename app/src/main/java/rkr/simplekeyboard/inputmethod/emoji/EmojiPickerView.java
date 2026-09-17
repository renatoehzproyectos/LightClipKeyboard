package rkr.simplekeyboard.inputmethod.emoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lightweight emoji picker. Uses system emoji characters only (no asset pack).
 * Lazy: create only when the user opens the emoji panel.
 */
public class EmojiPickerView extends FrameLayout {

    public interface Listener {
        void onEmojiSelected(String emoji);
        void onClose();
    }

    private static final String PREFS = "emoji_recent";
    private static final String KEY_RECENT = "recent";
    private static final int MAX_RECENT = 24;

    private Listener mListener;
    private final SharedPreferences mPrefs;
    private GridLayout mGrid;
    private final List<String> mRecent = new ArrayList<>();

    // Minimal category set — system font renders them
    private static final String[][] CATEGORIES = {
            {"😀","Recent", "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃","😉","😊","😇","🥰","😍","🤩","😘","😗","😚","😙","🥲","😋"},
            {"👋","People", "👋","🤚","🖐","✋","🖖","👌","🤌","🤏","✌️","🤞","🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝️","👍","👎","✊"},
            {"🐶","Animals", "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🙈","🙉","🙊","🐒","🐔","🐧"},
            {"🍎","Food", "🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒"},
            {"⚽","Activity", "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍","🏏","🪃","🥅","⛳","🪁","🏹"},
            {"🚗","Travel", "🚗","🚕","🚙","🚌","🚎","🏎","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🦯","🦽","🦼","🛴","🚲","🛵","🏍"},
            {"💡","Objects", "💡","🔦","🏮","🪔","📔","📕","📖","📗","📘","📙","📚","📓","📒","📃","📜","📄","📰","🗞","📑","🔖","🏷"},
            {"❤️","Symbols", "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗","💖","💘","💝","💟","☮️","✝️"},
            {"🏳️","Flags", "🏳️","🏴","🏁","🚩","🏳️‍🌈","🏳️‍⚧️","🇺🇳","🇦🇫","🇦🇽","🇦🇱","🇩🇿","🇦🇸","🇦🇩","🇦🇴","🇦🇮","🇦🇶","🇦🇬","🇦🇷","🇦🇲","🇦🇼"}
    };

    public EmojiPickerView(Context context) {
        super(context);
        mPrefs = prefsContext(context).getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        init(context);
    }

    public EmojiPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPrefs = prefsContext(context).getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        init(context);
    }

    private static Context prefsContext(final Context context) {
        // Direct-boot safe: credential protected storage is unavailable before first unlock.
        try {
            final Context deviceContext = context.createDeviceProtectedStorageContext();
            if (deviceContext != null) {
                return deviceContext;
            }
        } catch (Exception ignored) {
        }
        return context;
    }

    private void init(Context context) {
        setBackgroundColor(0xFF1A1A1A);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(8, 8, 8, 8);

        // Category tabs
        HorizontalScrollView tabScroll = new HorizontalScrollView(context);
        LinearLayout tabs = new LinearLayout(context);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int idx = i;
            Button b = new Button(context);
            b.setText(CATEGORIES[i][0]);
            b.setTextSize(18);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setPadding(16, 8, 16, 8);
            b.setOnClickListener(v -> showCategory(idx));
            tabs.addView(b);
        }
        tabScroll.addView(tabs);
        root.addView(tabScroll);

        // Close
        Button close = new Button(context);
        close.setText("✕ Close");
        close.setOnClickListener(v -> { if (mListener != null) mListener.onClose(); });
        root.addView(close);

        mGrid = new GridLayout(context);
        mGrid.setColumnCount(8);
        root.addView(mGrid);

        addView(root);
        loadRecent();
        showCategory(0);
    }

    public void setListener(Listener l) { mListener = l; }

    private void loadRecent() {
        String raw = mPrefs.getString(KEY_RECENT, "");
        mRecent.clear();
        if (!raw.isEmpty()) {
            mRecent.addAll(Arrays.asList(raw.split(",")));
        }
    }

    private void saveRecent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mRecent.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(mRecent.get(i));
        }
        mPrefs.edit().putString(KEY_RECENT, sb.toString()).apply();
    }

    private void showCategory(int idx) {
        mGrid.removeAllViews();
        List<String> emojis = new ArrayList<>();
        if (idx == 0) {
            emojis.addAll(mRecent);
            // fill with defaults if empty
            if (emojis.isEmpty()) {
                for (int i = 2; i < CATEGORIES[0].length; i++) emojis.add(CATEGORIES[0][i]);
            }
        } else {
            for (int i = 2; i < CATEGORIES[idx].length; i++) {
                emojis.add(CATEGORIES[idx][i]);
            }
        }
        for (String e : emojis) {
            TextView tv = new TextView(getContext());
            tv.setText(e);
            tv.setTextSize(28);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(12, 12, 12, 12);
            tv.setOnClickListener(v -> {
                if (mListener != null) mListener.onEmojiSelected(e);
                // update recent
                mRecent.remove(e);
                mRecent.add(0, e);
                while (mRecent.size() > MAX_RECENT) mRecent.remove(mRecent.size() - 1);
                saveRecent();
            });
            mGrid.addView(tv);
        }
    }
}
