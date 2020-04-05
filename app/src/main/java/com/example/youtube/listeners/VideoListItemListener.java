package com.example.youtube.listeners;

import com.example.youtube.models.VideoItems;

public interface VideoListItemListener {
    void videoItemClicked(int position, VideoItems videoItem);
}