package com.franz.easymusicplayer.base

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils
import com.franz.easymusicplayer.bean.DownloadBean
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.service.DownloadService
import com.franz.easymusicplayer.service.MusicService
import com.franz.easymusicplayer.ui.HomePageActivity
import com.franz.easymusicplayer.ui.logIn.LoginActivity
import com.franz.easymusicplayer.utils.FileUtils
import com.franz.easymusicplayer.utils.SPUtil
import com.liulishuo.filedownloader.FileDownloader

class BaseApplication : Application() {
    companion object{
        @JvmStatic
         lateinit var context : Context
        @JvmStatic
         lateinit var userId: String
        @JvmStatic
         lateinit var cookie: String
         @JvmStatic
         val activityList: MutableList<Activity> = ArrayList()
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        FileDownloader.setup(context)

        /**
         * 删除所有下载的音乐
         * 仅作为功能测试*/
//        val list = PlayListDataBase.getDBInstance().downloadDao().getAll()
//        for (i in 0 until list.size){
//            FileUtils.getInstance().deleteDirectory(list[i])
//        }
//        PlayListDataBase.getDBInstance().downloadDao().deleteAll()



        val mCookie: String = SPUtil.getInstance().GetData(context, CacheKeyParam.cookieKey,"") as String
        val mId: String = SPUtil.getInstance().GetData(context, CacheKeyParam.UserId,"") as String

        val intent: Intent = if ((TextUtils.isEmpty(mCookie)) or (TextUtils.isEmpty(mId))){
            userId = ""
            cookie = ""

            Intent(this,LoginActivity::class.java)
        }else{
            cookie = mCookie
            userId = mId

            Intent(this,HomePageActivity::class.java)
        }

        bind()

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        startActivity(intent)
    }

    private fun bind(){
        /**
         * 歌曲播放*/
        MusicService.bindService(context,object : ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }
        })
        /**
         * 歌曲下载*/
        DownloadService.bindService(context,object :ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }

        })
    }
}