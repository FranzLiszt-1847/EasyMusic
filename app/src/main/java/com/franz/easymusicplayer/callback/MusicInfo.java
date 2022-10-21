package com.franz.easymusicplayer.callback;

public interface MusicInfo {
    void onRespond(String json);
    void onFailed(Exception e);
}
