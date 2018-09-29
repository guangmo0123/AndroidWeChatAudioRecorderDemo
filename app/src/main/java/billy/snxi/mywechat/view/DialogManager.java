package billy.snxi.mywechat.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import billy.snxi.mywechat.R;

public class DialogManager {

    //dialog
    private Dialog mDialog;
    //dialog中的2个图片和一个文字
    private ImageView mImageViewIcon;
    private ImageView mImageViewVoice;
    private TextView mTextViewLabel;
    //context
    private Context mContext;

    public DialogManager(Context context) {
        this.mContext = context;
    }

    /**
     * 显示dialog
     */
    public void showRecordingDialog() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.layout_dialog_recorder, null);

        mDialog = new Dialog(mContext, R.style.Theme_AudioDialog);
        mDialog.setContentView(dialogView);

        mImageViewIcon = mDialog.findViewById(R.id.iv_dialog_icon);
        mImageViewVoice = mDialog.findViewById(R.id.iv_dialog_voice);
        mTextViewLabel = mDialog.findViewById(R.id.tv_dialog_label);
        mDialog.show();
    }

    /**
     * 正在录音时的dialog
     */
    public void recording() {
        if (mDialog != null && mDialog.isShowing()) {
            mImageViewIcon.setVisibility(View.VISIBLE);
            mImageViewVoice.setVisibility(View.VISIBLE);
            //设置dialog的2个图片和文字
            mImageViewIcon.setImageResource(R.mipmap.recorder);
            mImageViewVoice.setImageResource(R.mipmap.v1);
            mTextViewLabel.setText(R.string.str_dialog_recording);
        }
    }

    /**
     * 想要取消录音的dialog
     */
    public void wantCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            mImageViewIcon.setVisibility(View.VISIBLE);
            mImageViewVoice.setVisibility(View.GONE);

            mImageViewIcon.setImageResource(R.mipmap.cancel);
            mTextViewLabel.setText(R.string.str_dialog_record_cancel);
        }
    }

    /**
     * 录音时间太短时的dialog
     */
    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            mImageViewIcon.setVisibility(View.VISIBLE);
            mImageViewVoice.setVisibility(View.GONE);

            mImageViewIcon.setImageResource(R.mipmap.voice_to_short);
            mTextViewLabel.setText(R.string.str_dialog_recorder_too_short);
        }
    }

    /**
     * 正在录音时用户音量改变时更新音量图标
     */
    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
            mImageViewVoice.setVisibility(View.VISIBLE);
            //通过资源id的名称来反向查找资源的id
            int voiceResID = mContext.getResources().getIdentifier("v" + level,
                    "mipmap", mContext.getPackageName());
            mImageViewVoice.setImageResource(voiceResID);
        }
    }

    /**
     * 关闭dialog
     */
    public void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
