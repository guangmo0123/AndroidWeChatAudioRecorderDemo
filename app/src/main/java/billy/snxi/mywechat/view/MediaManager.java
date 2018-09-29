package billy.snxi.mywechat.view;

import android.media.AudioManager;
import android.media.MediaPlayer;

/**
 * 播放指定的录音文件<br/>
 * 步骤：getInstance()->playSound()->pause() || reset()->release()
 */
public class MediaManager {

    private static MediaManager mInstance;
    private MediaPlayer mMediaPlayer;

    private MediaManager(MediaPlayer.OnCompletionListener onCompletionListener) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mMediaPlayer.reset();
                return true;
            }
        });
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    /**
     * 单例模式
     */
    public static MediaManager getInstance(MediaPlayer.OnCompletionListener onCompletionListener) {
        if (mInstance == null) {
            synchronized (MediaManager.class) {
                if (mInstance == null) {
                    mInstance = new MediaManager(onCompletionListener);
                }
            }
        }
        return mInstance;
    }

    public void playSound(String filePath) {
        if (mMediaPlayer == null) {
            return;
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer == null) {
            return false;
        }
        return mMediaPlayer.isPlaying();
    }

    public void pause() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void release() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
    }


}
