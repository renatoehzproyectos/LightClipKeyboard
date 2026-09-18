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

package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import rkr.simplekeyboard.inputmethod.keyboard.PanelTheme;

/**
 * Live theme preview (Master Plan §25, §27, Phase 7).
 *
 * Draws a miniature of the three LightClip layers — contextual strip, tools
 * toolbar and keys — using the exact colors {@link PanelTheme} resolves for
 * the currently selected theme, so changing a theme, accent or keyboard color
 * updates the preview immediately.
 */
public final class KeyboardPreviewPreference extends Preference {

    public KeyboardPreviewPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setSelectable(false);
        setPersistent(false);
    }

    /** Re-draws the preview after a theme/color change. */
    public void refresh() {
        notifyChanged();
    }

    @Override
    protected View onCreateView(final ViewGroup parent) {
        final Context context = getContext();
        final PanelTheme theme = PanelTheme.resolve(context);

        final LinearLayout outer = new LinearLayout(context);
        outer.setOrientation(LinearLayout.VERTICAL);
        final int outerPad = theme.dp(16);
        outer.setPadding(outerPad, theme.dp(12), outerPad, theme.dp(12));

        final LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(theme.roundRect(theme.background, 16f));
        final int pad = theme.dp(8);
        card.setPadding(pad, pad, pad, pad);

        // Layer 1 — contextual strip.
        card.addView(chipRow(context, theme, new String[] { "Paste", "Select all", "Translate" },
                true));
        // Layer 2 — tools toolbar.
        card.addView(chipRow(context, theme, new String[] { "Smart", "Clipboard", "Emoji" },
                false));
        // Layer 3 — keys.
        card.addView(keyRow(context, theme, "qwertyuiop"));
        card.addView(keyRow(context, theme, "asdfghjkl"));
        card.addView(keyRow(context, theme, "zxcvbnm"));

        outer.addView(card, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return outer;
    }

    @Override
    protected void onBindView(final View view) {
        // The whole view is rebuilt in onCreateView; nothing to bind.
    }

    private static LinearLayout chipRow(final Context context, final PanelTheme theme,
            final String[] labels, final boolean emphasizeFirst) {
        final LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        final LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = theme.dp(6);
        row.setLayoutParams(rowLp);

        for (int i = 0; i < labels.length; i++) {
            final boolean emphasized = emphasizeFirst && i == 0;
            final TextView chip = new TextView(context);
            chip.setText(labels[i]);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            chip.setSingleLine(true);
            chip.setGravity(Gravity.CENTER);
            chip.setTypeface(Typeface.DEFAULT, emphasized ? Typeface.BOLD : Typeface.NORMAL);
            chip.setTextColor(emphasized ? theme.onSurface : theme.onSurfaceVariant);
            final int padH = theme.dp(10);
            final int padV = theme.dp(5);
            chip.setPadding(padH, padV, padH, padV);
            chip.setBackground(theme.roundRect(
                    emphasized ? theme.surface : theme.functionalSurface, 14f));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = theme.dp(6);
            row.addView(chip, lp);
        }
        return row;
    }

    private static LinearLayout keyRow(final Context context, final PanelTheme theme,
            final String letters) {
        final LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        final LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = theme.dp(4);
        row.setLayoutParams(rowLp);

        for (int i = 0; i < letters.length(); i++) {
            final TextView key = new TextView(context);
            key.setText(String.valueOf(letters.charAt(i)));
            key.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            key.setGravity(Gravity.CENTER);
            key.setTextColor(theme.onSurface);
            key.setHeight(theme.dp(30));
            key.setBackground(theme.roundRect(theme.surface, 8f));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.rightMargin = theme.dp(3);
            row.addView(key, lp);
        }
        return row;
    }
}
