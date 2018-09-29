package billy.snxi.mywechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import billy.snxi.mywechat.adapter.AudioRecorderAdapter;
import billy.snxi.mywechat.bean.AudioRecorderBean;
import billy.snxi.mywechat.db.RecorderDBUtils;
import billy.snxi.mywechat.utils.FileUtils;
import billy.snxi.mywechat.utils.StringUtils;
import billy.snxi.mywechat.view.AudioRecorderButton;
import billy.snxi.mywechat.view.MediaManager;

/**
 * 一个仿微信的语音录入界面
 *
 * @author Billy
 */
public class MainActivity extends Activity {

    private Context mContext;
    private ListView mLvChat;
    private AudioRecorderButton mBtnRecorder;
    private List<AudioRecorderBean> mDatas;
    private MediaManager mMediaManager;
    private AudioRecorderAdapter mAdapter;
    private View mLastAnimView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initAdapter();
        initEvent();
    }

    private void initView() {
        mLvChat = findViewById(R.id.lv_chat);
        mBtnRecorder = findViewById(R.id.btn_recorder);
    }

    private void initAdapter() {
        mDatas = new ArrayList<>();
        //从数据库中获取录音记录
        RecorderDBUtils.getInstance(mContext).queryRecorder(mDatas);
        mAdapter = new AudioRecorderAdapter(mContext, mDatas);
        mLvChat.setAdapter(mAdapter);
        //自动滚动到最后一个item
        mLvChat.setSelection(mDatas.size() - 1);
    }

    private void initEvent() {
        //录音完成后的回调，添加并保存数据
        mBtnRecorder.setAudioRecorderFinishListener(new AudioRecorderButton.AudioRecorderFinishListener() {
            @Override
            public void onFinishRecorder(float audioSeconds, String audioFilePath) {
                String date = StringUtils.getNowDate(null);
                AudioRecorderBean bean = new AudioRecorderBean(date, audioSeconds, audioFilePath, 0);
                //保存录音记录至数据库中
                RecorderDBUtils.getInstance(mContext).saveRecorder(bean);
                //添加至list中
                mDatas.add(bean);
                mAdapter.notifyDataSetChanged();
                //自动滚动到最后一个item
                mLvChat.setSelection(mDatas.size() - 1);
            }
        });
        //listview item单击监听，播放录音
        mLvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //当前的item view
                View currentView = view.findViewById(R.id.view_voice_anim);
                //停止上一个item语音和动画的播放
                stopPlayViewAnim();
                //若上一个录音正在播放，并且当前view与上个view是同一个，则只是停止播放，否则停止播放上个view 录音，开始播放当前view 录音
                if (mMediaManager.isPlaying()) {
                    mMediaManager.pause();
                    if (currentView == mLastAnimView) {
                        mLastAnimView = null;
                        return;
                    }
                }

                AudioRecorderBean bean = mDatas.get(position);
                //录音文件不存在时
                if (!FileUtils.isFileExists(bean.getFilePath())) {
                    Toast.makeText(mContext, "该条语音的音频文件不存在，无法播放语音！", Toast.LENGTH_SHORT).show();
                    return;
                }
                //将录音记录标记已读
                if (bean.getReadedFlag() == 0) {
                    RecorderDBUtils.getInstance(mContext).updateRecorderReaded(bean.getFilePath());
                    bean.setReadedFlag(1);
                    mAdapter.notifyDataSetChanged();
                }
                currentView.setBackgroundResource(R.drawable.anim_audio_recorder_play);
                AnimationDrawable animDrawable = (AnimationDrawable) currentView.getBackground();
                animDrawable.start();
                //播放录音
                mMediaManager.playSound(bean.getFilePath());
                mLastAnimView = currentView;
            }
        });
        //MediaManager创建单例对象，并设置录音播放完成后的回调接口
        mMediaManager = MediaManager.getInstance(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlayViewAnim();
            }
        });
        //listview item长按监听，删除记录
        mLvChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("确定要删除本条语音记录？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AudioRecorderBean bean = mDatas.get(position);
                                //删除数据库记录和实际文件
                                RecorderDBUtils.getInstance(mContext).deleteDBRecorder(bean.getFilePath());
                                FileUtils.deleteFile(bean.getFilePath());
                                mDatas.remove(position);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(mContext, "语音记录删除成功！", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //由于MediaManager是单例模式且存在同步锁，因此可能存在单例对象还未创建完成时，用户就退出的情况，因此需要null判断
        if (mMediaManager != null) {
            mMediaManager.pause();
        }
        stopPlayViewAnim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaManager != null) {
            mMediaManager.release();
        }
    }

    /**
     * 停止录音播放时的动画
     */
    private void stopPlayViewAnim() {
        if (mLastAnimView != null) {
            mLastAnimView.setBackgroundResource(R.mipmap.adj);
        }
    }

}
