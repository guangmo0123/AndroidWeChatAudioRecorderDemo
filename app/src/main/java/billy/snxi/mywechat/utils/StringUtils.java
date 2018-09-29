package billy.snxi.mywechat.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
    public static String getNowDate(String format) {
        if (format == null || format.length() == 0) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
}
