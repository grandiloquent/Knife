package euphoria.psycho.knife;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class Bookmark extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public Bookmark(@Nullable Context context) {
        super(context, getDatbaseName(context), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS bookmarks("
                + "_id INTEGER PRIMARY KEY,"
                + "bookmark TEXT NOT NULL UNIQUE"
                + ");");

    }

    public void insert(String bookmark) {
        ContentValues values = new ContentValues();
        values.put("bookmark", bookmark);
        getWritableDatabase().insertWithOnConflict("bookmarks", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void delete(String bookmark) {
        getWritableDatabase().delete("bookmarks", "bookmark=?", new String[]{
                bookmark
        });
    }

    public List<String> fetchBookmarks() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT bookmark FROM bookmarks", null)) {
            List<String> bookmarks = new ArrayList<>();
            while (cursor.moveToNext()) {
                bookmarks.add(cursor.getString(0));
            }
            return bookmarks;
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static String getDatbaseName(Context context) {
        String databaseName = context.getExternalCacheDir().getAbsolutePath() + File.separatorChar + "bookmark.db";

        return databaseName;
    }
}
