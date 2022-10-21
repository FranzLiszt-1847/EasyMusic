package com.franz.easymusicplayer.bean;

import com.google.gson.JsonObject;

public class SongsResultBean {

    private Long id;
    private String name;
    private String coverImgUrl;
    private JsonObject creator;
    private Object lastSong;
    private Boolean subscribed;
    private Integer trackCount;
    private Integer userId;
    private Integer playCount;
    private Integer bookCount;
    private Integer specialType;
    private Object officialTags;
    private Object action;
    private Object actionType;
    private Object recommendText;
    private Object score;
    private String description;
    private Boolean highQuality;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public JsonObject getCreator() {
        return creator;
    }

    public void setCreator(JsonObject creator) {
        this.creator = creator;
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getBookCount() {
        return bookCount;
    }

    public void setBookCount(Integer bookCount) {
        this.bookCount = bookCount;
    }

    public Integer getSpecialType() {
        return specialType;
    }

    public void setSpecialType(Integer specialType) {
        this.specialType = specialType;
    }

    public Object getOfficialTags() {
        return officialTags;
    }

    public void setOfficialTags(Object officialTags) {
        this.officialTags = officialTags;
    }

    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }

    public Object getActionType() {
        return actionType;
    }

    public void setActionType(Object actionType) {
        this.actionType = actionType;
    }

    public Object getRecommendText() {
        return recommendText;
    }

    public void setRecommendText(Object recommendText) {
        this.recommendText = recommendText;
    }

    public Object getScore() {
        return score;
    }

    public void setScore(Object score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHighQuality() {
        return highQuality;
    }

    public void setHighQuality(Boolean highQuality) {
        this.highQuality = highQuality;
    }

    public Object getLastSong() {
        return lastSong;
    }

    public void setLastSong(Object lastSong) {
        this.lastSong = lastSong;
    }
}
