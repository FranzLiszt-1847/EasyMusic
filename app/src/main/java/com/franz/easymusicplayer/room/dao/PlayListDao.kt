package com.franz.easymusicplayer.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franz.easymusicplayer.bean.SongBean

@Dao
interface PlayListDao {

    /**
     * 获取表中所有数据*/
    @Query("SELECT * FROM PlayList")
    fun getAll():MutableList<SongBean>

    /**
     * 删除表中的所有数据
     * 1:success
     * 0:failed*/
    @Query("DELETE FROM PlayList")
    fun deleteAll(): Int

    /**
     * 通过主键id获取表中对应列*/
    @Query("SELECT * FROM PlayList WHERE mainId LIKE :mainId order by mainId desc  LIMIT 1")
    fun findByMainId(mainId:Int): SongBean

    /**
     * 通过歌曲id获取表中对应列*/
    @Query("SELECT * FROM PlayList WHERE mainId LIKE :SongId order by mainId desc  LIMIT 1")
    fun findBySongId(SongId:Int): SongBean

    /**
     * 插入至表中
     *
     * 如果已经存在相同的数据，则替换*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: SongBean)

    /**
     * 将所有数据插入至表中*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(beanList: MutableList<SongBean>)

}