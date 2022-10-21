package com.franz.easymusicplayer.bean;

public class DownloadCompleteBean {
    private boolean isComplete;

    public DownloadCompleteBean(boolean isComplete){
        this.isComplete = isComplete;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
