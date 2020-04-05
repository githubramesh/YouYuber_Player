/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.youtube;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youtube.adapter.VideoListAdapter;
import com.example.youtube.listeners.VideoListItemListener;
import com.example.youtube.models.VideoItems;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple YouTube Android API demo application demonstrating the use of {@link YouTubePlayer}
 * programmatic controls.
 */
public class PlayerControlsDemoActivity extends YouTubeFailureRecoveryActivity implements
        View.OnClickListener,
        TextView.OnEditorActionListener,
        CompoundButton.OnCheckedChangeListener,
        VideoListItemListener {

    private static final String KEY_CURRENTLY_SELECTED_ID = "currentlySelectedId";

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer player;
    private TextView stateText;
    private Button playButton;
    private Button pauseButton;
    private EditText skipTo;
    private TextView eventLog;
    private StringBuilder logString;
    private RadioGroup styleRadioGroup;

    private MyPlaylistEventListener playlistEventListener;
    private MyPlayerStateChangeListener playerStateChangeListener;
    private MyPlaybackEventListener playbackEventListener;

    private VideoListAdapter videoListAdapter;
    private VideoListAsyncTask videoListAsyncTask;
    List<VideoItems> videoItemList = new ArrayList<>();
    private List<String> videoIdsList = new ArrayList<>();

    private static int currentlySelectedPosition;
    private String currentlySelectedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.player_controls_demo);

        youTubePlayerView = findViewById(R.id.youtube_view);
        RecyclerView videoListRecyclerView = findViewById(R.id.video_list_recycler_view);
        stateText = findViewById(R.id.state_text);
        playButton = findViewById(R.id.play_button);
        pauseButton = findViewById(R.id.pause_button);
        skipTo = findViewById(R.id.skip_to_text);
        eventLog = findViewById(R.id.event_log);

        styleRadioGroup = findViewById(R.id.style_radio_group);
        ((RadioButton) findViewById(R.id.style_default)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.style_minimal)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.style_chromeless)).setOnCheckedChangeListener(this);
        logString = new StringBuilder();

        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        skipTo.setOnEditorActionListener(this);

        youTubePlayerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

        playlistEventListener = new MyPlaylistEventListener();
        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();

        setControlsEnabled(false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        videoListRecyclerView.setLayoutManager(mLayoutManager);
        videoListRecyclerView.setHasFixedSize(true);
        videoListRecyclerView.setNestedScrollingEnabled(false);
        videoListAdapter = new VideoListAdapter(this, this);
        videoListRecyclerView.setAdapter(videoListAdapter);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        this.player = player;
        player.setPlaylistEventListener(playlistEventListener);
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);

        if (!wasRestored) {
            if (videoIdsList != null) {
                player.cueVideos(videoIdsList);
            }
            if (videoItemList != null) {
                playVideoAtSelection(currentlySelectedPosition, videoItemList.get(currentlySelectedPosition));
            }
        }

        setControlsEnabled(true);
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubePlayerView;
    }

    private void playVideoAtSelection(int position, VideoItems videoItem) {
        currentlySelectedPosition = position;
        if (videoItem != null) {
            String videoId = videoItem.getVideoId();
            if (!videoId.equals(currentlySelectedId) && player != null) {
                currentlySelectedId = videoId;
                if (videoItem.isPlaylist()) {
                    player.cuePlaylist(videoId);
                } else {
                    player.cueVideo(videoId);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == playButton) {
            player.play();
        } else if (v == pauseButton) {
            player.pause();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v == skipTo) {
            int skipToSecs = parseInt(skipTo.getText().toString(), 0);
            player.seekToMillis(skipToSecs * 1000);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(skipTo.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && player != null) {
            switch (buttonView.getId()) {
                case R.id.style_default:
                    player.setPlayerStyle(PlayerStyle.DEFAULT);
                    break;
                case R.id.style_minimal:
                    player.setPlayerStyle(PlayerStyle.MINIMAL);
                    break;
                case R.id.style_chromeless:
                    player.setPlayerStyle(PlayerStyle.CHROMELESS);
                    break;
            }
        }
    }

    @Override
    public void videoItemClicked(int position, VideoItems videoItem) {
        playVideoAtSelection(position, videoItem);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(KEY_CURRENTLY_SELECTED_ID, currentlySelectedId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        currentlySelectedId = state.getString(KEY_CURRENTLY_SELECTED_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoListAsyncTask != null) {
            videoListAsyncTask.cancel(true);
        }
        videoListAsyncTask = new VideoListAsyncTask();
        videoListAsyncTask.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoListAsyncTask != null) {
            videoListAsyncTask.cancel(true);
        }
    }

    private void updateText() {
        stateText.setText(String.format("Current state: %s %s %s",
                playerStateChangeListener.playerState, playbackEventListener.playbackState,
                playbackEventListener.bufferingState));
    }

    private void log(String message) {
        logString.append(message + "\n");
        eventLog.setText(logString);
    }

    private void setControlsEnabled(boolean enabled) {
        playButton.setEnabled(enabled);
        pauseButton.setEnabled(enabled);
        skipTo.setEnabled(enabled);
        for (int i = 0; i < styleRadioGroup.getChildCount(); i++) {
            styleRadioGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    private static final int parseInt(String intString, int defaultValue) {
        try {
            return intString != null ? Integer.valueOf(intString) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        return (hours == 0 ? "" : hours + ":")
                + String.format("%02d:%02d", minutes % 60, seconds % 60);
    }

    private String getTimesText() {
        int currentTimeMillis = player.getCurrentTimeMillis();
        int durationMillis = player.getDurationMillis();
        return String.format("(%s/%s)", formatTime(currentTimeMillis), formatTime(durationMillis));
    }

    private final class MyPlaylistEventListener implements PlaylistEventListener {
        @Override
        public void onNext() {
            log("NEXT VIDEO");
        }

        @Override
        public void onPrevious() {
            log("PREVIOUS VIDEO");
        }

        @Override
        public void onPlaylistEnded() {
            log("PLAYLIST ENDED");
        }
    }

    private final class MyPlaybackEventListener implements PlaybackEventListener {
        String playbackState = "NOT_PLAYING";
        String bufferingState = "";

        @Override
        public void onPlaying() {
            playbackState = "PLAYING";
            updateText();
            log("\tPLAYING " + getTimesText());
        }

        @Override
        public void onBuffering(boolean isBuffering) {
            bufferingState = isBuffering ? "(BUFFERING)" : "";
            updateText();
            log("\t\t" + (isBuffering ? "BUFFERING " : "NOT BUFFERING ") + getTimesText());
        }

        @Override
        public void onStopped() {
            playbackState = "STOPPED";
            updateText();
            log("\tSTOPPED");
        }

        @Override
        public void onPaused() {
            playbackState = "PAUSED";
            updateText();
            log("\tPAUSED " + getTimesText());
        }

        @Override
        public void onSeekTo(int endPositionMillis) {
            log(String.format("\tSEEKTO: (%s/%s)",
                    formatTime(endPositionMillis),
                    formatTime(player.getDurationMillis())));
        }
    }

    private final class MyPlayerStateChangeListener implements PlayerStateChangeListener {
        String playerState = "UNINITIALIZED";

        @Override
        public void onLoading() {
            playerState = "LOADING";
            updateText();
            log(playerState);
        }

        @Override
        public void onLoaded(String videoId) {
            playerState = String.format("LOADED %s", videoId);
            updateText();
            log(playerState);
        }

        @Override
        public void onAdStarted() {
            playerState = "AD_STARTED";
            updateText();
            log(playerState);
        }

        @Override
        public void onVideoStarted() {
            playerState = "VIDEO_STARTED";
            updateText();
            log(playerState);
        }

        @Override
        public void onVideoEnded() {
            playerState = "VIDEO_ENDED";
            updateText();
            log(playerState);
        }

        @Override
        public void onError(ErrorReason reason) {
            playerState = "ERROR (" + reason + ")";
            if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // When this error occurs the player is released and can no longer be used.
                player = null;
                setControlsEnabled(false);
            }
            updateText();
            log(playerState);
        }

    }

    public class VideoListAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            videoItemList.add(new VideoItems("Androidify App", "irH3OSOskcE", false));
            videoIdsList.add("irH3OSOskcE");
            videoItemList.add(new VideoItems("Playlist: Google I/O 2012", "PL56D792A831D0C362", false));
            videoIdsList.add("PL56D792A831D0C362");
            videoItemList.add(new VideoItems("YouTube Collection", "Y_UmWdcTrrc", false));
            videoIdsList.add("Y_UmWdcTrrc");
            videoItemList.add(new VideoItems("GMail Tap", "1KhZKNZO8mQ", false));
            videoIdsList.add("1KhZKNZO8mQ");
            videoItemList.add(new VideoItems("Chrome Multitask", "UiLSiqyDf4Y", false));
            videoIdsList.add("UiLSiqyDf4Y");
            videoItemList.add(new VideoItems("Google Fiber", "re0VRK6ouwI", false));
            videoIdsList.add("re0VRK6ouwI");
            videoItemList.add(new VideoItems("Autocompleter", "blB_X38YSxQ", false));
            videoIdsList.add("blB_X38YSxQ");
            videoItemList.add(new VideoItems("GMail Motion", "Bu927_ul_X0", false));
            videoIdsList.add("Bu927_ul_X0");
            videoItemList.add(new VideoItems("Translate for Animals", "3I24bSteJpw", false));
            videoIdsList.add("3I24bSteJpw");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (videoListAdapter != null) {
                videoListAdapter.setVideoItemList(videoItemList);
            }

            if (player != null) {
                player.cueVideos(videoIdsList);
            }
        }
    }
}