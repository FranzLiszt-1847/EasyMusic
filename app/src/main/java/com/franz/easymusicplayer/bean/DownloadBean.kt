package com.franz.easymusicplayer.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DownloadTable")
data class DownloadBean(
    @PrimaryKey(autoGenerate = false) var primaryKey:String,//主键由歌曲id+歌曲名称组成
    @ColumnInfo(name = "songName")var songName:String,//g歌曲名称
    @ColumnInfo(name = "songId")var songId: Long,//歌曲id
    @ColumnInfo(name = "singer")var singer:String,//歌手名称
    @ColumnInfo(name = "url") var url:String,//下载url
    @ColumnInfo(name = "cover")var cover:String,//歌曲封面
    @ColumnInfo(name = "songSize")var songSize:String,//歌曲大小
    @ColumnInfo(name = "speed") var speed:String,//下载速率
    @ColumnInfo(name = "progress")var progress: Int,//下载进度
    @ColumnInfo(name = "status")var status: Byte,//下载状态
    @ColumnInfo(name = "select")var select: Boolean,//是否被选中
    @ColumnInfo(name = "complete")var complete: Boolean,//是否下载完成
    @ColumnInfo(name = "path")var path: String//本地地址

)
