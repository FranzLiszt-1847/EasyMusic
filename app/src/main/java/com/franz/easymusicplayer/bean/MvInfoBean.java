package com.franz.easymusicplayer.bean;

import com.google.gson.JsonArray;

import java.util.List;

public class MvInfoBean {
    private Integer id;
    private String name;
    private Integer artistId;
    private String artistName;
    private String briefDesc;
    private Object desc;
    private String cover;
    private String coverId_str;
    private Long coverId;
    private Integer playCount;
    private Integer subCount;
    private Integer shareCount;
    private Integer commentCount;
    private Integer duration;
    private Integer nType;
    private String publishTime;
    private Object price;
    private JsonArray brs;
    private JsonArray artists;
    private String commentThreadId;
    private JsonArray videoGroup;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getArtistId() {
        return artistId;
    }

    public void setArtistId(Integer artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getBriefDesc() {
        return briefDesc;
    }

    public void setBriefDesc(String briefDesc) {
        this.briefDesc = briefDesc;
    }

    public Object getDesc() {
        return desc;
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCoverId_str() {
        return coverId_str;
    }

    public void setCoverId_str(String coverId_str) {
        this.coverId_str = coverId_str;
    }

    public Long getCoverId() {
        return coverId;
    }

    public void setCoverId(Long coverId) {
        this.coverId = coverId;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getSubCount() {
        return subCount;
    }

    public void setSubCount(Integer subCount) {
        this.subCount = subCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getNType() {
        return nType;
    }

    public void setNType(Integer nType) {
        this.nType = nType;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public Object getPrice() {
        return price;
    }

    public void setPrice(Object price) {
        this.price = price;
    }

    public JsonArray getBrs() {
        return brs;
    }

    public void setBrs(JsonArray brs) {
        this.brs = brs;
    }

    public JsonArray getArtists() {
        return artists;
    }

    public void setArtists(JsonArray artists) {
        this.artists = artists;
    }

    public String getCommentThreadId() {
        return commentThreadId;
    }

    public void setCommentThreadId(String commentThreadId) {
        this.commentThreadId = commentThreadId;
    }

    public JsonArray getVideoGroup() {
        return videoGroup;
    }

    public void setVideoGroup(JsonArray videoGroup) {
        this.videoGroup = videoGroup;
    }
}
