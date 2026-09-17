package rkr.simplekeyboard.inputmethod.clipboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Lightweight SQLite helper for clipboard history, notes and groups.
 * Uses only built-in android.database.sqlite — zero extra APK size.
 */
public final class ClipboardDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "lightclip_clipboard.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_ITEMS = "clipboard_items";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_ITEM_GROUPS = "item_groups";

    public ClipboardDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "text TEXT NOT NULL," +
                "timestamp INTEGER NOT NULL," +
                "pinned INTEGER NOT NULL DEFAULT 0," +
                "is_note INTEGER NOT NULL DEFAULT 0" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_GROUPS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL UNIQUE," +
                "created INTEGER NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_ITEM_GROUPS + " (" +
                "item_id INTEGER NOT NULL," +
                "group_id INTEGER NOT NULL," +
                "PRIMARY KEY(item_id, group_id)," +
                "FOREIGN KEY(item_id) REFERENCES " + TABLE_ITEMS + "(id) ON DELETE CASCADE," +
                "FOREIGN KEY(group_id) REFERENCES " + TABLE_GROUPS + "(id) ON DELETE CASCADE" +
                ")");

        // Indexes for fast recent / pinned / search
        db.execSQL("CREATE INDEX idx_items_ts ON " + TABLE_ITEMS + "(timestamp DESC)");
        db.execSQL("CREATE INDEX idx_items_pinned ON " + TABLE_ITEMS + "(pinned)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future migrations
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
