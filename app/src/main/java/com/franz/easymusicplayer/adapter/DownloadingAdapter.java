package com.franz.easymusicplayer.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.franz.easymusicplayer.R;
import com.franz.easymusicplayer.bean.DownloadBean;


import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class DownloadingAdapter extends RecyclerView.Adapter<DownloadingAdapter.ViewHolder> implements View.OnClickListener {
    private List<DownloadBean> beanList;
    private OnDownloadingClickListener listener = null;
    private boolean flagStatus = false;
    private Context context;

    public DownloadingAdapter(List<DownloadBean> beanList, Context context) {
        this.beanList = beanList;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.downloading_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        DownloadBean bean = beanList.get(position);

        if (!flagStatus) {
            holder.downloadingSelect.setVisibility(View.GONE);
        } else {
            holder.downloadingSelect.setVisibility(View.VISIBLE);
        }

        /**
         * default false,so init the icon be normal status
         * */
        if (bean.getSelect()) {
            holder.downloadingSelect.setImageResource(R.drawable.icon_select_yes);
        } else {
            holder.downloadingSelect.setImageResource(R.drawable.icon_select_not);
        }


        holder.downloadingSong.setText(bean.getSongName());
        holder.downloadingSinger.setText(bean.getSinger());
        holder.downloadingBar.setProgress(bean.getProgress());
        holder.downloadingSpeed.setText(bean.getSpeed());

        Glide.with(holder.downloadingImg)
                .load(bean.getCover())
                .into(holder.downloadingImg);

        holder.layout.setTag(position);


        holder.downloadingStatus.setText("Waiting");
        holder.downloadingStatus.setTextColor(context.getColor(R.color.waiting_download));
        holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_seek));
        holder.downloadingSpeed.setVisibility(View.GONE);

//        if (target.getTaskState() == STATE_RUNNING) {
//            holder.downloadingStatus.setText("Downloading");
//            holder.downloadingStatus.setTextColor(context.getColor(R.color.starting_download));
//            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_starting));
//            holder.downloadingSpeed.setVisibility(View.VISIBLE);
//        } else if (target.getTaskState() == STATE_WAIT) {
//            holder.downloadingStatus.setText("Waiting");
//            holder.downloadingStatus.setTextColor(context.getColor(R.color.waiting_download));
//            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_pause));
//            holder.downloadingSpeed.setVisibility(View.GONE);
//        } else if (target.getTaskState() == STATE_FAIL) {
//            holder.downloadingStatus.setText("Failed");
//            holder.downloadingStatus.setTextColor(context.getColor(R.color.failed_download));
//            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_pause));
//            holder.downloadingSpeed.setVisibility(View.GONE);
//        } else if (target.getTaskState() == STATE_COMPLETE) {
//
//        } else if (target.getTaskState() == STATE_CANCEL) {
//
//        } else if (target.getTaskState() == STATE_STOP) {
//            holder.downloadingStatus.setText("Pause");
//            holder.downloadingStatus.setTextColor(context.getColor(R.color.pause_download));
//            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_pause));
//            holder.downloadingSpeed.setVisibility(View.GONE);
//        } else {
//            holder.downloadingStatus.setText("Network Error");
//            holder.downloadingStatus.setTextColor(context.getColor(R.color.waiting_download));
//            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_pause));
//            holder.downloadingSpeed.setVisibility(View.GONE);
//        }

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position, @NonNull @NotNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else if ((int)payloads.get(0) == 1){
            DownloadBean bean = beanList.get(position);
            holder.downloadingBar.setProgress(bean.getProgress());
            holder.downloadingSpeed.setText(bean.getSpeed());
            holder.downloadingStatus.setText("Downloading");
            holder.downloadingStatus.setTextColor(context.getColor(R.color.starting_download));
            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_starting));
            holder.downloadingSpeed.setVisibility(View.VISIBLE);
        }else if ((int)payloads.get(0) == 2){
            DownloadBean bean = beanList.get(position);
            holder.downloadingBar.setProgress(bean.getProgress());
            holder.downloadingStatus.setText("Pause");
            holder.downloadingStatus.setTextColor(context.getColor(R.color.pause_download));
            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_pause));
            holder.downloadingSpeed.setVisibility(View.GONE);
        }else if ((int)payloads.get(0) == 3){
            DownloadBean bean = beanList.get(position);
            holder.downloadingBar.setProgress(bean.getProgress());
            holder.downloadingStatus.setText("Failed");
            holder.downloadingStatus.setTextColor(context.getColor(R.color.failed_download));
            holder.downloadingBar.setProgressDrawable(context.getDrawable(R.drawable.layer_list_pause));
            holder.downloadingSpeed.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return beanList.size();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            int pos = (int) v.getTag();
            if (pos >= beanList.size()) return;
            listener.onClickListener(v, pos);
        }
    }

    public boolean isFlagStatus() {
        return flagStatus;
    }

    public void setFlagStatus(boolean flagStatus) {
        this.flagStatus = flagStatus;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView downloadingSong, downloadingSinger, downloadingStatus, downloadingSpeed;
        private ImageView downloadingSelect, downloadingImg;
        private ProgressBar downloadingBar;
        private LinearLayout layout;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            downloadingSong = itemView.findViewById(R.id.downloadingSong);
            downloadingSinger = itemView.findViewById(R.id.downloadingSinger);
            downloadingStatus = itemView.findViewById(R.id.downloadingStatus);
            downloadingSpeed = itemView.findViewById(R.id.downloadingSpeed);
            downloadingSelect = itemView.findViewById(R.id.downloadingSelect);
            downloadingImg = itemView.findViewById(R.id.downloadingImg);
            downloadingBar = itemView.findViewById(R.id.downloadingProgress);
            layout = itemView.findViewById(R.id.downloadingLayout);
            layout.setOnClickListener(DownloadingAdapter.this);
        }

    }

    public void setItemClickListener(OnDownloadingClickListener listener) {
        this.listener = listener;
    }

    public interface OnDownloadingClickListener {
        void onClickListener(View view, int pos);
    }
    public void deleteCompletedItem(int index){
       beanList.remove(index);
       notifyDataSetChanged();
    }
}
