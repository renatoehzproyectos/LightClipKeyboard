# LightClip Keyboard

Lightweight Android keyboard with organized lossless clipboard, groups, notes, smart large-paste, emoji & translator.

Built on Simple Keyboard (Apache-2.0).

## Build the APK with GitHub Actions (no Android Studio needed)

1. Create a new GitHub repository.
2. Upload / push the entire contents of this folder to the `main` (or `master`) branch.
3. Go to the **Actions** tab of your repository.
4. The workflow **Build APK** will run automatically (or click "Run workflow").
5. When it finishes successfully, download the artifact named **`lightclip-keyboard-debug`**.
6. Unzip it — you will get the `.apk` file.
7. Install the APK on your Android device, then:
   - Settings → System → Languages & input → On-screen keyboard → Manage on-screen keyboards
   - Enable **LightClip Keyboard**
   - Select it as the current keyboard.

The workflow is the same proven one used by ProjectCenter (JDK 17 + official cmdline-tools + assembleDebug).

## Features

- Three-layer keyboard UI: contextual action strip, tools toolbar, keys
- Contextual actions (copy / cut / paste / select all / translate)
- Live theme preview and one-tap theme presets
- Lossless clipboard history (never truncates text)
- User groups, notes, pinning, search
- SmartPasteEngine for large texts (chunked, boundary-aware)
- Emoji picker (lazy, system emoji)
- Translator (lazy, modular)
- Extremely low resource usage, zero external libraries

## Manual build (if you have Android Studio)

Open the project and run `assembleDebug`.

## License

Apache License 2.0
