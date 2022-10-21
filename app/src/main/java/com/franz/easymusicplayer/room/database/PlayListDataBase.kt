package com.franz.easymusicplayer.room.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.DownloadBean
import com.franz.easymusicplayer.bean.HistorySearchBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.room.dao.DownloadDao
import com.franz.easymusicplayer.room.dao.HistorySearchDao
import com.franz.easymusicplayer.room.dao.PlayListDao

@Database(entities = [SongBean::class,HistorySearchBean::class,DownloadBean::class], version = 5)
abstract class PlayListDataBase: RoomDatabase() {
    abstract fun playListDao(): PlayListDao
    abstract fun historyDao(): HistorySearchDao
    abstract fun downloadDao(): DownloadDao

    companion object {

        @Volatile
        private var instance: PlayListDataBase? = null

        fun getDBInstance(): PlayListDataBase {

            if (instance == null) {

                synchronized(PlayListDataBase::class) {

                    if (instance == null) {

                        instance = Room.databaseBuilder(
                            BaseApplication.context,
                            PlayListDataBase::class.java,
                            "Music.db"
                        )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()//如果更新version之后，不提供自定义Migration，又不想引发crash，引入它，系统自动删除数据库重建
                            .build()
                    }
                }
            }
            return instance!!
        }

    }
}