package billy.snxi.mywechat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecorderDBHelper extends SQLiteOpenHelper {

    public static final String DB_FILE_NAME_RECORDER = "recorder_db";
    public static final String DB_TABLE_NAME_RECORDER = "recorder";

    private static final String SQL_TABLE_CREATE_RECORDER = "CREATE TABLE " + DB_TABLE_NAME_RECORDER + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "date TEXT COLLATE NOCASE,seconds FLOAT,filepath TEXT COLLATE NOCASE,readedflag INTEGER"
            + ")";
    ;

    public RecorderDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_TABLE_CREATE_RECORDER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
