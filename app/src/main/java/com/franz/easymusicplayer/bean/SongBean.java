package com.franz.easymusicplayer.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "PlayList")
public class SongBean {
    @PrimaryKey(autoGenerate = false)
    private int mainId;
    @ColumnInfo(name = "SongName")
    private String SongName ;
    @ColumnInfo(name = "SongId")
    private Long SongId;
    @ColumnInfo(name = "SingerName")
    private String SingerName;
    @ColumnInfo(name = "SingerId")
    private Long SingerId;
    @ColumnInfo(name = "AlbumCover")
    private String AlbumCover;
    @ColumnInfo(name = "SongUrl")
    private String SongUrl;
    @Ignore
    private boolean select;

    public SongBean(int mainId,String SongName,Long SongId,String SingerName,Long SingerId,String AlbumCover){
        this.SongName = SongName;
        this.SongId = SongId;
        this.SingerName = SingerName;
        this.SingerId = SingerId;
        this.AlbumCover = AlbumCover;
        this.SongUrl = null;
        this.mainId = mainId;
        this.select = false;
    }
    public String getSongName() {
        return SongName;
    }

    public void setSongName(String songName) {
        SongName = songName;
    }

    public Long getSongId() {
        return SongId;
    }

    public void setSongId(Long songId) {
        SongId = songId;
    }

    public String getSingerName() {
        return SingerName;
    }

    public void setSingerName(String singerName) {
        SingerName = singerName;
    }

    public Long getSingerId() {
        return SingerId;
    }

    public void setSingerId(Long singerId) {
        SingerId = singerId;
    }

    public String getAlbumCover() {
        return AlbumCover;
    }

    public void setAlbumCover(String albumCover) {
        AlbumCover = albumCover;
    }

    public String getSongUrl() {
        return SongUrl;
    }

    public void setSongUrl(String songUrl) {
        SongUrl = songUrl;
    }

    public int getMainId() {
        return mainId;
    }

    public void setMainId(int mainId) {
        this.mainId = mainId;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
