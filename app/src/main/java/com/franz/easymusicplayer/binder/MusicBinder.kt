package com.franz.easymusicplayer.binder

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.SEEK_CLOSEST
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.PlayProgressBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.callback.MusicInfo
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.HomePageActivity
import com.franz.easymusicplayer.utils.HttpUtil
import com.franz.easymusicplayer.utils.SPUtil
import com.franz.easymusicplayer.utils.ToastUtil
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object MusicBinder : Binder() {
    private val TAG: String = "MusicBinderLog"

    /**
     * 当前播放歌曲在播放列表中的位置*/
    var curSongsCur: Int = 0
    private var curPosition: Long = 0
    private var duration: Long = 0
    var isPlay: Boolean = false
    lateinit var bean: SongBean
    private lateinit var player: MediaPlayer
    private lateinit var listener: ProgressStatus

    private val runnable = object : Runnable {
        override fun run() {
            if (isPlay) {
                curPosition += 1000
                listener.getCurrentProgress(isPlay, player.currentPosition.toLong(), duration)
            }
        }
    }

    private val scheduled = Executors.newSingleThreadScheduledExecutor()


    init {
        player = MediaPlayer()

        val jsonBean =
            SPUtil.getInstance().GetData(BaseApplication.context, CacheKeyParam.recordBean, "")
        if (!TextUtils.isEmpty(jsonBean.toString())) {
            val jsonObject = JSONObject(jsonBean.toString())
            val bean: SongBean = HttpUtil.fromJson(jsonObject.toString(), SongBean::class.java)
            this.bean = bean
            //player.setDataSource(bean.songUrl)
        }

        player.setOnPreparedListener {
            Log.d(TAG, "准备完成")
            duration = player.duration.toLong()
            curPosition = 0
            play()
        }

        player.setOnCompletionListener {
            next(curSongsCur + 1)
        }

        player.setOnErrorListener { mp, what, extra ->
            stop()
            false
        }
    }


    /**
     * 准备播放，传入播放地址*/
    fun prepare(bean: SongBean) {
        Log.d(TAG, "准备播放")
        this.bean = bean
        try {
            player.stop()
            player.reset()
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.setDataSource(bean.songUrl)
            player.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * 开始播放*/
    fun play() {
        Log.d(TAG, "开始播放")
        player.start()
        isPlay = true
        EventBus.getDefault().postSticky(this.bean)
        scheduled.scheduleAtFixedRate(runnable, 0, 1000, TimeUnit.MILLISECONDS)
        SPUtil.getInstance().PutData(BaseApplication.context, CacheKeyParam.recordId, curSongsCur)
        SPUtil.getInstance().PutData(
            BaseApplication.context,
            CacheKeyParam.recordBean,
            Gson().toJson(this.bean).toString()
        )
    }

    /**
     * 暂停播放，保留当前seek*/
    fun pause() {
        Log.d(TAG, "暂停播放")
        player.pause()
        isPlay = false
        EventBus.getDefault().postSticky(PlayProgressBean(false, curPosition, duration))
    }

    /**
     * 停止播放，清空播放状态*/
    fun stop() {
        player.stop()
        isPlay = false
        EventBus.getDefault().postSticky(PlayProgressBean(false, curPosition, duration))
    }

    /**
     * 读取播放列表下一首歌曲,如果已是最后一首，则播放第一首*/
    fun next(pos: Int) {
        val db = PlayListDataBase.getDBInstance().playListDao()
        val songs = db.getAll()
        if (songs.isEmpty()) {
            ToastUtil.setFailToast(HomePageActivity.MA, "当前播放列表暂无数据!")
            return
        }

        val position = if (songs.size - 1 >= pos)
            pos
        else
            0

        curSongsCur = position
        val bean = db.findByMainId(position)

        getSongUrl(bean.songId.toString(), object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                isPlay = true
                bean.songUrl = json
                prepare(bean)
            }

            override fun onFailed(e: String?) {
                ToastUtil.setFailToast(HomePageActivity.MA, "歌曲播放错误!")
            }
        })
    }

    /**
     * 读取播放列表上一首歌曲，如果已是第一首，则播放最后一首*/
    fun previous(pos: Int) {
        val db = PlayListDataBase.getDBInstance().playListDao()
        val songs = db.getAll()
        if (songs.isEmpty()) {
            ToastUtil.setFailToast(HomePageActivity.MA, "当前播放列表暂无数据!")
            return
        }

        val position = if (pos >= 0)
            pos
        else
            songs.size - 1

        curSongsCur = position
        val bean = db.findByMainId(position)

        getSongUrl(bean.songId.toString(), object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                isPlay = true
                bean.songUrl = json
                prepare(bean)
            }

            override fun onFailed(e: String?) {
                ToastUtil.setFailToast(HomePageActivity.MA, "歌曲播放错误!")
            }
        })
    }

    /**
     * 当前是否正在播放*/
    fun isPlaying(): Boolean {
        return isPlay
    }

    /**
     * 歌曲总时长*/
    fun duration(): Long {
        return duration
    }

    /**
     * 当前播放进度*/
    fun position(): Long {
        return curPosition
    }

    /**
     * 改变进度条*/
    fun seek(pos: Long) {
        player.seekTo(pos, SEEK_CLOSEST)
    }


    private fun getSongUrl(id: String, callback: IGenerallyInfo) {
        var url = HttpUtil.getSongURL(id)
        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val array = JSONObject(json.toString()).getJSONArray("data")
                val songURL = JSONObject(array[0].toString()).getString("url")
                callback.onRespond(songURL)
            }

            override fun onFailed(e: String?) {
                callback.onFailed(e.toString())
            }
        })
    }

    interface ProgressStatus {
        fun getCurrentProgress(isStart: Boolean, current: Long, duration: Long)
    }

    fun setProgressListener(listener: ProgressStatus) {
        this.listener = listener
    }
}