// Replacement for pasteClipboard() in RichInputConnection.java
// This uses the SmartPasteEngine for lossless large pastes.

import rkr.simplekeyboard.inputmethod.paste.SmartPasteEngine;

public void pasteClipboard() {
    final ClipboardManager clipboard = (ClipboardManager) mLatinIME.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard != null && clipboard.hasPrimaryClip()) {
        final ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() >= 1) {
            final CharSequence pasteData = clipData.getItemAt(0).getText();
            if (pasteData != null && pasteData.length() > 0) {
                final String full = pasteData.toString();
                // Use SmartPasteEngine instead of a single onTextInput / commit
                SmartPasteEngine.paste(mIC, full, new SmartPasteEngine.Callback() {
                    @Override
                    public void onComplete(boolean success, int charsInserted) {
                        // Optional: log or show subtle feedback
                    }
                    @Override
                    public void onProgress(int inserted, int total) {
                        // Optional: keep keyboard responsive, no heavy UI updates
                    }
                });
                return;
            }
        }
    }
    // Fallback to system paste for non-text or empty
    mIC.performContextMenuAction(android.R.id.paste);
}

// Also add a public method for inserting from our own history:
public void pasteFromHistory(String fullText) {
    if (fullText == null || fullText.isEmpty()) return;
    SmartPasteEngine.paste(mIC, fullText, null);
}
