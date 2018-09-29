package billy.snxi.mywechat.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ContextUtils {
    /**
     * 获取屏幕的宽度px
     *
     * @param context
     * @return
     */
    public static int getScreenWidthPx(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
}
