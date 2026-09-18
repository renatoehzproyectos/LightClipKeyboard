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

import android.content.SharedPreferences;

import rkr.simplekeyboard.inputmethod.keyboard.KeyboardTheme;

/**
 * One-tap theme presets (Master Plan §26, Phase 7).
 *
 * A preset is simply a keyboard theme plus an optional background color, so it
 * writes the same preferences the manual controls write — no parallel theming
 * system, no migration concerns.
 */
public final class ThemePresets {

    public static final class Preset {
        public final String name;
        public final int themeId;
        /** Background color, or {@code null} to keep the theme default. */
        public final Integer color;

        Preset(final String name, final int themeId, final Integer color) {
            this.name = name;
            this.themeId = themeId;
            this.color = color;
        }
    }

    public static final Preset[] PRESETS = {
        new Preset("Midnight", KeyboardTheme.THEME_ID_DARK, 0xFF16181C),
        new Preset("Graphite", KeyboardTheme.THEME_ID_DARK, 0xFF24262B),
        new Preset("Deep ocean", KeyboardTheme.THEME_ID_DARK, 0xFF10253A),
        new Preset("Pure black", KeyboardTheme.THEME_ID_AMOLED, null),
        new Preset("Daylight", KeyboardTheme.THEME_ID_LIGHT, 0xFFF2F3F7),
        new Preset("Paper", KeyboardTheme.THEME_ID_LIGHT, 0xFFFBF9F4),
        new Preset("Outlined light", KeyboardTheme.THEME_ID_LIGHT_BORDER, null),
        new Preset("Material You", KeyboardTheme.THEME_ID_SYSTEM, null),
    };

    public static String[] names() {
        final String[] names = new String[PRESETS.length];
        for (int i = 0; i < PRESETS.length; i++) {
            names[i] = PRESETS[i].name;
        }
        return names;
    }

    public static void apply(final Preset preset, final SharedPreferences prefs) {
        KeyboardTheme.saveKeyboardThemeId(preset.themeId, prefs);
        final SharedPreferences.Editor editor = prefs.edit();
        if (preset.color == null) {
            editor.remove(Settings.PREF_KEYBOARD_COLOR);
        } else {
            editor.putInt(Settings.PREF_KEYBOARD_COLOR, preset.color);
        }
        editor.apply();
    }

    private ThemePresets() {}
}
