package com.franz.easymusicplayer.room.dao

import androidx.room.*
import com.franz.easymusicplayer.bean.HistorySearchBean
import com.franz.easymusicplayer.bean.SongBean


@Dao
interface HistorySearchDao {
    /**
     * 获取表中所有数据*/
    @Query("SELECT * FROM HistorySearch")
    fun getAll():MutableList<HistorySearchBean>

    /**
     * 删除表中的所有数据
     * 1:success
     * 0:failed*/
    @Query("DELETE FROM HistorySearch")
    fun deleteAll()

    /**
     * 插入至表中
     * 如果已经存在相同的数据，则替换*/
    @Insert
    fun insert(key: HistorySearchBean)

    @Delete
    fun delete(key: HistorySearchBean)

    /**
     * 通过歌曲id获取表中对应列*/
    @Query("SELECT * FROM HistorySearch WHERE `key` LIKE :key LIMIT 1")
    fun findByKey(key:String): HistorySearchBean
}