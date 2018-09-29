package billy.snxi.mywechat.view;

import android.media.MediaRecorder;

import java.io.File;
import java.util.UUID;

public class AudioManager {
    //音频控件
    private MediaRecorder mMediaRecorder;
    //音频文件的父目录
    private String mDirPath;
    //当前文件的路径
    private String mCurrentFilePath;
    private static AudioManager mInstance;
    //当音频控件完成某些操作时对外的回调接口
    private AudioStateLintsener mAudioStateLintsener;
    //标记MediaRecorder是否已经处于prepared状态
    private boolean isPrepared;

    private AudioManager(String dirPath) {
        mDirPath = dirPath;
        init();
    }

    /**
     * 单例模式
     */
    public static AudioManager getInstance(String dirPath) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dirPath);
                }
            }
        }
        return mInstance;
    }

    private void init() {
        File dir = new File(mDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 初始化MediaRecorder控件，并使之进入prepared状态
     */
    public void prepareAudio() {
        try {
            //当前录音的文件
            File file = new File(mDirPath, getFileName());
            mCurrentFilePath = file.getAbsolutePath();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOutputFile(mCurrentFilePath);
            //设置音频源为麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频的文件格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //设置音频的文件编码
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //准备完成
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isPrepared = true;
            if (mAudioStateLintsener != null) {
                mAudioStateLintsener.preparedDone();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取音频的音量等级
     *
     * @return value in [1-7]
     */
    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            try {
                //getMaxAmplitude() 值的范围为 1-32767
                int voiceLevel = maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
                //不能超过指定的maxLevel
                voiceLevel = Math.min(voiceLevel, maxLevel);
                return voiceLevel;
            } catch (Exception e) {
            }
        }
        return 1;
    }

    /**
     * 释放MediaRecorder控件资源
     */
    public void release() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
        isPrepared = false;
    }

    /**
     * 取消MediaRecorder录音
     */
    public void cancel() {
        release();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            if (file.exists()) {
                file.delete();
            }
            mCurrentFilePath = null;
        }
    }

    /**
     * 返回随机的音频文件的文件名
     */
    private String getFileName() {
        return UUID.randomUUID().toString() + ".amr";
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    public void setOnAudioStateLintsener(AudioStateLintsener mAudioStateLintsener) {
        this.mAudioStateLintsener = mAudioStateLintsener;
    }

    /**
     * 回调接口
     */
    public interface AudioStateLintsener {
        void preparedDone();
    }

}
