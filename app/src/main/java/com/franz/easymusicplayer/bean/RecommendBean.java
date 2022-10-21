package com.franz.easymusicplayer.bean;

import com.google.gson.JsonArray;

import java.util.List;

public class RecommendBean {

    private Integer id;
    private Integer type;
    private String name;
    private String copywriter;
    private String picUrl;
    private Boolean canDislike;
    private Object trackNumberUpdateTime;
    private Integer duration;
    private Integer playCount;
    private Integer trackCount;
    private Boolean highQuality;
    private Object song;
    private Boolean subed;
    private JsonArray artists;
    private String artistName;
    private Integer artistId;
    private String alg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCopywriter() {
        return copywriter;
    }

    public void setCopywriter(String copywriter) {
        this.copywriter = copywriter;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Boolean getCanDislike() {
        return canDislike;
    }

    public void setCanDislike(Boolean canDislike) {
        this.canDislike = canDislike;
    }

    public Object getTrackNumberUpdateTime() {
        return trackNumberUpdateTime;
    }

    public void setTrackNumberUpdateTime(Object trackNumberUpdateTime) {
        this.trackNumberUpdateTime = trackNumberUpdateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Boolean getSubed() {
        return subed;
    }

    public void setSubed(Boolean subed) {
        this.subed = subed;
    }

    public JsonArray getArtists() {
        return artists;
    }

    public void setArtists(JsonArray artists) {
        this.artists = artists;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Integer getArtistId() {
        return artistId;
    }

    public void setArtistId(Integer artistId) {
        this.artistId = artistId;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }

    public Boolean getHighQuality() {
        return highQuality;
    }

    public void setHighQuality(Boolean highQuality) {
        this.highQuality = highQuality;
    }

    public Object getSong() {
        return song;
    }

    public void setSong(Object song) {
        this.song = song;
    }
}
