## v9.0 — redesign completion (Master Plan phases 3, 6, 7)

This release closes the remaining gaps between the code and
`LIGHTCLIP_UI_REDESIGN_MASTERPLAN.md`.

### Layer 1 — contextual strip (§3, §14)
- New `keyboard/LightClipStripView`: the top layer of the keyboard, built
  programmatically from `PanelTheme` so it matches every theme (including
  Material You) with no per-theme resources.
- Contextual behaviour driven by the editor state:
  - no selection → **Paste · Select all · Translate**
  - text selected → **Copy · Cut · Paste · Select all**
- Actions use the platform context-menu actions (`android.R.id.copy` etc.),
  so they behave exactly like the system ones in every app.
- Wired through `KeyboardSwitcher#updateContextStrip`, called from
  `LatinIME#onUpdateSelection` and `onStartInputViewInternal`.

### Layer 2 — toolbar (§12, §13)
- Replaced the emoji-glyph toolbar labels with a real vector icon set
  (`ic_lc_smart`, `ic_lc_clipboard`, `ic_lc_emoji`, `ic_lc_more`,
  `ic_lc_copy`, `ic_lc_cut`, `ic_lc_paste`, `ic_lc_select_all`,
  `ic_lc_translate`), tinted with the active theme's functional color.
- The **Smart** button is no longer a placeholder: it opens the translator
  panel in the same panel host used by clipboard and emoji.

### Translator
- Restyled with the design system: rounded accent primary button, outlined
  input field, theme-aware secondary/close buttons, sentence-case labels.

### Settings (§23, §24 — Phase 6)
- Root settings split into **Keyboard**, **Personalization** and **About**
  categories, every row now carries a descriptive summary.
- Appearance screen reorganised into **Preview**, **Theme** and **Keyboard**
  groups.

### Theme studio (§25, §26 — Phase 7)
- New `KeyboardPreviewPreference`: a live miniature of all three layers
  drawn with the resolved theme colors; refreshes immediately after any
  theme, preset or color change.
- New `ThemePresets`: one-tap looks (Midnight, Graphite, Deep ocean, Pure
  black, Daylight, Paper, Outlined light, Material You) that write the same
  preferences the manual controls use.
