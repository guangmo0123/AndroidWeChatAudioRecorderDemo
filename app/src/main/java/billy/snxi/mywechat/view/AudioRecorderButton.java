package billy.snxi.mywechat.view;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import billy.snxi.mywechat.R;

public class AudioRecorderButton extends Button
        implements View.OnLongClickListener, AudioManager.AudioStateLintsener {

    //Y轴滑动的距离界限，超过此距离时，则判断为想要取消
    private static final int DISTANCE_Y_CANCEL = 50;
    //录音文件的存放位置
    private static final String AUDIO_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyWeChat/AudioRecorder";
    //音量等级的最大值
    private static final int AUDIO_VOICE_MAX = 7;
    //录音时间最小值
    private static final float AUDIO_RECORDER_TIME_MIN = 1.0f;

    //按钮的状态
    private static final int STATE_NORMAL = 0x01;  //正常
    private static final int STATE_RECORDING = 0x02;   //录音中
    private static final int STATE_WANT_CANCAL = 0x03; //想要取消

    //handler的msg类型
    private static final int MSG_AUDIO_PREPARED = 0x001;  //音频控件准备完成
    private static final int MSG_AUDIO_VOICE_UPDATE = 0x002;   //更新音频的音量等级
    private static final int MSG_DIALOG_DISMISS = 0x003; //dialog dismiss

    //当前按钮的状态
    private int mCurrentState = STATE_NORMAL;
    //标记是否正在录音
    private boolean isRecoreding = false;
    //标记当前用户是否正在处于wantCancel状态
    private boolean isWantCancelState = false;
    //dialog
    private DialogManager mDialogManager;
    //audio manager
    private AudioManager mAudioManager;
    //更新ui的handler
    private Handler mHandler;
    //更新音量大小的等级的线程
    private Runnable mUpdateVoiceRunnable;
    //是否停止更新音量大小
    private boolean isStopUpdateVoice = false;
    //计算录音的时间
    private float mRecordTime;
    /**
     * 判断录音控件是否进入准备状态，因为若用户按按钮的时间很短，还没触发LongClick时，
     * 则此时AudioManager不再需要进行prepare了；若有触发LongClick时，则AudioManager就需要release了
     */
    private boolean isStartPrepare = false;
    //录音完成后的回调接口
    private AudioRecorderFinishListener mAudioRecorderFinishListener;

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new DialogManager(getContext());
        mAudioManager = AudioManager.getInstance(AUDIO_DIR_PATH);
        mAudioManager.setOnAudioStateLintsener(this);
        setOnLongClickListener(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_AUDIO_PREPARED:
                        isRecoreding = true;
                        //弹出dialog
                        mDialogManager.showRecordingDialog();
                        //使用异步线程更新音量等级，其中还计算了录音的时间
                        new Thread(mUpdateVoiceRunnable).start();
                        break;
                    case MSG_AUDIO_VOICE_UPDATE:
                        mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(AUDIO_VOICE_MAX));
                        break;
                    case MSG_DIALOG_DISMISS:
                        mDialogManager.dismissDialog();
                        resetState();
                        break;
                }
            }
        };
        mUpdateVoiceRunnable = new Runnable() {
            @Override
            public void run() {
                while (isRecoreding) {
                    try {
                        Thread.sleep(100);
                        mRecordTime += 0.1f;
                        //若用户正处于想要取消的状态或者停止更新音量标记为真时，则不更新音量大小
                        if (!isWantCancelState && !isStopUpdateVoice) {
                            mHandler.sendEmptyMessage(MSG_AUDIO_VOICE_UPDATE);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecoreding) {
                    //判断用户是否想要取消
                    isWantCancelState = isWantCancel(x, y);
                    if (isWantCancelState) {
                        changeState(STATE_WANT_CANCAL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isStopUpdateVoice = true;
                //若AudioManager还没有进入prepare状态，则直接reset并返回即可
                if (!isStartPrepare) {
                    resetState();
                    return super.onTouchEvent(event);
                }
                //提示录音时间太短的dialog，有2种情况
                //1、还没开始录音时，AudioManager已经进入prepare过程，但还没prepare完成，而用户又抬起按钮，此时需要提示dialog并release
                //2、录音时间小于最小值时且正处于录音状态时，此时需要相同的操作，若用户此时取消状态，则直接走取消流程，无需提示录音时间太短的dialog
                if (!isRecoreding || (mRecordTime < AUDIO_RECORDER_TIME_MIN && mCurrentState == STATE_RECORDING)) {
                    //提示时间太短的dialog，由于只有AudioManager Prepare完成了才调用了showRecordingDialog()，所以此处需要判断是否需要调用show dialog
                    if (!isRecoreding) {
                        mDialogManager.showRecordingDialog();
                    }
                    mDialogManager.tooShort();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1300);
                    mAudioManager.cancel();
                    return super.onTouchEvent(event);
                }
                //录音状态正常时
                if (mCurrentState == STATE_RECORDING) {     //正常录音并完成
                    mAudioManager.release();
                    //录音完成，回调接口
                    if (mAudioRecorderFinishListener != null) {
                        mAudioRecorderFinishListener.onFinishRecorder(mRecordTime, mAudioManager.getCurrentFilePath());
                    }
                } else if (mCurrentState == STATE_WANT_CANCAL) {    //正常录音，但用户选择取消
                    mAudioManager.cancel();
                }
                mDialogManager.dismissDialog();
                resetState();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 重置view，恢复默认状态
     */
    private void resetState() {
        isRecoreding = false;
        isStartPrepare = false;
        isWantCancelState = false;
        isStopUpdateVoice = false;
        mRecordTime = 0;
        changeState(STATE_NORMAL);
    }

    /**
     * 根据x，y判断用户是否为想要取消录音
     *
     * @param x
     * @param y
     */
    private boolean isWantCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    /**
     * 根据录音状态来进行相应的处理
     *
     * @param state
     */
    private void changeState(int state) {
        //当前状态与传入的一样时，则无需改变
        if (mCurrentState == state) {
            return;
        }
        mCurrentState = state;
        switch (state) {
            case STATE_NORMAL:
                setBackgroundResource(R.drawable.shape_btn_recorder_normal);
                setText(R.string.str_recorder_normal);
                break;
            case STATE_RECORDING:
                setBackgroundResource(R.drawable.shape_btn_recording);
                setText(R.string.str_recording);
                mDialogManager.recording();
                break;
            case STATE_WANT_CANCAL:
                setBackgroundResource(R.drawable.shape_btn_recording);
                setText(R.string.str_recorder_cancel);
                mDialogManager.wantCancel();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        //标记AudioManger已经开始prepare
        isStartPrepare = true;
        //开始准备录音控件
        mAudioManager.prepareAudio();
        return false;
    }

    @Override
    public void preparedDone() {
        //当录音准备完成后，向handler发送准备完成的消息
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    public void setAudioRecorderFinishListener(AudioRecorderFinishListener audioRecorderFinishListener) {
        mAudioRecorderFinishListener = audioRecorderFinishListener;
    }

    /**
     * 录音完成后回调的接口
     */
    public interface AudioRecorderFinishListener {
        void onFinishRecorder(float audioSeconds, String audioFilePath);
    }

}
