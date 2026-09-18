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

package rkr.simplekeyboard.inputmethod.keyboard;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import rkr.simplekeyboard.inputmethod.R;

/**
 * LightClip contextual strip — Master Plan §3 Layer 1 and §14.
 *
 * The top-most layer of the keyboard. It is intentionally *not* a word
 * suggestion strip (LightClip has no prediction engine); it is a contextual
 * action strip that changes with the state of the text field:
 *
 *   normal typing  -> Paste · Select all · Translate
 *   text selected  -> Copy · Cut · Paste · Select all
 *
 * Chips are built programmatically from {@link PanelTheme}, so the strip
 * always matches the active keyboard theme (including Material You) with no
 * per-theme resources.
 */
public final class LightClipStripView extends HorizontalScrollView {

    public static final int ACTION_SELECT_ALL = 1;
    public static final int ACTION_CUT = 2;
    public static final int ACTION_COPY = 3;
    public static final int ACTION_PASTE = 4;
    public static final int ACTION_TRANSLATE = 5;

    public interface Listener {
        void onStripAction(final int action);
    }

    private final LinearLayout mRow;
    private PanelTheme mTheme;
    private Listener mListener;
    private boolean mHasSelection;
    private boolean mBuilt;

    public LightClipStripView(final Context context) {
        this(context, null);
    }

    public LightClipStripView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);
        mRow = new LinearLayout(context);
        mRow.setOrientation(LinearLayout.HORIZONTAL);
        mRow.setGravity(Gravity.CENTER_VERTICAL);
        addView(mRow, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        applyTheme();
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    /** Re-reads the active theme (called when the user changes themes/colors). */
    public void applyTheme() {
        mTheme = PanelTheme.resolve(getContext());
        setBackgroundColor(mTheme.background);
        final int pad = mTheme.dp(6);
        mRow.setPadding(pad, mTheme.dp(4), pad, mTheme.dp(4));
        mBuilt = false;
        build();
    }

    /**
     * Updates the strip for the current editor state.
     *
     * @param hasSelection whether the text field currently has a selection.
     */
    public void setSelectionState(final boolean hasSelection) {
        if (mBuilt && hasSelection == mHasSelection) {
            return;
        }
        mHasSelection = hasSelection;
        build();
    }

    private void build() {
        mRow.removeAllViews();
        if (mHasSelection) {
            addChip(R.drawable.ic_lc_copy, R.string.lightclip_action_copy, ACTION_COPY, true);
            addChip(R.drawable.ic_lc_cut, R.string.lightclip_action_cut, ACTION_CUT, false);
            addChip(R.drawable.ic_lc_paste, R.string.lightclip_action_paste, ACTION_PASTE, false);
            addChip(R.drawable.ic_lc_select_all, R.string.lightclip_action_select_all,
                    ACTION_SELECT_ALL, false);
        } else {
            addChip(R.drawable.ic_lc_paste, R.string.lightclip_action_paste, ACTION_PASTE, true);
            addChip(R.drawable.ic_lc_select_all, R.string.lightclip_action_select_all,
                    ACTION_SELECT_ALL, false);
            addChip(R.drawable.ic_lc_translate, R.string.lightclip_action_translate,
                    ACTION_TRANSLATE, false);
        }
        mBuilt = true;
    }

    private void addChip(final int iconRes, final int labelRes, final int action,
            final boolean emphasized) {
        final Context context = getContext();
        final TextView chip = new TextView(context);
        chip.setText(labelRes);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        chip.setSingleLine(true);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setTypeface(Typeface.DEFAULT, emphasized ? Typeface.BOLD : Typeface.NORMAL);
        final int textColor = emphasized ? mTheme.onSurface : mTheme.onSurfaceVariant;
        chip.setTextColor(textColor);

        final Drawable icon = context.getResources().getDrawable(iconRes, context.getTheme());
        if (icon != null) {
            final int size = mTheme.dp(16);
            icon.mutate();
            icon.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
            icon.setBounds(0, 0, size, size);
            chip.setCompoundDrawables(icon, null, null, null);
            chip.setCompoundDrawablePadding(mTheme.dp(6));
        }

        final int padH = mTheme.dp(12);
        final int padV = mTheme.dp(7);
        chip.setPadding(padH, padV, padH, padV);
        chip.setBackground(mTheme.pressable(
                emphasized ? mTheme.surface : mTheme.functionalSurface,
                mTheme.surfacePressed, 18f));
        chip.setContentDescription(context.getString(labelRes));
        chip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mListener != null) {
                    mListener.onStripAction(action);
                }
            }
        });

        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = mTheme.dp(6);
        mRow.addView(chip, lp);
    }
}
