package com.franz.easymusicplayer.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.franz.easymusicplayer.bean.DownloadBean


@Dao
interface DownloadDao {
    /**
     * 获取表中所有数据*/
    @Query("SELECT * FROM DownloadTable")
    fun getAll():MutableList<DownloadBean>

    /**
     * 删除表中的所有数据
     * 1:success
     * 0:failed*/
    @Query("DELETE FROM DownloadTable")
    fun deleteAll()

    /**
     * 插入至表中
     * 如果已经存在相同的数据，则替换*/
    @Insert
    fun insert(key: DownloadBean)

    @Delete
    fun delete(key: DownloadBean)

    /**
     * 通过歌曲id获取表中对应列*/
    @Query("SELECT * FROM DownloadTable WHERE `primaryKey` LIKE :primaryKey LIMIT 1")
    fun findByKey(primaryKey:String): DownloadBean

    /**
     * 更新下载本地地址*/
    @Query("UPDATE DownloadTable SET path = :path WHERE primaryKey =:primaryKey")
    fun updatePath(primaryKey:String,path:String)

    /**
     * 更新是否下载完成*/
    @Query("UPDATE DownloadTable SET complete = :complete WHERE primaryKey =:primaryKey")
    fun updateComplete(primaryKey:String,complete:Boolean)

    /**
     * 更新url*/
    @Query("UPDATE DownloadTable SET url = :url WHERE primaryKey =:primaryKey")
    fun updateUrl(primaryKey:String,url:String)
    /**
     * 更新文件大小*/
    @Query("UPDATE DownloadTable SET songSize = :songSize WHERE primaryKey =:primaryKey")
    fun updateSize(primaryKey:String,songSize:String)
}