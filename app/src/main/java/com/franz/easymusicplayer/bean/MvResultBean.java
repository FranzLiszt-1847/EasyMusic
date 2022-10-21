package com.franz.easymusicplayer.bean;

import com.google.gson.JsonArray;

import java.util.List;

public class MvResultBean {
    private String id;
    private String cover;
    private String name;
    private Long playCount;
    private Object briefDesc;
    private Object desc;
    private String artistName;
    private Integer artistId;
    private Long duration;
    private Integer mark;
    private JsonArray artists;
    private Object transNames;
    private Object alias;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
    }

    public Object getBriefDesc() {
        return briefDesc;
    }

    public void setBriefDesc(Object briefDesc) {
        this.briefDesc = briefDesc;
    }

    public Object getDesc() {
        return desc;
    }

    public void setDesc(Object desc) {
        this.desc = desc;
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    public JsonArray getArtists() {
        return artists;
    }

    public void setArtists(JsonArray artists) {
        this.artists = artists;
    }

    public Object getTransNames() {
        return transNames;
    }

    public void setTransNames(Object transNames) {
        this.transNames = transNames;
    }

    public Object getAlias() {
        return alias;
    }

    public void setAlias(Object alias) {
        this.alias = alias;
    }
}
