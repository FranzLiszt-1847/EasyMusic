package com.franz.easymusicplayer.ui.songList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.franz.easymusicplayer.R
import com.franz.easymusicplayer.adapter.SongsAdapter
import com.franz.easymusicplayer.base.BaseApplication
import com.franz.easymusicplayer.bean.DetailInfoBean
import com.franz.easymusicplayer.bean.DownloadBean
import com.franz.easymusicplayer.bean.SongBean
import com.franz.easymusicplayer.binder.DownloadBinder
import com.franz.easymusicplayer.binder.MusicBinder
import com.franz.easymusicplayer.callback.IDetailInfo
import com.franz.easymusicplayer.callback.IGenerallyInfo
import com.franz.easymusicplayer.databinding.ActivityAlbumListBinding
import com.franz.easymusicplayer.databinding.PopDownloadTipsBinding
import com.franz.easymusicplayer.databinding.PopSongDetailBinding
import com.franz.easymusicplayer.databinding.TitleBarWhiteBinding
import com.franz.easymusicplayer.param.CacheKeyParam
import com.franz.easymusicplayer.room.database.PlayListDataBase
import com.franz.easymusicplayer.ui.player.MLogActivity
import com.franz.easymusicplayer.utils.*
import com.franz.easymusicplayer.widgets.GlideRoundTransform
import jp.wasabeef.glide.transformations.BlurTransformation
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AlbumListActivity : AppCompatActivity(),View.OnClickListener{
    private lateinit var binding: ActivityAlbumListBinding
    private lateinit var titleBarBinding: TitleBarWhiteBinding
    private val TAG:String = "AlbumListActivityLog"
    private lateinit var albumId: String
    private lateinit var albumInfo: String

    private val songBeanList: MutableList<SongBean> = ArrayList()
    private lateinit var songsAdapter: SongsAdapter

    private lateinit var detailBinding: PopSongDetailBinding
    private lateinit var detailView: View
    private lateinit var detailPop: PopupWindow

    private lateinit var downloadBinding: PopDownloadTipsBinding
    private lateinit var downloadView: View
    private lateinit var downloadPop: PopupWindow

    private var displayDownload:Boolean = false

    private val ALBUM: Int = 1
    private val SONGURL: Int = 2
    private val SONGDETAIL: Int = 3

    private val handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                ALBUM -> updateSongs(msg.obj as JSONArray)
                SONGURL -> updatePlayInfo(msg.obj as SongBean)
                SONGDETAIL -> updateSongDetail(msg.obj as DetailInfoBean)
                else -> {}
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarHide(window)
        binding = ActivityAlbumListBinding.inflate(layoutInflater)
        titleBarBinding = binding.titleBarWhite
        setContentView(binding.root)

        getAlbumId()
        initListener()
        initRecycler()
        initPopWindow()
        getAlbumContent()

    }

    /**
     * 获取歌单id*/
    private fun getAlbumId() {
        binding.loading.show()
        val intent = intent
        albumId = intent.getStringExtra(CacheKeyParam.albumSongs).toString()
        Log.d(TAG, "id=$albumId")
    }

    private fun initListener() {
        titleBarBinding.exit.setOnClickListener(this)
        binding.playAllText.setOnClickListener(this)
        binding.songSelect.setOnClickListener(this)
        binding.songDownload.setOnClickListener(this)
        titleBarBinding.majorTitle.text = "专辑"
        titleBarBinding.majorTitle.setTextColor(getColor(R.color.white))

    }

    private fun initRecycler() {
        binding.albumListRecycler.layoutManager = LinearLayoutManager(this)
        songsAdapter = SongsAdapter(songBeanList)
        binding.albumListRecycler.adapter = songsAdapter

        songsAdapter.setItemClickListener(object : SongsAdapter.OnSongClickListener {
            override fun onClickListener(pos: Int, bean: SongBean) {
                /**
                 * 修改歌曲在列表中位置*/
                if (!songsAdapter.displaySelect){
                    MusicBinder.curSongsCur = pos
                    getSongUrl(bean)
                    ToastUtil.setSuccessToast(this@AlbumListActivity, "歌曲正在加载中...")
                }else{
                    /*处理下载*/
                    if (bean.isSelect) {
                        songBeanList[pos].isSelect = false
                        songsAdapter.notifyItemChanged(pos,1)
                    }
                    else {
                        songBeanList[pos].isSelect = true
                        songsAdapter.notifyItemChanged(pos,1)
                    }
                }
            }
        })

        songsAdapter.setMvItemClickListener(object : SongsAdapter.OnMvClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                val intent = Intent(this@AlbumListActivity, MLogActivity::class.java)
                intent.putExtra(CacheKeyParam.mlogId, "${bean.songId}")
                startActivity(intent)
            }
        })

        /**
         * 根据歌曲id获取歌曲详情*/
        songsAdapter.setMoreInfoItemClickListener(object : SongsAdapter.OnMoreInfoClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                getSongDetail(bean.songId.toString())
            }
        })

        /**
         * 歌曲下载
         * 歌曲id+歌曲名称组成主键，首先判断数据库是否存在相同实例，存在则不下载，进行提示*/
        songsAdapter.setDownloadItemClickListener(object :SongsAdapter.OnDownloadClickListener{
            override fun onClickListener(pos: Int, bean: SongBean) {
                val flag = isExistIdentity(bean)
                if (flag){
                    ToastUtil.setFailToast(this@AlbumListActivity,"已存在相同下载项，请勿重复添加!")
                }else{
                    DownloadBinder.download()
                    ToastUtil.setSuccessToast(this@AlbumListActivity,"已添加到下载队列中!")
                }
            }
        })
    }

    /**
     * 初始化歌曲详细pop*/
    private fun initPopWindow(){
        detailView = LayoutInflater.from(this).inflate(R.layout.pop_song_detail,null,false)
        detailBinding = PopSongDetailBinding.bind(detailView)

        detailPop = PopupWindow(detailView, SystemParamUtil.width-200, ViewGroup.LayoutParams.WRAP_CONTENT)
        //detailPop.contentView = detailView

        detailPop.isFocusable = true
        detailPop.isTouchable = true
        detailPop.isOutsideTouchable = true
        detailPop.animationStyle = R.style.PopupWindowAnim

        /**
         * 下载提示*/
        downloadView = LayoutInflater.from(this).inflate(R.layout.pop_download_tips,null,false)
        downloadBinding = PopDownloadTipsBinding.bind(downloadView)

        downloadPop = PopupWindow(downloadView,SystemParamUtil.width-200,ViewGroup.LayoutParams.WRAP_CONTENT)

        downloadPop.isFocusable = true
        downloadPop.isTouchable = true
        downloadPop.isOutsideTouchable = true
        downloadPop.animationStyle = R.style.PopupWindowAnim

        downloadBinding.cancel.setOnClickListener{
            downloadPop.dismiss()
        }

        downloadBinding.confirm.setOnClickListener{
            var count = 0
            for (i in songBeanList.indices){
                if (songBeanList[i].isSelect){
                    var flag = isExistIdentity(songBeanList[i])
                    if (flag)count++
                }
            }
            if (count> 0)ToastUtil.setFailToast(this@AlbumListActivity,"部分音乐可能已经存在下载队列之后，请勿重复添加!")
            DownloadBinder.download()
            downloadPop.dismiss()
        }
    }

    /**
     * 获取歌曲url并播放*/
    private fun getSongUrl(bean: SongBean) {
        val url = HttpUtil.getSongURL(bean.songId.toString())
        Log.d(TAG, "歌曲url=$url")
        HttpUtil.getGenerallyInfo(url, object : IGenerallyInfo {
            override fun onRespond(json: String?) {
                Log.d(TAG, "歌曲数据=${json.toString()}")
                val array = JSONObject(json.toString()).getJSONArray("data")
                val songURL = JSONObject(array[0].toString()).getString("url")
                bean.songUrl = songURL
                val mes = Message()
                mes.what = SONGURL
                mes.obj = bean
                handler.sendMessage(mes)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@AlbumListActivity, e.toString())
            }
        })
    }

    private fun getAlbumContent(){
        val url = HttpUtil.getAlbumSongsURL(albumId)
        HttpUtil.getGenerallyInfo(url,object : IGenerallyInfo{
            override fun onRespond(json: String?) {
                Log.d(TAG, json.toString())
                val songs = JSONObject(json.toString()).getJSONArray("songs")
                albumInfo = JSONObject(json.toString()).getString("album").toString()
                val message = Message()
                message.what = ALBUM
                message.obj = songs
                handler.sendMessage(message)
            }

            override fun onFailed(e: String?) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@AlbumListActivity, e.toString())
            }
        })
    }

    /**
     * 根据歌曲id获取歌曲详情*/
    private fun getSongDetail(songId: String){
        val detailInfo = DetailInfo()
        detailInfo.getSongDetail(songId,object : IDetailInfo{
            override fun onRespond(bean: DetailInfoBean) {
                val message = Message()
                message.what = SONGDETAIL
                message.obj = bean
                handler.sendMessage(message)
            }

            override fun onFailed(e: String) {
                Log.d(TAG, e.toString())
                ToastUtil.setFailToast(this@AlbumListActivity, e.toString())
            }
        })
    }

    /**
     * 解析歌曲json数据*/
    private fun updateSongs(array: JSONArray) {
        if (array.length() == 0) {
            ToastUtil.setFailToast(this@AlbumListActivity, "专辑解析错误!")
            binding.loading.hide()
            return
        }
        for (obj in 0 until array.length()) {
            val jsonObject = JSONObject(array[obj].toString())
            val songName = jsonObject.getString("name")//歌曲名称
            val songId = jsonObject.getLong("id")//歌曲id
            val singer = jsonObject.getJSONArray("ar")
            val singerName = JSONObject(singer[0].toString()).getString("name")//歌手名称
            val singerId = JSONObject(singer[0].toString()).getLong("id")//歌手id
            val albumCover = jsonObject.getJSONObject("al").getString("picUrl")
            songBeanList.add(SongBean(obj, songName, songId, singerName, singerId, albumCover))
            Log.d(TAG, "songName=$songName,singerName=$singerName")
        }

        if (TextUtils.isEmpty(albumInfo))return

        val mObject = JSONObject(albumInfo.toString())
        binding.playAllImg.visibility = View.VISIBLE
        binding.playAllText.visibility = View.VISIBLE
        binding.songSelect.visibility = View.VISIBLE

        binding.albumName.text = mObject.getString("name").toString()
        val author = JSONObject(array[0].toString()).getJSONArray("ar")
        binding.albumAuthor.text = "歌手: ${JSONObject(author[0].toString()).getString("name").toString()}"
        binding.albumNumber.text = "共计${mObject.getString("size").toString()}首"
        val time = mObject.getLong("publishTime").toLong()
        binding.albumTime.text = "发行时间: ${millToDate(time)}"

        val img = mObject.getString("picUrl").toString()
        /**
         * 当你启动此activity,然后立马退出，此时网络数据并未返回
         * glide会继续获取数据，当获取数据准备附加到img时，发现activity已经被destroyed
         *  异常如下：You cannot start a load for a destroyed activity*/

        if (!this.isFinishing) {
            Glide.with(binding.albumSmallCover)
                .asDrawable()
                .load(img)
                .transform(GlideRoundTransform(BaseApplication.context, 10))
                .placeholder(R.drawable.icon_default_songs)
                .dontAnimate()
                .into(binding.albumSmallCover)

            Glide.with(binding.albumBigCover)
                .asDrawable()
                .load(img)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(50)))
                .placeholder(R.drawable.icon_default_songs)
                .dontAnimate()
                .into(binding.albumBigCover)
        }
        songsAdapter.notifyDataSetChanged()
        binding.loading.hide()
    }

    /**
     * 更新pop歌曲信息*/
    private fun updateSongDetail(bean: DetailInfoBean){
        detailBinding.songDetailSong.text = bean.songName
        detailBinding.songDetailSinger.text = bean.singer
        detailBinding.songDetailAlbum.text = bean.album
        Glide.with(detailBinding.songDetailCover)
            .asDrawable()
            .load(bean.cover)
            .transform(GlideRoundTransform(BaseApplication.context,10))
            .placeholder(R.drawable.icon_default_songs)
            .dontAnimate()
            .into(detailBinding.songDetailCover)
        detailBinding.songDetailTime.text = bean.time
        detailBinding.songDetailType.text = bean.type
        detailShow()
    }

    private fun millToDate(time: Long): String{
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(date)
    }

    private fun detailShow(){
        detailPop.showAtLocation(detailView, Gravity.CENTER,0,0)
    }

    private fun downloadShow(){
        downloadPop.showAtLocation(downloadView,Gravity.CENTER,0,0)
    }

    /**
     * 播放歌曲，并更新歌曲信息*/
    private fun updatePlayInfo(bean: SongBean) {
        MusicBinder.prepare(bean)
        ToastUtil.setSuccessToast(this@AlbumListActivity, "歌曲信息获取完成")
    }

    /**
     * 点击播放按钮，将当前歌单内所有歌曲添加到播放列表*/
    private fun addToSongList() {
        /**
         * 获取数据库实例*/
        val db = PlayListDataBase.getDBInstance().playListDao()
        val allSongs = db.getAll()
        if (allSongs != null || allSongs.size > 0) {
            db.deleteAll()
        }

        db.insertAll(songBeanList)

        ToastUtil.setSuccessToast(this@AlbumListActivity, "已添加${songBeanList.size}首歌至播放列表")
    }

    private fun isExistIdentity(bean: SongBean): Boolean{
        val dao = PlayListDataBase.getDBInstance().downloadDao()
        val key = "${bean.songId}${bean.songName}"
        val isExist: DownloadBean = dao.findByKey(key)
        /**
         * 如果不为空，则代表存在，则不再进行重复下载*/
        if (isExist != null) return true

        val downloadBean = DownloadBean(key,bean.songName,bean.songId,bean.singerName,"",bean.albumCover,"","",0,0,false,false,"")

        /*update room*/
        dao.insert(downloadBean)

        /*update binder*/
        DownloadBinder.downloadList.add(downloadBean)
        return false
    }

    override fun onClick(v: View?) = when (v?.id) {
        R.id.exit -> finish()
        R.id.playAllText -> addToSongList()
        /**
         * 点击多选按钮，显示下载按钮,recyclerview复选框*/
        R.id.songSelect -> {
            if (displayDownload){
                /*隐藏*/
                hide()
            }else{
                /*显示*/
                show()
            }
        }
        R.id.songDownload->{
            val count = isSelect()
            if (count == 0){
                ToastUtil.setFailToast(this@AlbumListActivity,"请先勾选需要下载的音乐!")
            }else{
                val format = resources.getString(R.string.download_content)
                val content = String.format(format,count).toString()
                downloadBinding.tipsContent.text = content
                downloadShow()
            }
        }
        else -> {}
    }

    private fun isSelect():Int{
        var count = 0
        if (songBeanList.size == 0) return count
        for (i in songBeanList.indices){
            if (songBeanList[i].isSelect)count++
        }
        return count
    }

    private fun show(){
        binding.songDownload.visibility = View.VISIBLE
        displayDownload = true
        songsAdapter.displaySelect = true
        songsAdapter.notifyDataSetChanged()
    }

    private fun hide(){
        binding.songDownload.visibility = View.GONE
        displayDownload = false
        songsAdapter.displaySelect = false
        songsAdapter.notifyDataSetChanged()
    }

}