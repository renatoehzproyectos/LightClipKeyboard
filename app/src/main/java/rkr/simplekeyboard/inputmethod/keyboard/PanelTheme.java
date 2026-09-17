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
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;
import rkr.simplekeyboard.inputmethod.latin.settings.Settings;

/**
 * Resolves the colors of the active keyboard theme so the emoji / clipboard panels can be
 * drawn with the same Material 3 palette as the keys (including Material You on Android 12+).
 */
public final class PanelTheme {

    public final int background;
    public final int surface;
    public final int surfacePressed;
    public final int functionalSurface;
    public final int outline;
    public final int onSurface;
    public final int onSurfaceVariant;
    public final int accent;
    public final int accentPressed;
    public final int onAccent;
    public final Context themedContext;

    private PanelTheme(final Context themedContext, final int background, final int surface,
            final int surfacePressed, final int functionalSurface, final int outline,
            final int onSurface, final int onSurfaceVariant, final int accent,
            final int accentPressed, final int onAccent) {
        this.themedContext = themedContext;
        this.background = background;
        this.surface = surface;
        this.surfacePressed = surfacePressed;
        this.functionalSurface = functionalSurface;
        this.outline = outline;
        this.onSurface = onSurface;
        this.onSurfaceVariant = onSurfaceVariant;
        this.accent = accent;
        this.accentPressed = accentPressed;
        this.onAccent = onAccent;
    }

    public static PanelTheme resolve(final Context context) {
        final KeyboardTheme theme = KeyboardTheme.getKeyboardTheme(context);
        final Context themed = new ContextThemeWrapper(context, theme.mStyleId);

        final TypedArray t = themed.obtainStyledAttributes(R.styleable.KeyboardTheme);
        final int surface = t.getColor(R.styleable.KeyboardTheme_keyNormalBackgroundColor, 0xFFFFFFFF);
        final int surfacePressed = t.getColor(R.styleable.KeyboardTheme_keyPressedBackgroundColor, 0x33000000);
        final int functional = t.getColor(R.styleable.KeyboardTheme_keyFunctionalBackgroundColor, surfacePressed);
        final int accent = t.getColor(R.styleable.KeyboardTheme_keyAccentBackgroundColor, 0xFF0B57D0);
        final int accentPressed = t.getColor(R.styleable.KeyboardTheme_keyAccentPressedBackgroundColor, accent);
        final int onAccent = t.getColor(R.styleable.KeyboardTheme_keyOnAccentColor, 0xFFFFFFFF);
        final int outline = t.getColor(R.styleable.KeyboardTheme_panelOutlineColor, 0x1F000000);
        final int keyboardViewStyle = t.getResourceId(R.styleable.KeyboardTheme_keyboardViewStyle, 0);
        t.recycle();

        int background = 0xFF1E1F22;
        int onSurface = 0xFFF1F2F4;
        int onSurfaceVariant = 0x8AF1F2F4;
        if (keyboardViewStyle != 0) {
            final TypedArray kv = themed.obtainStyledAttributes(keyboardViewStyle, R.styleable.Keyboard_Key);
            onSurface = kv.getColor(R.styleable.Keyboard_Key_keyTextColor, onSurface);
            onSurfaceVariant = kv.getColor(R.styleable.Keyboard_Key_keyHintLetterColor, onSurfaceVariant);
            kv.recycle();
            final TypedArray bg = themed.obtainStyledAttributes(keyboardViewStyle,
                    new int[] { android.R.attr.background });
            background = bg.getColor(0, background);
            bg.recycle();
        }
        // Light / Dark themes let the user pick a custom keyboard color; honour it.
        if (theme.mCustomColorSupport) {
            final SharedPreferences prefs = PreferenceManagerCompat.getDeviceSharedPreferences(context);
            background = Settings.readKeyboardColor(prefs, context);
        }
        return new PanelTheme(themed, background, surface, surfacePressed, functional, outline,
                onSurface, onSurfaceVariant, accent, accentPressed, onAccent);
    }

    // ---------------------------------------------------------------- helpers

    public int dp(final float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                themedContext.getResources().getDisplayMetrics()));
    }

    /** Whether the resolved background is dark (used to pick ripple tint). */
    public boolean isDark() {
        final int c = background;
        final double lum = 0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c);
        return lum < 140;
    }

    public GradientDrawable roundRect(final int color, final float radiusDp) {
        final GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    public GradientDrawable outlinedRoundRect(final int color, final float radiusDp) {
        final GradientDrawable d = roundRect(color, radiusDp);
        d.setStroke(dp(1), outline);
        return d;
    }

    /** A rounded surface with a soft pressed state. */
    public android.graphics.drawable.Drawable pressable(final int normal, final int pressed,
            final float radiusDp) {
        final StateListDrawable sl = new StateListDrawable();
        sl.addState(new int[] { android.R.attr.state_pressed }, roundRect(pressed, radiusDp));
        sl.addState(new int[] {}, roundRect(normal, radiusDp));
        return sl;
    }

    /** Transparent background that only shows a ripple/press tint (for emoji cells, icons). */
    public android.graphics.drawable.Drawable ripple(final float radiusDp) {
        final int tint = isDark() ? 0x33FFFFFF : 0x1F000000;
        final GradientDrawable mask = roundRect(Color.WHITE, radiusDp);
        return new RippleDrawable(ColorStateList.valueOf(tint), null, mask);
    }

    public void applyPanelBackground(final View v) {
        v.setBackgroundColor(background);
    }
}
