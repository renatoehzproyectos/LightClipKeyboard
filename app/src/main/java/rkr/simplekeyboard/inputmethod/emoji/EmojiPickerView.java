/*
 * Copyright (C) 2026 LightClipKeyboard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.emoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rkr.simplekeyboard.inputmethod.keyboard.PanelBottomBar;
import rkr.simplekeyboard.inputmethod.keyboard.PanelTheme;

/**
 * Gboard-style emoji panel: section title, scrollable emoji grid, and a bottom bar with
 * ABC / category tabs / backspace. Colors follow the active keyboard theme.
 */
public class EmojiPickerView extends FrameLayout {

    public interface Listener {
        void onEmojiSelected(String emoji);
        void onBackspace();
        void onClose();
    }

    private static final String PREFS = "emoji_recent";
    private static final String KEY_RECENT = "recent";
    private static final int MAX_RECENT = 32;
    private static final int COLUMNS = 8;

    private Listener mListener;
    private final SharedPreferences mPrefs;
    private PanelTheme mTheme;
    private GridLayout mGrid;
    private ScrollView mScroll;
    private TextView mTitle;
    private final List<TextView> mTabs = new ArrayList<>();
    private int mCategory = 0;
    private final List<String> mRecent = new ArrayList<>();

    private static final String[] TITLES = {
            "Recently used", "Smileys & emotion", "People & body", "Animals & nature",
            "Food & drink", "Activities", "Travel & places", "Objects", "Symbols", "Flags" };

    private static final String[] TAB_ICONS = {
            "🕒", "😀", "👋", "🐻", "🍔", "⚽", "🚗", "💡", "🔣", "🏁" };

    private static final String[][] CATEGORIES = {
            {}, // recent, filled at runtime
            {"😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃","😉","😊","😇","🥰","😍","🤩","😘","😗","😚","😙","🥲","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🤫","🤔","🤐","🤨","😐","😑","😶","😏","😒","🙄","😬","🤥","😌","😔","😪","🤤","😴","😷","🤒","🤕","🤢","🤮","🤧","🥵","🥶","🥴","😵","🤯","🤠","🥳","🥸","😎","🤓","🧐","😕","😟","🙁","☹️","😮","😯","😲","😳","🥺","😦","😧","😨","😰","😥","😢","😭","😱","😖","😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬","😈","👿","💀","☠️","💩","🤡","👹","👺","👻","👽","👾","🤖","😺","😸","😹","😻","😼","😽","🙀","😿","😾"},
            {"👋","🤚","🖐","✋","🖖","👌","🤌","🤏","✌️","🤞","🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝️","👍","👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝","🙏","✍️","💅","🤳","💪","🦾","🦵","🦶","👂","🦻","👃","🧠","🫀","🫁","🦷","🦴","👀","👁","👅","👄","👶","🧒","👦","👧","🧑","👱","👨","🧔","👩","🧓","👴","👵","🙍","🙎","🙅","🙆","💁","🙋","🧏","🙇","🤦","🤷","👮","🕵️","💂","🥷","👷","🤴","👸","👳","👲","🧕","🤵","👰","🤰","🤱","👼","🎅","🤶","🦸","🦹","🧙","🧚","🧛","🧜","🧝","🧞","🧟","💆","💇","🚶","🧍","🧎","🏃","💃","🕺","👯","🧖","🧗","🤺","🏇","⛷","🏂","🏌️","🏄","🚣","🏊","⛹️","🏋️","🚴","🚵","🤸","🤼","🤽","🤾","🤹","🧘","🛀","🛌","👭","👫","👬","💏","💑","👪"},
            {"🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐽","🐸","🐵","🙈","🙉","🙊","🐒","🐔","🐧","🐦","🐤","🐣","🐥","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🪱","🐛","🦋","🐌","🐞","🐜","🪰","🪲","🪳","🦟","🦗","🕷","🕸","🦂","🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦐","🦞","🦀","🐡","🐠","🐟","🐬","🐳","🐋","🦈","🐊","🐅","🐆","🦓","🦍","🦧","🦣","🐘","🦛","🦏","🐪","🐫","🦒","🦘","🦬","🐃","🐂","🐄","🐎","🐖","🐏","🐑","🦙","🐐","🦌","🐕","🐩","🦮","🐕‍🦺","🐈","🐈‍⬛","🪶","🐓","🦃","🦤","🦚","🦜","🦢","🦩","🕊","🐇","🦝","🦨","🦡","🦫","🦦","🦥","🐁","🐀","🐿","🦔","🐾","🐉","🐲","🌵","🎄","🌲","🌳","🌴","🪵","🌱","🌿","☘️","🍀","🎍","🪴","🎋","🍃","🍂","🍁","🍄","🐚","🪨","🌾","💐","🌷","🌹","🥀","🌺","🌸","🌼","🌻","🌞","🌝","🌛","🌜","🌚","🌕","🌖","🌗","🌘","🌑","🌒","🌓","🌔","🌙","🌎","🌍","🌏","🪐","💫","⭐","🌟","✨","⚡","☄️","💥","🔥","🌪","🌈","☀️","🌤","⛅","🌥","☁️","🌦","🌧","⛈","🌩","🌨","❄️","☃️","⛄","🌬","💨","💧","💦","☔","☂️","🌊","🌫"},
            {"🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒","🌶","🫑","🌽","🥕","🫒","🧄","🧅","🥔","🍠","🥐","🥯","🍞","🥖","🥨","🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩","🍗","🍖","🦴","🌭","🍔","🍟","🍕","🫓","🥪","🥙","🧆","🌮","🌯","🫔","🥗","🥘","🫕","🥫","🍝","🍜","🍲","🍛","🍣","🍱","🥟","🦪","🍤","🍙","🍚","🍘","🍥","🥠","🥮","🍢","🍡","🍧","🍨","🍦","🥧","🧁","🍰","🎂","🍮","🍭","🍬","🍫","🍿","🍩","🍪","🌰","🥜","🍯","🥛","🍼","🫖","☕","🍵","🧃","🥤","🧋","🍶","🍺","🍻","🥂","🍷","🥃","🍸","🍹","🧉","🍾","🧊","🥄","🍴","🍽","🥣","🥡","🥢","🧂"},
            {"⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍","🏏","🪃","🥅","⛳","🪁","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷","⛸","🥌","🎿","⛷","🏂","🪂","🏋️","🤼","🤸","⛹️","🤺","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚣","🧗","🚵","🚴","🏆","🥇","🥈","🥉","🏅","🎖","🏵","🎗","🎫","🎟","🎪","🤹","🎭","🩰","🎨","🎬","🎤","🎧","🎼","🎹","🥁","🪘","🎷","🎺","🪗","🎸","🪕","🎻","🎲","♟","🎯","🎳","🎮","🎰","🧩"},
            {"🚗","🚕","🚙","🚌","🚎","🏎","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🦯","🦽","🦼","🛴","🚲","🛵","🏍","🛺","🚨","🚔","🚍","🚘","🚖","🚡","🚠","🚟","🚃","🚋","🚞","🚝","🚄","🚅","🚈","🚂","🚆","🚇","🚊","🚉","✈️","🛫","🛬","🛩","💺","🛰","🚀","🛸","🚁","🛶","⛵","🚤","🛥","🛳","⛴","🚢","⚓","🪝","⛽","🚧","🚦","🚥","🚏","🗺","🗿","🗽","🗼","🏰","🏯","🏟","🎡","🎢","🎠","⛲","⛱","🏖","🏝","🏜","🌋","⛰","🏔","🗻","🏕","⛺","🛖","🏠","🏡","🏘","🏚","🏗","🏭","🏢","🏬","🏣","🏤","🏥","🏦","🏨","🏪","🏫","🏩","💒","🏛","⛪","🕌","🕍","🛕","🕋","⛩","🛤","🛣","🗾","🎑","🏞","🌅","🌄","🌠","🎇","🎆","🌇","🌆","🏙","🌃","🌌","🌉","🌁"},
            {"⌚","📱","📲","💻","⌨️","🖥","🖨","🖱","🖲","🕹","🗜","💽","💾","💿","📀","📼","📷","📸","📹","🎥","📽","🎞","📞","☎️","📟","📠","📺","📻","🎙","🎚","🎛","🧭","⏱","⏲","⏰","🕰","⌛","⏳","📡","🔋","🔌","💡","🔦","🕯","🪔","🧯","🛢","💸","💵","💴","💶","💷","🪙","💰","💳","💎","⚖️","🪜","🧰","🪛","🔧","🔨","⚒","🛠","⛏","🪚","🔩","⚙️","🪤","🧱","⛓","🧲","🔫","💣","🧨","🪓","🔪","🗡","⚔️","🛡","🚬","⚰️","🪦","⚱️","🏺","🔮","📿","🧿","💈","⚗️","🔭","🔬","🕳","🩹","🩺","💊","💉","🩸","🧬","🦠","🧫","🧪","🌡","🧹","🪠","🧺","🧻","🚽","🚰","🚿","🛁","🛀","🧼","🪥","🪒","🧽","🪣","🧴","🛎","🔑","🗝","🚪","🪑","🛋","🛏","🛌","🧸","🪆","🖼","🪞","🪟","🛍","🛒","🎁","🎈","🎏","🎀","🪄","🪅","🎊","🎉","🎎","🏮","🎐","🧧","✉️","📩","📨","📧","💌","📥","📤","📦","🏷","🪧","📪","📫","📬","📭","📮","📯","📜","📃","📄","📑","🧾","📊","📈","📉","🗒","🗓","📆","📅","🗑","📇","🗃","🗳","🗄","📋","📁","📂","🗂","🗞","📰","📓","📔","📒","📕","📗","📘","📙","📚","📖","🔖","🧷","🔗","📎","🖇","📐","📏","🧮","📌","📍","✂️","🖊","🖋","✒️","🖌","🖍","📝","✏️","🔍","🔎","🔏","🔐","🔒","🔓"},
            {"❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗","💖","💘","💝","💟","☮️","✝️","☪️","🕉","☸️","✡️","🔯","🕎","☯️","☦️","🛐","⛎","♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆔","⚛️","🉑","☢️","☣️","📴","📳","🈶","🈚","🈸","🈺","🈷️","✴️","🆚","💮","🉐","㊙️","㊗️","🈴","🈵","🈹","🈲","🅰️","🅱️","🆎","🆑","🅾️","🆘","❌","⭕","🛑","⛔","📛","🚫","💯","💢","♨️","🚷","🚯","🚳","🚱","🔞","📵","🚭","❗","❕","❓","❔","‼️","⁉️","🔅","🔆","〽️","⚠️","🚸","🔱","⚜️","🔰","♻️","✅","🈯","💹","❇️","✳️","❎","🌐","💠","Ⓜ️","🌀","💤","🏧","🚾","♿","🅿️","🛗","🈳","🈂️","🛂","🛃","🛄","🛅","🚹","🚺","🚼","⚧","🚻","🚮","🎦","📶","🈁","🔣","ℹ️","🔤","🔡","🔠","🆖","🆗","🆙","🆒","🆕","🆓","0️⃣","1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","🔟","🔢","#️⃣","*️⃣","⏏️","▶️","⏸","⏯","⏹","⏺","⏭","⏮","⏩","⏪","⏫","⏬","◀️","🔼","🔽","➡️","⬅️","⬆️","⬇️","↗️","↘️","↙️","↖️","↕️","↔️","↪️","↩️","⤴️","⤵️","🔀","🔁","🔂","🔄","🔃","🎵","🎶","➕","➖","➗","✖️","🟰","♾","💲","💱","™️","©️","®️","👁‍🗨","🔚","🔙","🔛","🔝","🔜","〰️","➰","➿","✔️","☑️","🔘","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","🔸","🔹","🔶","🔷","🔳","🔲","▪️","▫️","◾","◽","◼️","◻️","🟥","🟧","🟨","🟩","🟦","🟪","⬛","⬜","🟫","🔈","🔇","🔉","🔊","🔔","🔕","📣","📢","💬","💭","🗯","♠️","♣️","♥️","♦️","🃏","🎴","🀄","🕐","🕑","🕒","🕓","🕔","🕕","🕖","🕗","🕘","🕙","🕚","🕛"},
            {"🏳️","🏴","🏁","🚩","🏳️‍🌈","🏳️‍⚧️","🏴‍☠️","🇺🇳","🇪🇺","🇦🇫","🇦🇱","🇩🇿","🇦🇩","🇦🇴","🇦🇷","🇦🇲","🇦🇺","🇦🇹","🇦🇿","🇧🇸","🇧🇭","🇧🇩","🇧🇾","🇧🇪","🇧🇿","🇧🇴","🇧🇦","🇧🇷","🇧🇬","🇰🇭","🇨🇲","🇨🇦","🇨🇱","🇨🇳","🇨🇴","🇨🇷","🇭🇷","🇨🇺","🇨🇾","🇨🇿","🇩🇰","🇩🇴","🇪🇨","🇪🇬","🇸🇻","🇪🇪","🇪🇹","🇫🇮","🇫🇷","🇬🇪","🇩🇪","🇬🇭","🇬🇷","🇬🇹","🇭🇳","🇭🇰","🇭🇺","🇮🇸","🇮🇳","🇮🇩","🇮🇷","🇮🇶","🇮🇪","🇮🇱","🇮🇹","🇯🇲","🇯🇵","🇯🇴","🇰🇿","🇰🇪","🇰🇷","🇰🇼","🇱🇻","🇱🇧","🇱🇾","🇱🇹","🇱🇺","🇲🇾","🇲🇽","🇲🇩","🇲🇳","🇲🇪","🇲🇦","🇳🇵","🇳🇱","🇳🇿","🇳🇮","🇳🇬","🇲🇰","🇳🇴","🇴🇲","🇵🇰","🇵🇦","🇵🇾","🇵🇪","🇵🇭","🇵🇱","🇵🇹","🇶🇦","🇷🇴","🇷🇺","🇸🇦","🇷🇸","🇸🇬","🇸🇰","🇸🇮","🇿🇦","🇪🇸","🇱🇰","🇸🇪","🇨🇭","🇸🇾","🇹🇼","🇹🇭","🇹🇳","🇹🇷","🇺🇦","🇦🇪","🇬🇧","🇺🇸","🇺🇾","🇺🇿","🇻🇪","🇻🇳","🇾🇪","🇿🇼"}
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

    private void init(final Context context) {
        mTheme = PanelTheme.resolve(context);
        mTheme.applyPanelBackground(this);

        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Section title.
        mTitle = new TextView(context);
        mTitle.setTextSize(12);
        mTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mTitle.setLetterSpacing(0.06f);
        mTitle.setAllCaps(true);
        mTitle.setTextColor(mTheme.onSurfaceVariant);
        mTitle.setPadding(mTheme.dp(16), mTheme.dp(10), mTheme.dp(16), mTheme.dp(4));
        root.addView(mTitle);

        // Scrollable grid.
        mScroll = new ScrollView(context);
        mScroll.setVerticalScrollBarEnabled(false);
        mScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mGrid = new GridLayout(context);
        mGrid.setColumnCount(COLUMNS);
        mGrid.setPadding(mTheme.dp(8), 0, mTheme.dp(8), mTheme.dp(8));
        mScroll.addView(mGrid, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(mScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Bottom bar with category tabs in the middle.
        final PanelBottomBar bar = new PanelBottomBar(context, mTheme, new PanelBottomBar.Listener() {
            @Override public void onBackToKeyboard() { if (mListener != null) mListener.onClose(); }
            @Override public void onBackspace() { if (mListener != null) mListener.onBackspace(); }
        });
        final HorizontalScrollView tabScroll = new HorizontalScrollView(context);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        final LinearLayout tabs = new LinearLayout(context);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int idx = i;
            final TextView tab = new TextView(context);
            tab.setText(TAB_ICONS[i]);
            tab.setTextSize(17);
            tab.setGravity(Gravity.CENTER);
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mTheme.dp(38), mTheme.dp(34));
            lp.setMargins(mTheme.dp(1), 0, mTheme.dp(1), 0);
            tab.setLayoutParams(lp);
            tab.setOnClickListener(v -> showCategory(idx));
            tabs.addView(tab);
            mTabs.add(tab);
        }
        tabScroll.addView(tabs);
        bar.getCenterSlot().addView(tabScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        root.addView(bar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        addView(root);
        loadRecent();
        showCategory(mRecent.isEmpty() ? 1 : 0);
    }

    public void setListener(Listener l) { mListener = l; }

    private void loadRecent() {
        final String raw = mPrefs.getString(KEY_RECENT, "");
        mRecent.clear();
        if (!raw.isEmpty()) {
            mRecent.addAll(Arrays.asList(raw.split(",")));
        }
    }

    private void saveRecent() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mRecent.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(mRecent.get(i));
        }
        mPrefs.edit().putString(KEY_RECENT, sb.toString()).apply();
    }

    private void showCategory(final int idx) {
        mCategory = idx;
        mTitle.setText(TITLES[idx]);
        for (int i = 0; i < mTabs.size(); i++) {
            final TextView t = mTabs.get(i);
            if (i == idx) {
                t.setBackground(mTheme.roundRect(mTheme.accent, 17));
                t.setAlpha(1f);
            } else {
                t.setBackground(mTheme.ripple(17));
                t.setAlpha(0.7f);
            }
        }

        mGrid.removeAllViews();
        mScroll.scrollTo(0, 0);
        final List<String> emojis = new ArrayList<>();
        if (idx == 0) {
            emojis.addAll(mRecent);
        } else {
            emojis.addAll(Arrays.asList(CATEGORIES[idx]));
        }
        if (emojis.isEmpty()) {
            final TextView empty = new TextView(getContext());
            empty.setText("Emoji you use will show up here");
            empty.setTextColor(mTheme.onSurfaceVariant);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, mTheme.dp(40), 0, mTheme.dp(40));
            final GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.columnSpec = GridLayout.spec(0, COLUMNS, 1f);
            lp.width = 0;
            empty.setLayoutParams(lp);
            mGrid.addView(empty);
            return;
        }
        final int cell = mTheme.dp(44);
        for (final String e : emojis) {
            final TextView tv = new TextView(getContext());
            tv.setText(e);
            tv.setTextSize(26);
            tv.setGravity(Gravity.CENTER);
            tv.setBackground(mTheme.ripple(12));
            final GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.height = cell;
            tv.setLayoutParams(lp);
            tv.setOnClickListener(v -> {
                if (mListener != null) mListener.onEmojiSelected(e);
                mRecent.remove(e);
                mRecent.add(0, e);
                while (mRecent.size() > MAX_RECENT) mRecent.remove(mRecent.size() - 1);
                saveRecent();
            });
            mGrid.addView(tv);
        }
    }
}
