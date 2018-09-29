package billy.snxi.mywechat.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import billy.snxi.mywechat.bean.AudioRecorderBean;

/**
 * 录音记录数据库处理类，单例模式
 */
public class RecorderDBUtils {
    private static RecorderDBUtils mInstance;
    private Context mContext;
    private RecorderDBHelper mDbHelpler;
    private SQLiteDatabase mDB;
    //数据库文件名
    private String mDBFileName;
    //存储录音记录的表名
    private static final String DB_TABLE_NAME_RECORDER = RecorderDBHelper.DB_TABLE_NAME_RECORDER;

    private RecorderDBUtils(Context context, String dbFileName) {
        this.mContext = context;
        mDBFileName = dbFileName;
        //创建dbHelper
        mDbHelpler = new RecorderDBHelper(context, dbFileName, null, 1);
    }

    /**
     * 单例模式
     *
     * @param context
     * @return
     */
    public static RecorderDBUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RecorderDBUtils.class) {
                if (mInstance == null) {
                    mInstance = new RecorderDBUtils(context, RecorderDBHelper.DB_FILE_NAME_RECORDER);
                }
            }
        }
        return mInstance;
    }

    /**
     * 保存录音记录至数据库中
     */
    public void saveRecorder(AudioRecorderBean bean) {
        mDB = mDbHelpler.getWritableDatabase();
        String sql = "INSERT INTO " + DB_TABLE_NAME_RECORDER + " (date, seconds, filepath, readedflag) " +
                "VALUES (?, ?, ?, ?)";
        Object[] params = new Object[]{bean.getDate(), bean.getSeconds(), bean.getFilePath(), bean.getReadedFlag()};
        mDB.execSQL(sql, params);
        mDB.close();
    }

    /**
     * 获取录音记录List<AudioRecorderBean>
     *
     * @param list
     */
    public void queryRecorder(List<AudioRecorderBean> list) {
        list.clear();
        mDB = mDbHelpler.getReadableDatabase();
        String sql = "Select date,seconds,filepath,readedflag from " + DB_TABLE_NAME_RECORDER + " ORDER BY date";
        Cursor cursor = mDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            list.add(new AudioRecorderBean(
                    cursor.getString(0),
                    cursor.getFloat(1),
                    cursor.getString(2),
                    cursor.getInt(3)));
        }
        cursor.close();
        mDB.close();
    }

    /**
     * 更新指定语音记录为已读
     *
     * @param filePath
     */
    public void updateRecorderReaded(String filePath) {
        mDB = mDbHelpler.getWritableDatabase();
        String sql = "UPDATE " + DB_TABLE_NAME_RECORDER + " SET readedflag=1 WHERE filepath=?";
        mDB.execSQL(sql, new String[]{filePath});
        mDB.close();
    }

    /**
     * 从数据库中删除指定的filepath的记录和实际存储的文件
     *
     * @param filePath
     */
    public void deleteDBRecorder(String filePath) {
        mDB = mDbHelpler.getWritableDatabase();
        String sql = "Delete from " + DB_TABLE_NAME_RECORDER + " WHERE filepath=?";
        mDB.execSQL(sql, new String[]{filePath});
        mDB.close();
    }

}
