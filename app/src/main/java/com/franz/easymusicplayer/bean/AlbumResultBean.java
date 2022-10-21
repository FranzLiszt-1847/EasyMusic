package com.franz.easymusicplayer.bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class AlbumResultBean {

    private String name;
    private Integer id;
    private String idStr;
    private String type;
    private Integer size;
    private Long picId;
    private String blurPicUrl;
    private Integer companyId;
    private Long pic;
    private String picUrl;
    private Long publishTime;
    private String description;
    private String tags;
    private String company;
    private String briefDesc;
    private JsonObject artist;
    private JsonArray songs;
    private JsonArray alias;
    private Integer status;
    private Integer copyrightId;
    private String commentThreadId;
    private JsonArray artists;
    private String picId_str;
    private Boolean isSub;
    private String subType;
    private Object transName;
    private Integer mark;
    private Object lastSong;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getPicId() {
        return picId;
    }

    public void setPicId(Long picId) {
        this.picId = picId;
    }

    public String getBlurPicUrl() {
        return blurPicUrl;
    }

    public void setBlurPicUrl(String blurPicUrl) {
        this.blurPicUrl = blurPicUrl;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Long getPic() {
        return pic;
    }

    public void setPic(Long pic) {
        this.pic = pic;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getBriefDesc() {
        return briefDesc;
    }

    public void setBriefDesc(String briefDesc) {
        this.briefDesc = briefDesc;
    }

    public JsonObject getArtist() {
        return artist;
    }

    public void setArtist(JsonObject artist) {
        this.artist = artist;
    }

    public JsonArray getSongs() {
        return songs;
    }

    public void setSongs(JsonArray songs) {
        this.songs = songs;
    }

    public JsonArray getAlias() {
        return alias;
    }

    public void setAlias(JsonArray alias) {
        this.alias = alias;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCopyrightId() {
        return copyrightId;
    }

    public void setCopyrightId(Integer copyrightId) {
        this.copyrightId = copyrightId;
    }

    public String getCommentThreadId() {
        return commentThreadId;
    }

    public void setCommentThreadId(String commentThreadId) {
        this.commentThreadId = commentThreadId;
    }

    public JsonArray getArtists() {
        return artists;
    }

    public void setArtists(JsonArray artists) {
        this.artists = artists;
    }

    public String getPicId_str() {
        return picId_str;
    }

    public void setPicId_str(String picId_str) {
        this.picId_str = picId_str;
    }

    public Boolean getIsSub() {
        return isSub;
    }

    public void setIsSub(Boolean isSub) {
        this.isSub = isSub;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Object getTransName() {
        return transName;
    }

    public void setTransName(Object transName) {
        this.transName = transName;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    public Object getLastSong() {
        return lastSong;
    }

    public void setLastSong(Object lastSong) {
        this.lastSong = lastSong;
    }
}
