package billy.snxi.mywechat.bean;

public class AudioRecorderBean {
    private String date;
    private float seconds;
    private String filePath;
    private int readedFlag = 0;

    public AudioRecorderBean(String date, float seconds, String filePath, int readedFlag) {
        this.date = date;
        this.seconds = seconds;
        this.filePath = filePath;
        this.readedFlag = readedFlag;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getSeconds() {
        return seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getReadedFlag() {
        return readedFlag;
    }

    public void setReadedFlag(int readedFlag) {
        this.readedFlag = readedFlag;
    }
}
