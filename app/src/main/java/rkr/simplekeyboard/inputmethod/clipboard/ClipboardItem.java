package rkr.simplekeyboard.inputmethod.clipboard;

import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight representation of a clipboard entry or note.
 * The full original text is always preserved (lossless).
 * Preview is only for UI display.
 */
public final class ClipboardItem {
    public final long id;
    public final String text;          // NEVER truncated
    public final long timestamp;
    public boolean pinned;
    public final boolean isNote;
    public final Set<Long> groupIds;

    // UI-only short preview (computed, not stored as source of truth)
    private transient String previewCache;

    public ClipboardItem(long id, String text, long timestamp, boolean pinned, boolean isNote) {
        this.id = id;
        this.text = text != null ? text : "";
        this.timestamp = timestamp;
        this.pinned = pinned;
        this.isNote = isNote;
        this.groupIds = new HashSet<>();
    }

    public ClipboardItem(long id, String text, long timestamp, boolean pinned, boolean isNote, Set<Long> groupIds) {
        this.id = id;
        this.text = text != null ? text : "";
        this.timestamp = timestamp;
        this.pinned = pinned;
        this.isNote = isNote;
        this.groupIds = groupIds != null ? new HashSet<>(groupIds) : new HashSet<>();
    }

    /**
     * Returns a short preview for list UI only.
     * Does NOT affect the stored text.
     */
    public String getPreview(int maxChars) {
        if (previewCache != null && previewCache.length() <= maxChars + 3) {
            return previewCache;
        }
        if (text.length() <= maxChars) {
            previewCache = text;
            return text;
        }
        // Prefer breaking at newline or space near the limit
        int end = maxChars;
        int nl = text.lastIndexOf('\n', maxChars);
        if (nl > maxChars / 2) {
            end = nl;
        } else {
            int sp = text.lastIndexOf(' ', maxChars);
            if (sp > maxChars / 2) end = sp;
        }
        previewCache = text.substring(0, end).trim() + "…";
        return previewCache;
    }

    public int length() {
        return text.length();
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClipboardItem)) return false;
        ClipboardItem that = (ClipboardItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
