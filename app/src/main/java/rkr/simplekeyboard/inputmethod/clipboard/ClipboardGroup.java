package rkr.simplekeyboard.inputmethod.clipboard;

/**
 * User-defined group for organizing clipboard items and notes.
 */
public final class ClipboardGroup {
    public final long id;
    public String name;
    public final long created;

    public ClipboardGroup(long id, String name, long created) {
        this.id = id;
        this.name = name != null ? name : "";
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClipboardGroup)) return false;
        return id == ((ClipboardGroup) o).id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
