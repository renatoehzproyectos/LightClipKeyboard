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
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Gboard-style bottom bar shared by the emoji and clipboard panels:
 * [ ABC ]  ......... center slot .........  [ backspace ]
 */
public final class PanelBottomBar extends LinearLayout {

    public interface Listener {
        void onBackToKeyboard();
        void onBackspace();
    }

    private static final long REPEAT_START_MS = 400;
    private static final long REPEAT_INTERVAL_MS = 50;

    private final PanelTheme mTheme;
    private final Listener mListener;
    private final LinearLayout mCenter;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public PanelBottomBar(final Context context, final PanelTheme theme, final Listener listener) {
        super(context);
        mTheme = theme;
        mListener = listener;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        final int h = theme.dp(6);
        setPadding(theme.dp(8), h, theme.dp(8), h);
        theme.applyPanelBackground(this);

        // ABC — back to the keyboard.
        final TextView abc = new TextView(context);
        abc.setText("ABC");
        abc.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        abc.setTextSize(15);
        abc.setLetterSpacing(0.04f);
        abc.setTextColor(theme.onSurface);
        abc.setGravity(Gravity.CENTER);
        abc.setBackground(theme.pressable(theme.functionalSurface, theme.surfacePressed, 12));
        abc.setLayoutParams(new LayoutParams(theme.dp(64), theme.dp(40)));
        abc.setOnClickListener(v -> listener.onBackToKeyboard());
        addView(abc);

        mCenter = new LinearLayout(context);
        mCenter.setOrientation(HORIZONTAL);
        mCenter.setGravity(Gravity.CENTER);
        final LayoutParams cp = new LayoutParams(0, theme.dp(40), 1f);
        cp.setMargins(theme.dp(8), 0, theme.dp(8), 0);
        mCenter.setLayoutParams(cp);
        addView(mCenter);

        // Backspace with press-and-hold repeat.
        final ImageView del = new ImageView(context);
        del.setImageResource(rkr.simplekeyboard.inputmethod.R.drawable.ic_backspace);
        del.setColorFilter(theme.onSurface, PorterDuff.Mode.SRC_IN);
        del.setScaleType(ImageView.ScaleType.CENTER);
        del.setBackground(theme.pressable(theme.functionalSurface, theme.surfacePressed, 12));
        del.setLayoutParams(new LayoutParams(theme.dp(64), theme.dp(40)));
        del.setContentDescription("Delete");
        del.setOnTouchListener(new OnTouchListener() {
            private final Runnable mRepeat = new Runnable() {
                @Override public void run() {
                    mListener.onBackspace();
                    mHandler.postDelayed(this, REPEAT_INTERVAL_MS);
                }
            };
            @Override public boolean onTouch(final View v, final MotionEvent e) {
                switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.setPressed(true);
                    mListener.onBackspace();
                    mHandler.postDelayed(mRepeat, REPEAT_START_MS);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setPressed(false);
                    mHandler.removeCallbacks(mRepeat);
                    return true;
                }
                return false;
            }
        });
        addView(del);
    }

    /** Slot between ABC and backspace (category tabs, search field, ...). */
    public LinearLayout getCenterSlot() {
        return mCenter;
    }

    public PanelTheme getTheme() {
        return mTheme;
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
