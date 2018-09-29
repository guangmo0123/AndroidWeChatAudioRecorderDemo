package billy.snxi.mywechat.utils;

import java.io.File;

public class FileUtils {

    /**
     * 判断文件是否存在
     *
     * @param filePath
     * @return 若文件不存在或路径不是文件，则return false，否则return true
     */
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        return true;
    }

    /**
     * 删除磁盘上的录音文件
     *
     * @param filePath
     */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

}
