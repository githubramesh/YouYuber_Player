package com.example.youtube.models;

public class VideoItems {
    private String text;
    private String videoId;
    private boolean isPlaylist;

    public VideoItems(String text, String videoId, boolean isPlaylist) {
        this.text = text;
        this.videoId = videoId;
        this.isPlaylist = isPlaylist;
    }

    public String getText() {
        return text;
    }

    public String getVideoId() {
        return videoId;
    }

    public boolean isPlaylist() {
        return isPlaylist;
    }
}