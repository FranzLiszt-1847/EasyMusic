package com.franz.easymusicplayer.binder


import android.os.Binder
import android.text.TextUtils
import android.util.Log
import com.franz.easymusicplayer.bean.DownloadBean
import com.franz.easymusicplayer.bean.DownloadingBean
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.HomePageActivity
import com.franz.easymusicplayer.ui.download.DownloadActivity
import com.franz.easymusicplayer.utils.DownloadUtils
import com.franz.easymusicplayer.utils.FileUtils
import com.franz.easymusicplayer.utils.HttpUtil

import com.franz.easymusicplayer.utils.ToastUtil
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import kotlin.collections.ArrayList


object DownloadBinder : Binder() {
    val downloadList: MutableList<DownloadBean>

    private var percentage: Int = 0
    private lateinit var locationDir: String

    private var current: Int = 0 //当前下载序号

    init {
        downloadList = ArrayList()
        locationDir = FileUtils.getInstance().mainCatalogue()
    }

    /**
     * 获取音乐下载源*/
    private fun startDownload(bean: DownloadBean,callback: IGenerallyInfo) {
        val url = HttpUtil.getDownloadURL(bean.songId.toString())

        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                val downloadUrl = JSONObject(json.toString()).getJSONObject("data").getString("url").toString()
                if (!TextUtils.isEmpty(downloadUrl) && downloadUrl != "null") {
                    callback.onRespond(downloadUrl)
                } else {
                    callback.onFailed("此音乐需要开通才能进行下载!")
                }
            }

            override fun onFailed(e: String?) {
                callback.onFailed(e.toString())
            }
        })
    }

    fun download() {
        if (downloadList.size == 0) return

        if (current >= downloadList.size) {
            ToastUtil.setSuccessToast(HomePageActivity.MA, "下载完成!")
            current = 0
            downloadList.clear()
            return
        }

        val name = downloadList[current].songId.toString() + downloadList[current].songName + ".mp3"
        val path = locationDir + File.separator + name
        downloadList[current].path = path

        startDownload(downloadList[current],object :IGenerallyInfo{
            override fun onRespond(json: String?) {
                downloadList[current].url = json.toString()
                bindDownload(path, downloadList[current].url)
            }

            override fun onFailed(e: String?) {
                delete(downloadList[current])
                EventBus.getDefault().postSticky(DownloadingBean(111, "", percentage, e.toString(), null))
                download()
            }
        })
    }

    private fun bindDownload(path:String,url:String){
        FileDownloader.getImpl()
            .create(url)
            .setPath(path, false)
            .setListener(object : FileDownloadListener() {
                override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    //已经进入下载队列，正在等待下载
                    EventBus.getDefault().postSticky(DownloadingBean(task.status, "", 0, "", null))
                }

                override fun started(task: BaseDownloadTask?) {
                    //结束了pending，并且开始当前任务的Runnable
                    if (task != null) {
                        EventBus.getDefault().postSticky(DownloadingBean(task.status, "", 0, "", null))
                    }
                }

                override fun connected(task: BaseDownloadTask?, etag: String?, isContinue: Boolean, soFarBytes: Int, totalBytes: Int) {
                    //已经连接上
                }

                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    //soFarBytes:已下载字节数
                    //totalBytes：文件总字节数
                    Log.d("downloadTAG", "running:${task.speed}")
                    percentage = ((soFarBytes * 1.0 / totalBytes) * 100).toInt()
                    EventBus.getDefault().postSticky(DownloadingBean(task.status, "${remainDigit(task.speed / 8.0)}KB/s", percentage, "", downloadList[current]))
                }

                override fun completed(task: BaseDownloadTask) {
                    //status = -3
//                /**
//                 * 除2个1024的到大小MB
//                 * 记得最后保留一位小数*/
                    val primary = "${downloadList[current].songId}${downloadList[current].songName}"
//
//                /**
//                 * 下载完成之后，更新数据库字段*/
                    PlayListDataBase.getDBInstance().downloadDao().updateComplete(primary, true)
                    PlayListDataBase.getDBInstance().downloadDao().updatePath(primary, task.path)
                    PlayListDataBase.getDBInstance().downloadDao().updateUrl(primary, task.url)
                    val size = remainDigit(task.smallFileTotalBytes * 1.0 / 1024 / 1024)
                    PlayListDataBase.getDBInstance().downloadDao().updateSize(primary, "${size}MB")
                    EventBus.getDefault().postSticky(DownloadingBean(task.status, "", percentage, "", downloadList[current]))

                    current++
                    download()
                }

                override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                    //callback.pause(task)
                    Log.d("downloadTAG", "pause:${task.status}")
                    EventBus.getDefault().postSticky(DownloadingBean(task.status, "", percentage, "", downloadList[current]))
                }

                override fun error(task: BaseDownloadTask, e: Throwable) {
                    // callback.failed(task,e.message)
                    //error = -1
                    Log.d("downloadTAG", "failed:${task.status}")
                    Log.d("downloadTAG", "failed:${e.message}")
                    EventBus.getDefault().postSticky(DownloadingBean(task.status, "", percentage, e.message!!, downloadList[current]))

                    download()
                }

                /**
                 * 存在相同项目*/
                override fun warn(task: BaseDownloadTask) {
                    // callback.exist(task)
                    /**
                     * 不会进入此处
                     * 因为外面已经判断过重复项*/
                    Log.d("downloadTAG", "exist:${task.status}")
                    EventBus.getDefault().postSticky(DownloadingBean(111, "", percentage, "", downloadList[current]))

                    //current++
                    download()
                }
            }).start()
    }

    private fun delete(bean: DownloadBean){
        if (downloadList.size == 0){
            current = 0
            return
        }
        /**
         * 倒叙删除无音源子项*/
        for (i in downloadList.size-1 downTo 0){
            if (downloadList[i] == bean){
                PlayListDataBase.getDBInstance().downloadDao().delete(downloadList[i])
                downloadList.removeAt(i)
            }
        }

        if (downloadList.size == 0){
            current = 0
            return
        }

        /**
         * 正序找到第一个未下载的子项*/
        for (i in downloadList.indices){
            if (!downloadList[i].complete){
                current = i
                return
            }
        }
    }

    /**
     * 保留一位小数*/
    private fun remainDigit(num: Double): String {
        val df = DecimalFormat("#.0")
        return df.format(num)
    }
}

