# Clipboard Module Design for LightClip Keyboard

## Data Model

```java
public class ClipboardItem {
    public long id;
    public String text;          // FULL original text, never truncated
    public long timestamp;
    public boolean pinned;
    public Set<Long> groupIds;   // Many-to-many with groups
    public boolean isNote;       // true for permanent notes
    public String preview;       // short UI preview only
}
```

## Persistence

Use SharedPreferences for small metadata + files in app private dir for large texts,
or better: SQLite with TEXT column (Android SQLite supports large TEXT).

For maximum lightness and no heavy deps: use a simple JSON file or Room is too heavy.
Prefer: File-based with one JSON index + individual .txt files for content > 4KB.
Or single SQLiteOpenHelper with one table.

Recommended: SQLiteOpenHelper (android.database.sqlite is built-in, zero extra size).

Table:
CREATE TABLE clipboard_items (
  id INTEGER PRIMARY KEY,
  text TEXT NOT NULL,          -- full lossless
  timestamp INTEGER,
  pinned INTEGER DEFAULT 0,
  is_note INTEGER DEFAULT 0
);

CREATE TABLE groups (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  created INTEGER
);

CREATE TABLE item_groups (
  item_id INTEGER,
  group_id INTEGER,
  PRIMARY KEY(item_id, group_id)
);

## Capture

In LatinIME or a ClipboardListener using ClipboardManager.OnPrimaryClipChangedListener
(registered only when keyboard is active or via sticky service carefully to avoid battery).

On change:
- getPrimaryClip
- if text and length > 0 and not equal to last captured -> insert

History limit configurable (default 100).

## UI

Separate full-screen or popup activity/fragment for Clipboard Hub, launched from key.

Keys to add:
- Clipboard key (📋)
- Emoji key
- Translate key (or long-press language)

Lazy inflate the ClipboardView only when the key is pressed.
