package com.franz.easymusicplayer.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.franz.easymusicplayer.binder.MusicBinder

class MusicService : Service() {
    private val TAG: String = "MusicService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"onCreate")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG,"onBind")
        return MusicBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object{
        fun bindService(context: Context,connection: ServiceConnection){
            val intent = Intent(context,MusicService::class.java)
            context.bindService(intent,connection,Service.BIND_AUTO_CREATE)
        }
    }
}