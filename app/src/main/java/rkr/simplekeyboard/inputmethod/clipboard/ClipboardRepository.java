package rkr.simplekeyboard.inputmethod.clipboard;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository for persistent, lossless clipboard history + notes + groups.
 * Designed for low RAM: queries are limited, full text loaded only when needed.
 */
public final class ClipboardRepository {
    private static final String TAG = "ClipboardRepo";
    private static final String PREFS = "clipboard_prefs";
    private static final String KEY_HISTORY_LIMIT = "history_limit";
    private static final int DEFAULT_LIMIT = 100;

    private static ClipboardRepository sInstance;

    private final ClipboardDbHelper mDbHelper;
    private final SharedPreferences mPrefs;
    private String mLastCapturedText; // avoid exact duplicates

    private ClipboardRepository(Context context) {
        // Always use device-protected storage: the IME service is directBootAware and can be
        // created before the user unlocks the device, where credential-protected storage (the
        // default) is unavailable and throws, killing the service on startup.
        final Context storageContext = getStorageContext(context);
        mDbHelper = new ClipboardDbHelper(storageContext);
        mPrefs = storageContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static Context getStorageContext(final Context context) {
        final Context appContext = context.getApplicationContext() != null
                ? context.getApplicationContext() : context;
        try {
            final Context deviceContext = appContext.createDeviceProtectedStorageContext();
            if (deviceContext != null) {
                return deviceContext;
            }
        } catch (Exception e) {
            Log.w(TAG, "Device protected storage unavailable", e);
        }
        return appContext;
    }

    public static synchronized ClipboardRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ClipboardRepository(context);
        }
        return sInstance;
    }

    public int getHistoryLimit() {
        return mPrefs.getInt(KEY_HISTORY_LIMIT, DEFAULT_LIMIT);
    }

    public void setHistoryLimit(int limit) {
        mPrefs.edit().putInt(KEY_HISTORY_LIMIT, Math.max(10, Math.min(limit, 5000))).apply();
    }

    /**
     * Capture a new clipboard entry. Lossless.
     * Skips if identical to the immediately previous capture.
     */
    public long addFromClipboard(String text) {
        if (text == null || text.isEmpty()) return -1;
        if (text.equals(mLastCapturedText)) return -1;
        mLastCapturedText = text;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("text", text);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("pinned", 0);
        cv.put("is_note", 0);
        long id = db.insert(ClipboardDbHelper.TABLE_ITEMS, null, cv);

        // Enforce limit on non-pinned, non-note items
        enforceLimit(db);
        return id;
    }

    public long addNote(String text) {
        if (text == null) text = "";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("text", text);
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("pinned", 0);
        cv.put("is_note", 1);
        return db.insert(ClipboardDbHelper.TABLE_ITEMS, null, cv);
    }

    public void updateText(long id, String newText) {
        if (newText == null) newText = "";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("text", newText);
        db.update(ClipboardDbHelper.TABLE_ITEMS, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void setPinned(long id, boolean pinned) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("pinned", pinned ? 1 : 0);
        db.update(ClipboardDbHelper.TABLE_ITEMS, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void delete(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ClipboardDbHelper.TABLE_ITEMS, "id=?", new String[]{String.valueOf(id)});
    }

    public ClipboardItem getById(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try (Cursor c = db.query(ClipboardDbHelper.TABLE_ITEMS, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return fromCursor(c, true);
            }
        }
        return null;
    }

    /**
     * Recent non-grouped items (or all recent if includeGrouped).
     * Full text is loaded; call only when needed for display of small sets.
     */
    public List<ClipboardItem> getRecent(int limit, boolean onlyUnpinned) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String selection = "is_note=0";
        if (onlyUnpinned) selection += " AND pinned=0";
        List<ClipboardItem> list = new ArrayList<>();
        try (Cursor c = db.query(ClipboardDbHelper.TABLE_ITEMS, null, selection,
                null, null, null, "timestamp DESC", String.valueOf(limit))) {
            while (c.moveToNext()) {
                list.add(fromCursor(c, true));
            }
        }
        loadGroupIds(list);
        return list;
    }

    public List<ClipboardItem> getPinned() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<ClipboardItem> list = new ArrayList<>();
        try (Cursor c = db.query(ClipboardDbHelper.TABLE_ITEMS, null, "pinned=1",
                null, null, null, "timestamp DESC")) {
            while (c.moveToNext()) {
                list.add(fromCursor(c, true));
            }
        }
        loadGroupIds(list);
        return list;
    }

    public List<ClipboardItem> getNotes() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<ClipboardItem> list = new ArrayList<>();
        try (Cursor c = db.query(ClipboardDbHelper.TABLE_ITEMS, null, "is_note=1",
                null, null, null, "timestamp DESC")) {
            while (c.moveToNext()) {
                list.add(fromCursor(c, true));
            }
        }
        loadGroupIds(list);
        return list;
    }

    public List<ClipboardItem> getByGroup(long groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "SELECT i.* FROM " + ClipboardDbHelper.TABLE_ITEMS + " i " +
                "INNER JOIN " + ClipboardDbHelper.TABLE_ITEM_GROUPS + " ig ON i.id = ig.item_id " +
                "WHERE ig.group_id=? ORDER BY i.timestamp DESC";
        List<ClipboardItem> list = new ArrayList<>();
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(groupId)})) {
            while (c.moveToNext()) {
                list.add(fromCursor(c, true));
            }
        }
        loadGroupIds(list);
        return list;
    }

    /**
     * Lightweight search across text. Uses LIKE — good enough for local small DB.
     * Limit results to keep memory low.
     */
    public List<ClipboardItem> search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) return getRecent(limit, false);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String q = "%" + query.trim() + "%";
        List<ClipboardItem> list = new ArrayList<>();
        try (Cursor c = db.query(ClipboardDbHelper.TABLE_ITEMS, null, "text LIKE ?",
                new String[]{q}, null, null, "pinned DESC, timestamp DESC", String.valueOf(limit))) {
            while (c.moveToNext()) {
                list.add(fromCursor(c, true));
            }
        }
        loadGroupIds(list);
        return list;
    }

    // ---- Groups ----

    public long createGroup(String name) {
        if (name == null || name.trim().isEmpty()) return -1;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name.trim());
        cv.put("created", System.currentTimeMillis());
        try {
            return db.insertOrThrow(ClipboardDbHelper.TABLE_GROUPS, null, cv);
        } catch (Exception e) {
            Log.w(TAG, "Group already exists or error", e);
            return -1;
        }
    }

    public void renameGroup(long id, String newName) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", newName);
        db.update(ClipboardDbHelper.TABLE_GROUPS, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteGroup(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ClipboardDbHelper.TABLE_GROUPS, "id=?", new String[]{String.valueOf(id)});
        // item_groups cascade via FK
    }

    public List<ClipboardGroup> getAllGroups() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<ClipboardGroup> list = new ArrayList<>();
        try (Cursor c = db.query(ClipboardDbHelper.TABLE_GROUPS, null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) {
                list.add(new ClipboardGroup(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getLong(c.getColumnIndexOrThrow("created"))
                ));
            }
        }
        return list;
    }

    public void addItemToGroup(long itemId, long groupId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("item_id", itemId);
        cv.put("group_id", groupId);
        db.insertWithOnConflict(ClipboardDbHelper.TABLE_ITEM_GROUPS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeItemFromGroup(long itemId, long groupId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ClipboardDbHelper.TABLE_ITEM_GROUPS, "item_id=? AND group_id=?",
                new String[]{String.valueOf(itemId), String.valueOf(groupId)});
    }

    /**
     * Move: remove from all other groups (and conceptually from "recent" by leaving only the target group association).
     * Actual "recent" is just items without special filter; we keep the item.
     */
    public void moveItemToGroup(long itemId, long groupId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(ClipboardDbHelper.TABLE_ITEM_GROUPS, "item_id=?", new String[]{String.valueOf(itemId)});
            ContentValues cv = new ContentValues();
            cv.put("item_id", itemId);
            cv.put("group_id", groupId);
            db.insert(ClipboardDbHelper.TABLE_ITEM_GROUPS, null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ---- Helpers ----

    private void enforceLimit(SQLiteDatabase db) {
        int limit = getHistoryLimit();
        // Delete oldest non-pinned, non-note beyond limit
        String sql = "DELETE FROM " + ClipboardDbHelper.TABLE_ITEMS +
                " WHERE id IN (" +
                "SELECT id FROM " + ClipboardDbHelper.TABLE_ITEMS +
                " WHERE pinned=0 AND is_note=0 " +
                "ORDER BY timestamp ASC " +
                "LIMIT -1 OFFSET " + limit + ")";
        db.execSQL(sql);
    }

    private ClipboardItem fromCursor(Cursor c, boolean loadText) {
        long id = c.getLong(c.getColumnIndexOrThrow("id"));
        String text = loadText ? c.getString(c.getColumnIndexOrThrow("text")) : "";
        long ts = c.getLong(c.getColumnIndexOrThrow("timestamp"));
        boolean pinned = c.getInt(c.getColumnIndexOrThrow("pinned")) != 0;
        boolean isNote = c.getInt(c.getColumnIndexOrThrow("is_note")) != 0;
        return new ClipboardItem(id, text, ts, pinned, isNote);
    }

    private void loadGroupIds(List<ClipboardItem> items) {
        if (items.isEmpty()) return;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Simple approach for small lists
        for (ClipboardItem item : items) {
            try (Cursor c = db.query(ClipboardDbHelper.TABLE_ITEM_GROUPS, new String[]{"group_id"},
                    "item_id=?", new String[]{String.valueOf(item.id)}, null, null, null)) {
                while (c.moveToNext()) {
                    item.groupIds.add(c.getLong(0));
                }
            }
        }
    }

    public void close() {
        mDbHelper.close();
    }
}
