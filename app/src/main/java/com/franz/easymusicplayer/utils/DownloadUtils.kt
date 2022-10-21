package com.franz.easymusicplayer.utils

import android.text.TextUtils
import com.franz.easymusicplayer.bean.DownloadBean
import com.franz.easymusicplayer.binder.DownloadBinder
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.room.database.PlayListDataBase

import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import org.json.JSONObject
import java.io.File

object DownloadUtils {
    private lateinit var locationDir: String

    init {
        locationDir = FileUtils.getInstance().mainCatalogue()
    }


    /**
     * 获取音乐下载源*/
     fun startDownload(bean: DownloadBean,callback: FileDownloaderCallback) {
        val url = HttpUtil.getDownloadURL(bean.songId.toString())

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val downloadUrl = JSONObject(json.toString()).getJSONObject("data").getString("url").toString()
                if (!TextUtils.isEmpty(downloadUrl) && downloadUrl != "null"){
                    bean.url = downloadUrl
                    bindDownload(bean,callback)
                }else{
                    DownloadBinder.downloadList.remove(bean)
                    PlayListDataBase.getDBInstance().downloadDao().delete(bean)
                    callback.error("此音乐需要开通才能进行下载!")
                }
            }

            override fun onFailed(e: String?) {
                callback.error(e.toString())
            }
        })
    }

    private fun bindDownload(bean: DownloadBean, callback: FileDownloaderCallback) {
        val name = bean.songId.toString() + bean.songName + ".mp3"
        val path = locationDir + File.separator + name
        bean.path = path
        FileDownloader.getImpl()
            .create(bean.url)
            .setPath(path, false)
            .setListener(object : FileDownloadListener() {
                override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    //已经进入下载队列，正在等待下载
                    callback.pending(task)
                }

                override fun started(task: BaseDownloadTask?) {
                    //结束了pending，并且开始当前任务的Runnable
                    if (task != null) {
                        callback.start(task)
                    }
                }

                override fun connected(task: BaseDownloadTask?, etag: String?, isContinue: Boolean, soFarBytes: Int, totalBytes: Int) {
                    //已经连接上
                }
                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    //soFarBytes:已下载字节数
                    //totalBytes：文件总字节数
                    callback.running(task,task.speed,soFarBytes,totalBytes)
                }

                override fun completed(task: BaseDownloadTask) {
                    callback.completed(task)
                }

                override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    callback.pause(task)
                }
                override fun error(task: BaseDownloadTask, e: Throwable) {
                    callback.failed(task,e.message)
                }
                /**
                 * 存在相同项目*/
                override fun warn(task: BaseDownloadTask) {
                    callback.exist(task)
                }
            }).start()
    }


    interface FileDownloaderCallback {
        fun pending(task: BaseDownloadTask)
        fun start(task: BaseDownloadTask)
        fun running(task: BaseDownloadTask,speed: Int,current:Int,total:Int)
        fun pause(task: BaseDownloadTask)
        fun completed(task: BaseDownloadTask)
        fun failed(task: BaseDownloadTask,message: String?)
        fun error(message: String)
        fun exist(task: BaseDownloadTask)
    }
}