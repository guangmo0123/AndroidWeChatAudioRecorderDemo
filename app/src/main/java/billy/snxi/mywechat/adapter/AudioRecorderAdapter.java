package billy.snxi.mywechat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import billy.snxi.mywechat.R;
import billy.snxi.mywechat.bean.AudioRecorderBean;
import billy.snxi.mywechat.utils.ContextUtils;

public class AudioRecorderAdapter extends BaseAdapter {

    //录音条在listview的item中最长显示的宽度与屏幕宽度占比
    private static final float LAYOUT_AUDIO_LENGHT_MAX = 0.7f;
    private static final float LAYOUT_AUDIO_LENGHT_MIN = 0.15f;

    private List<AudioRecorderBean> mDatas;
    private LayoutInflater mInflater;
    private int mScreenWidth;

    public AudioRecorderAdapter(Context context, List<AudioRecorderBean> datas) {
        this.mDatas = datas;
        mInflater = LayoutInflater.from(context);
        mScreenWidth = ContextUtils.getScreenWidthPx(context);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_chat_msg_to, null);
            holder.tv_recorder_date = convertView.findViewById(R.id.tv_recorder_date);
            holder.view_recorder_no_readed = convertView.findViewById(R.id.view_recorder_no_readed);
            holder.tv_recorder_second = convertView.findViewById(R.id.tv_recorder_second);
            holder.flayout_recorder_length = convertView.findViewById(R.id.flayout_recorder_length);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AudioRecorderBean bean = mDatas.get(position);
        holder.tv_recorder_date.setText(bean.getDate());
        //是否已读标记
        if (bean.getReadedFlag() == 0) {
            holder.view_recorder_no_readed.setVisibility(View.VISIBLE);
        } else {
            holder.view_recorder_no_readed.setVisibility(View.GONE);
        }
        holder.tv_recorder_second.setText((int) bean.getSeconds() + "\"");
        //设置录音条的长度
        ViewGroup.LayoutParams lp = holder.flayout_recorder_length.getLayoutParams();
        lp.width = getRecorderViewWidth(bean.getSeconds());
        Log.d("AudioRecorderAdapter", "seconds:" + bean.getSeconds() + ", mScreenWidth:" + mScreenWidth + ", lp.width:" + lp.width);
        return convertView;
    }

    /**
     * 根据录音时长来计算view中录音条的长度
     *
     * @param seconds
     * @return
     */
    private int getRecorderViewWidth(float seconds) {
        int result = (int) (mScreenWidth * LAYOUT_AUDIO_LENGHT_MIN + mScreenWidth * seconds / 60);
        result = (int) Math.min(result, mScreenWidth * LAYOUT_AUDIO_LENGHT_MAX);
        return result;
    }

    private class ViewHolder {
        TextView tv_recorder_date;
        View view_recorder_no_readed;
        TextView tv_recorder_second;
        FrameLayout flayout_recorder_length;
    }

}
