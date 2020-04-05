package com.example.youtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.youtube.R;
import com.example.youtube.listeners.VideoListItemListener;
import com.example.youtube.models.VideoItems;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.ArrayList;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {
    private Context mContext;
    private List<VideoItems> videoItemList = new ArrayList<>();
    private VideoListItemListener itemListener;

    public VideoListAdapter(Context mContext, VideoListItemListener itemListener) {
        this.mContext = mContext;
        this.itemListener = itemListener;
    }

    public void setVideoItemList(List<VideoItems> videoItemList) {
        this.videoItemList = videoItemList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final VideoItems videoItem = videoItemList.get(position);

        holder.thumbnailView.setImageResource(R.drawable.loading_thumbnail);
        holder.videoNameTextView.setText(videoItem.getText());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemListener.videoItemClicked(position, videoItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private YouTubeThumbnailView thumbnailView;
        private TextView videoNameTextView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            videoNameTextView = view.findViewById(R.id.text);
        }
    }
}