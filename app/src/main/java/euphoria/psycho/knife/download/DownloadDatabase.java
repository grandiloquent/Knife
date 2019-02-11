package euphoria.psycho.knife.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.common.ContextUtils;


public class DownloadDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "infos";
    private final Context mContext;

    private DownloadDatabase(Context context) {
        super(context, getDatabaseFileName(), null, DATABASE_VERSION);
        mContext = context;
    }

    public synchronized void delete(DownloadInfo downloadInfo) {
        getWritableDatabase().delete(TABLE_NAME, "_id=?", new String[]{Long.toString(downloadInfo._id)});
    }

    public long insert(DownloadInfo downloadInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", downloadInfo.status);
        contentValues.put("fileName", downloadInfo.fileName);
        contentValues.put("filePath", downloadInfo.filePath);
        contentValues.put("bytesReceived", downloadInfo.bytesReceived);
        contentValues.put("bytesTotal", downloadInfo.bytesTotal);
        contentValues.put("url", downloadInfo.url);
        contentValues.put("speed", downloadInfo.speed);
        contentValues.put("message", downloadInfo.message);

        return getWritableDatabase().insert(TABLE_NAME, null, contentValues);

    }

    public List<DownloadInfo> queryPendingTask() {
        List<DownloadInfo> downloadInfos = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT  _id,status,fileName,filePath,bytesReceived,bytesTotal,url,message FROM infos WHERE status!=0", null)) {

            while (cursor.moveToNext()) {
                DownloadInfo downloadInfo = new DownloadInfo();
                downloadInfo._id = cursor.getLong(0);
                downloadInfo.status = cursor.getInt(1);
                downloadInfo.fileName = cursor.getString(2);
                downloadInfo.filePath = cursor.getString(3);
                downloadInfo.bytesReceived = cursor.getLong(4);
                downloadInfo.bytesTotal = cursor.getLong(5);
                downloadInfo.url = cursor.getString(6);
                downloadInfo.message = cursor.getString(7);
                downloadInfos.add(downloadInfo);
            }
        }
        return downloadInfos;
    }

    private static String getDatabaseFileName() {

        return new File(Environment.getExternalStorageDirectory(), "infos.db").getAbsolutePath();
    }

    public static DownloadDatabase instance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (\n" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "status INTEGER,\n" +
                "fileName TEXT NOT NULL,\n" +
                "filePath TEXT NOT NULL UNIQUE,\n" +
                "bytesReceived INTEGER,\n" +
                "bytesTotal INTEGER,\n" +
                "url TEXT NOT NULL,\n" +
                "speed INTEGER,\n" +
                "message TEXT" +
                ");");
    }

    public synchronized void update(DownloadInfo downloadInfo) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("status", downloadInfo.status);
        contentValues.put("bytesReceived", downloadInfo.bytesReceived);
        contentValues.put("bytesTotal", downloadInfo.bytesTotal);
        contentValues.put("speed", downloadInfo.speed);
        contentValues.put("message", downloadInfo.message);
        getWritableDatabase().update(TABLE_NAME, contentValues, "_id=?", new String[]{Long.toString(downloadInfo._id)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// SELECT _id,status,fileName,filePath,bytesReceived,bytesTotal,url,speed FROM table_name;
    }

    private static class Singleton {
        private static final DownloadDatabase INSTANCE =
                new DownloadDatabase(ContextUtils.getApplicationContext());
    }
}
